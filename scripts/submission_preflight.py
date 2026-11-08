#!/usr/bin/env python3
"""Fail-closed, privacy-preserving preflight for an individual submission plan."""
import argparse
from datetime import date, datetime, timedelta, timezone
import hashlib
import json
from pathlib import Path
import re
import sys
from zipfile import BadZipFile, ZipFile

RECIPIENT = "appdevchallenge@hyundai-partners.com"
SUBJECT_PREFIX = "[차량용 앱 개발 공모전]"
REVISION_PREFIX = "[수정제출]"
APK_NAME = re.compile(r"^[^/\\]+_[^/\\]+_release\.apk$")
PLACEHOLDER_TOKENS = {"미확정", "대표자명", "성명", "실제성명", "[미확정]"}
KST = timezone(timedelta(hours=9), name="KST")
OFFICIAL_START_KST = datetime(2026, 10, 21, 0, 0, tzinfo=KST)
OFFICIAL_END_EXCLUSIVE_KST = datetime(2026, 11, 16, 0, 0, 0, tzinfo=KST)


def _date(value):
    try:
        return date.fromisoformat(value) if isinstance(value, str) else None
    except ValueError:
        return None


def _path(plan_path, value):
    if not isinstance(value, str) or not value:
        return None
    candidate = Path(value)
    if candidate.is_absolute():
        return None
    try:
        resolved = (plan_path.parent / candidate).resolve()
        resolved.relative_to(plan_path.parent.resolve())
        return resolved
    except (OSError, RuntimeError, ValueError):
        return None


def _apk_structure(path):
    try:
        with ZipFile(path) as archive:
            names = set(archive.namelist())
    except (BadZipFile, OSError):
        return False
    return "AndroidManifest.xml" in names and "classes.dex" in names


def _evidence_matches(evidence, digest, method):
    return (isinstance(evidence, dict) and evidence.get("apk_sha256") == digest and
            evidence.get("method") == method and evidence.get("verified") is True)


def check_plan(plan_path, now=None):
    """Return non-sensitive statuses; ``now`` is injectable only for tests."""
    plan_path = Path(plan_path).resolve()
    failed = []
    notices = [
        "declaration: consent confirmation is a user declaration, not proof of signature authenticity",
        "scope: this CLI does not authenticate consent, send email, access GitHub, or execute/install APKs",
        "submission-route: official guidance mentions a submission page and email; no additional page was verified",
        "evidence: JSON signing/installation records check consistency only; they do not run apksigner or install an APK",
        "fixture: synthetic APK fixtures are not real build, signing, installation, or device evidence",
    ]
    try:
        plan = json.loads(plan_path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError, json.JSONDecodeError):
        return {"passed": False, "failed_checks": ["plan.readable_json"], "notices": notices}
    if not isinstance(plan, dict):
        return {"passed": False, "failed_checks": ["plan.object"], "notices": notices}

    current = now if now is not None else datetime.now(timezone.utc)
    if not isinstance(current, datetime) or current.tzinfo is None:
        return {"passed": False, "failed_checks": ["clock.timezone_aware"], "notices": notices}
    current_kst = current.astimezone(KST)
    if current_kst < OFFICIAL_START_KST:
        failed.append("official_period.not_started")
    if current_kst >= OFFICIAL_END_EXCLUSIVE_KST:
        failed.append("official_deadline")
    notices.append("official-period: 2026-10-21 00:00 through 2026-11-15 23:59:59.999999 KST; 2026-11-16 00:00:00 KST is exclusive")

    if plan.get("unit") != "individual":
        failed.append("unit.individual")
    if type(plan.get("participant_count")) is not int or plan.get("participant_count") != 1:
        failed.append("participant_count")

    form = _path(plan_path, plan.get("application_form"))
    if form is None or not form.is_file():
        failed.append("application_form.exists")
    attachments = plan.get("attachments")
    if not isinstance(attachments, list) or not attachments:
        failed.append("attachments.nonempty")
    else:
        for item in attachments:
            path = _path(plan_path, item)
            if path is None or not path.is_file():
                failed.append("attachment.exists")
                break
        if form and form.is_file() and not any(_path(plan_path, item) == form for item in attachments):
            failed.append("attachments.includes_application_form")

    consent = plan.get("consent")
    if not isinstance(consent, dict):
        failed.append("consent.object")
    else:
        if consent.get("confirmed") is not True:
            failed.append("consent.confirmed")
        if consent.get("signature_present") is not True:
            failed.append("consent.signature_present")
        consent_date = _date(consent.get("date"))
        if consent_date is None:
            failed.append("consent.date")
        elif consent_date > current_kst.date():
            failed.append("consent.date_not_future")

    email = plan.get("email")
    if not isinstance(email, dict):
        failed.append("email.object")
    else:
        if email.get("recipient") != RECIPIENT:
            failed.append("email.recipient")
        if email.get("sender_confirmed") is not True:
            failed.append("email.sender_confirmed")
        subject = email.get("subject")
        if not isinstance(subject, str) or not subject:
            failed.append("email.subject")
        else:
            base_subject = subject
            if email.get("revision") is True:
                if not base_subject.startswith(REVISION_PREFIX + " "):
                    failed.append("email.revision_subject")
                else:
                    base_subject = base_subject[len(REVISION_PREFIX) + 1:]
                if email.get("original_sender_matches") is not True:
                    failed.append("email.original_sender_matches")
            elif email.get("revision") is not False:
                failed.append("email.revision")
            if not base_subject.startswith(SUBJECT_PREFIX + " "):
                failed.append("email.subject")
            else:
                tail = base_subject[len(SUBJECT_PREFIX) + 1:].strip()
                tokens = tail.split("_")
                if len(tokens) != 2 or any(not token.strip() or token.strip() in PLACEHOLDER_TOKENS for token in tokens):
                    failed.append("email.subject")

    optional = plan.get("optional")
    if optional is None:
        optional = {}
    if not isinstance(optional, dict):
        failed.append("optional.object")
        optional = {}
    video = optional.get("video")
    if video is None:
        notices.append("optional.video: absent; optional bonus does not block minimum submission")
    else:
        if _path(plan_path, video) is None or not _path(plan_path, video).is_file():
            failed.append("optional.video.exists")
    github = optional.get("github")
    if github is not None:
        if not isinstance(github, dict):
            failed.append("optional.github.object")
        else:
            for key in ("source", "apk"):
                path = _path(plan_path, github.get(key))
                if path is None or not path.is_file():
                    failed.append("optional.github." + key + ".exists")
            if github.get("reviewer_access_confirmed") is not True:
                failed.append("optional.github.reviewer_access_confirmed")
            notices.append("optional.github: user confirmation is not proof of actual GitHub access")

    apk = optional.get("apk")
    if apk is not None:
        if not isinstance(apk, dict):
            failed.append("optional.apk.object")
        else:
            apk_path = _path(plan_path, apk.get("path"))
            name = Path(apk.get("path")).name if isinstance(apk.get("path"), str) else ""
            if apk_path is None or not apk_path.is_file():
                failed.append("optional.apk.exists")
            if not APK_NAME.fullmatch(name) or "debug" in name.lower() or "unsigned" in name.lower():
                failed.append("optional.apk.filename")
            if apk.get("kind") != "release":
                failed.append("optional.apk.kind")
            if apk_path and apk_path.is_file():
                try:
                    digest = hashlib.sha256(apk_path.read_bytes()).hexdigest()
                except OSError:
                    failed.append("optional.apk.readable")
                else:
                    if not _apk_structure(apk_path):
                        failed.append("optional.apk.structure")
                    if not _evidence_matches(apk.get("signing_evidence"), digest, "apksigner"):
                        failed.append("optional.apk.signing_evidence")
                    if not _evidence_matches(apk.get("installation_evidence"), digest, "adb"):
                        failed.append("optional.apk.installation_evidence")
                    notices.append("optional.apk.sha256: " + digest)
    if isinstance(github, dict):
        github_apk_path = _path(plan_path, github.get("apk"))
        optional_apk_path = _path(plan_path, apk.get("path")) if isinstance(apk, dict) else None
        if optional_apk_path is None:
            failed.append("optional.github.apk.release_evidence")
        elif github_apk_path != optional_apk_path:
            failed.append("optional.github.apk.matches_optional_apk")
    return {"passed": not failed, "failed_checks": sorted(set(failed)), "notices": notices}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plan", required=True, type=Path, help="JSON plan path")
    args = parser.parse_args(argv)
    result = check_plan(args.plan)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if result["passed"] else 1


if __name__ == "__main__":
    sys.exit(main())

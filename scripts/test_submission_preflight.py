#!/usr/bin/env python3
"""Synthetic tests for the individual submission preflight checker."""
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest import mock
from datetime import datetime, timezone
import hashlib
from zipfile import ZipFile, ZIP_DEFLATED


SPEC = importlib.util.spec_from_file_location(
    "submission_preflight", Path(__file__).with_name("submission_preflight.py")
)
checker = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(checker)


class SubmissionPreflightTest(unittest.TestCase):
    def plan(self, root, **overrides):
        plan = {
            "unit": "individual",
            "participant_count": 1,
            "application_form": "form.pdf",
            "consent": {"confirmed": True, "signature_present": True, "date": "2026-11-01"},
            "email": {
                "recipient": "appdevchallenge@hyundai-partners.com",
                "sender_confirmed": True,
                "subject": "[차량용 앱 개발 공모전] 합성성명_합성성명",
                "revision": False,
                "original_sender_matches": True,
            },
            "deadline": "2026-12-01",
            "as_of": "2026-11-08",
            "attachments": ["form.pdf", "consent.pdf"],
            "optional": {},
        }
        plan.update(overrides)
        return plan

    def write_plan(self, directory, plan, *files):
        for name in files:
            (directory / name).write_bytes(b"synthetic fixture")
        path = directory / "plan.json"
        path.write_text(json.dumps(plan, ensure_ascii=False), encoding="utf-8")
        return path

    def test_valid_individual_plan_passes_without_printing_pii(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root)
            plan["application_form"] = "form.pdf"
            plan["attachments"] = ["form.pdf", "consent.pdf"]
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path)
            self.assertTrue(result["passed"], result)
            rendered = json.dumps(result, ensure_ascii=False)
            self.assertNotIn("합성참가자", rendered)
            self.assertNotIn("appdevchallenge@hyundai-partners.com", rendered)

    def test_missing_consent_fails_closed_and_explains_declaration_limit(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, consent={"confirmed": False, "signature_present": False})
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path)
            self.assertFalse(result["passed"])
            self.assertIn("consent.confirmed", result["failed_checks"])
            self.assertTrue(any(note.startswith("declaration:") for note in result["notices"]))

    def test_revision_requires_original_sender_match(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            email = self.plan(root)["email"] | {"revision": True, "original_sender_matches": False}
            path = self.write_plan(root, self.plan(root, email=email), "form.pdf", "consent.pdf")
            result = checker.check_plan(path)
            self.assertFalse(result["passed"])
            self.assertIn("email.original_sender_matches", result["failed_checks"])

    def test_optional_missing_video_does_not_block(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            path = self.write_plan(root, self.plan(root), "form.pdf", "consent.pdf")
            result = checker.check_plan(path)
            self.assertTrue(result["passed"], result)
            self.assertTrue(any(note.startswith("optional.video:") for note in result["notices"]))

    def test_debug_or_unsigned_apk_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk = {"path": "app-debug.apk", "kind": "release", "signed": True, "installation_verified": True}
            path = self.write_plan(root, self.plan(root, optional={"apk": apk}), "form.pdf", "consent.pdf", "app-debug.apk")
            result = checker.check_plan(path)
            self.assertFalse(result["passed"])
            self.assertIn("optional.apk.filename", result["failed_checks"])

    def test_release_apk_requires_signature_and_install_evidence(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk = {"path": "team_app_release.apk", "kind": "release", "signed": False, "installation_verified": False}
            path = self.write_plan(root, self.plan(root, optional={"apk": apk}), "form.pdf", "consent.pdf", "team_app_release.apk")
            result = checker.check_plan(path)
            self.assertFalse(result["passed"])
            self.assertIn("optional.apk.structure", result["failed_checks"])
            self.assertIn("optional.apk.signing_evidence", result["failed_checks"])
            self.assertIn("optional.apk.installation_evidence", result["failed_checks"])

    def test_expired_or_malformed_dates_fail(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, deadline="2026-11-07", consent={"confirmed": True, "signature_present": True, "date": "not-a-date"})
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("consent.date", result["failed_checks"])

    def test_missing_attachment_path_fails(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            path = self.write_plan(root, self.plan(root), "form.pdf")
            result = checker.check_plan(path)
            self.assertFalse(result["passed"])
            self.assertIn("attachment.exists", result["failed_checks"])

    def test_real_individual_subject_does_not_require_placeholder_literal(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            email = self.plan(root)["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            path = self.write_plan(root, self.plan(root, email=email), "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertTrue(result["passed"], result)

    def test_revision_prefix_is_checked_separately(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            email = self.plan(root)["email"] | {
                "revision": True,
                "subject": "[수정제출] [차량용 앱 개발 공모전] 합성성명_합성성명",
            }
            path = self.write_plan(root, self.plan(root, email=email), "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertTrue(result["passed"], result)

    def test_official_deadline_is_not_overridden_by_plan_dates(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, as_of="2027-01-01", deadline="2030-01-01")
            email = plan["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            plan["email"] = email
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2027, 1, 1, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("official_deadline", result["failed_checks"])

    def test_deadline_last_kst_minute_is_inclusive_and_next_minute_fails(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, consent={"confirmed": True, "signature_present": True, "date": "2026-11-15"})
            plan["email"] = plan["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            inclusive = checker.check_plan(path, now=datetime(2026, 11, 15, 14, 59, tzinfo=timezone.utc))
            exclusive = checker.check_plan(path, now=datetime(2026, 11, 15, 15, 0, tzinfo=timezone.utc))
            self.assertTrue(inclusive["passed"], inclusive)
            self.assertFalse(exclusive["passed"])

    def test_future_consent_date_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, consent={"confirmed": True, "signature_present": True, "date": "2026-11-09"})
            plan["email"] = plan["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("consent.date_not_future", result["failed_checks"])

    def test_malformed_types_fail_closed_without_exception(self):
        cases = [
            {"participant_count": True},
            {"email": {"subject": None, "revision": None}},
            {"optional": {"apk": {"path": None}}},
        ]
        for change in cases:
            with self.subTest(change=change), tempfile.TemporaryDirectory() as raw:
                root = Path(raw)
                plan = self.plan(root)
                for key, value in change.items():
                    if isinstance(value, dict):
                        plan[key] = plan.get(key, {}) | value
                    else:
                        plan[key] = value
                path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
                result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
                self.assertFalse(result["passed"])

    def test_video_and_github_declarations_require_paths_and_user_confirmations(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            optional = {
                "video": "missing.mp4",
                "github": {"source": "source.zip", "apk": "app.apk", "reviewer_access_confirmed": False},
            }
            path = self.write_plan(root, self.plan(root, optional=optional), "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("optional.video.exists", result["failed_checks"])
            self.assertIn("optional.github.reviewer_access_confirmed", result["failed_checks"])

    def test_apk_requires_realistic_zip_and_matching_external_evidence(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk_path = root / "team_app_release.apk"
            with ZipFile(apk_path, "w", ZIP_DEFLATED) as archive:
                archive.writestr("AndroidManifest.xml", b"manifest")
                archive.writestr("classes.dex", b"dex")
                archive.writestr("META-INF/CERT.RSA", b"signature")
            digest = hashlib.sha256(apk_path.read_bytes()).hexdigest()
            apk = {
                "path": apk_path.name,
                "kind": "release",
                "signed": True,
                "installation_verified": True,
                "signing_evidence": {"apk_sha256": digest, "method": "apksigner", "verified": True},
                "installation_evidence": {"apk_sha256": digest, "method": "adb", "verified": True},
            }
            plan = self.plan(root, optional={"apk": apk})
            plan["email"] = plan["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertTrue(result["passed"], result)

    def test_named_fixture_with_boolean_claims_is_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk = {"path": "team_app_release.apk", "kind": "release", "signed": True, "installation_verified": True}
            plan = self.plan(root, optional={"apk": apk})
            plan["email"] = plan["email"] | {"subject": "[차량용 앱 개발 공모전] 합성성명_합성성명"}
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf", "team_app_release.apk")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("optional.apk.signing_evidence", result["failed_checks"])



    def test_v2_v3_style_zip_without_meta_inf_is_accepted_with_external_evidence(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk_path = root / "team_app_release.apk"
            with ZipFile(apk_path, "w", ZIP_DEFLATED) as archive:
                archive.writestr("AndroidManifest.xml", b"manifest")
                archive.writestr("classes.dex", b"dex")
            digest = hashlib.sha256(apk_path.read_bytes()).hexdigest()
            apk = {
                "path": apk_path.name, "kind": "release",
                "signing_evidence": {"apk_sha256": digest, "method": "apksigner", "verified": True},
                "installation_evidence": {"apk_sha256": digest, "method": "adb", "verified": True},
            }
            plan = self.plan(root, optional={"apk": apk})
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertTrue(result["passed"], result)
            self.assertTrue(any(note.startswith("fixture:") for note in result["notices"]))

    def test_github_apk_must_match_release_apk_evidence(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            source = root / "source.zip"
            github_apk = root / "team_app_release.apk"
            source.write_bytes(b"source")
            with ZipFile(github_apk, "w", ZIP_DEFLATED) as archive:
                archive.writestr("AndroidManifest.xml", b"manifest")
                archive.writestr("classes.dex", b"dex")
            digest = hashlib.sha256(github_apk.read_bytes()).hexdigest()
            github = {"source": source.name, "apk": github_apk.name, "reviewer_access_confirmed": True}
            plan = self.plan(root, optional={"github": github})
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf", github_apk.name)
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("optional.github.apk.release_evidence", result["failed_checks"])

    def test_github_debug_apk_and_path_mismatch_are_rejected(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            source = root / "source.zip"
            debug_apk = root / "app-debug.apk"
            release_apk = root / "team_app_release.apk"
            source.write_bytes(b"source")
            debug_apk.write_bytes(b"debug")
            release_apk.write_bytes(b"release")
            github = {"source": source.name, "apk": debug_apk.name, "reviewer_access_confirmed": True}
            plan = self.plan(root, optional={"github": github, "apk": {"path": release_apk.name}})
            path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("optional.github.apk.matches_optional_apk", result["failed_checks"])

    def test_optional_apk_read_failure_is_reported_without_exception_or_path(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            apk_name = "team_app_release.apk"
            apk = {
                "path": apk_name,
                "kind": "release",
                "signing_evidence": {},
                "installation_evidence": {},
            }
            path = self.write_plan(root, self.plan(root, optional={"apk": apk}), "form.pdf", "consent.pdf", apk_name)
            with mock.patch.object(Path, "read_bytes", side_effect=PermissionError("private-secret-path")):
                result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("optional.apk.readable", result["failed_checks"])
            rendered = json.dumps(result, ensure_ascii=False)
            self.assertNotIn("private-secret-path", rendered)
            self.assertNotIn(str(root), rendered)

    def test_nul_and_symlink_loop_paths_fail_without_traceback(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            plan = self.plan(root, application_form="bad\\x00path", attachments=["bad\\x00path", "consent.pdf"])
            path = self.write_plan(root, plan, "consent.pdf")
            result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
            self.assertFalse(result["passed"])
            self.assertIn("application_form.exists", result["failed_checks"])
            self.assertNotIn(str(root), json.dumps(result))

    def test_subject_requires_two_nonempty_non_placeholder_tokens(self):
        invalid = [
            "[차량용 앱 개발 공모전] _",
            "[차량용 앱 개발 공모전] 미확정_성명",
            "[차량용 앱 개발 공모전] 대표자명_성명",
            "[차량용 앱 개발 공모전] 합성성명_ ",
        ]
        for subject in invalid:
            with self.subTest(subject=subject), tempfile.TemporaryDirectory() as raw:
                root = Path(raw)
                plan = self.plan(root, email=self.plan(root)["email"] | {"subject": subject})
                path = self.write_plan(root, plan, "form.pdf", "consent.pdf")
                result = checker.check_plan(path, now=datetime(2026, 11, 8, 12, tzinfo=timezone.utc))
                self.assertFalse(result["passed"])
                self.assertIn("email.subject", result["failed_checks"])

    def test_last_second_fraction_is_inclusive_and_midnight_is_exclusive(self):
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            path = self.write_plan(root, self.plan(root), "form.pdf", "consent.pdf")
            last_fraction = checker.check_plan(path, now=datetime(2026, 11, 15, 14, 59, 59, 999999, tzinfo=timezone.utc))
            next_day = checker.check_plan(path, now=datetime(2026, 11, 15, 15, 0, tzinfo=timezone.utc))
            self.assertTrue(last_fraction["passed"], last_fraction)
            self.assertFalse(next_day["passed"])

if __name__ == "__main__":
    unittest.main()

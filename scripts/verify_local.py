#!/usr/bin/env python3
"""Repeatable checks. JVM/Compose source checks never claim to produce or run an APK."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / "docs" / "evidence"


def snapshot():
    paths = list((ROOT / "app/src").rglob("*.kt")) + list((ROOT / "app/src").rglob("*.xml"))
    paths += [p for p in (ROOT / "app/src/main/res").rglob("*") if p.is_file()]
    paths += list((ROOT / "verification/engine-acceptance/src").rglob("*.kt"))
    paths += list((ROOT / "engine-tests/src").rglob("*.kt"))
    paths += [ROOT / name for name in ["settings.gradle.kts", "build.gradle.kts", "app/build.gradle.kts",
              "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar",
              "verification/engine-acceptance/build.gradle.kts", "verification/ui-compile-check/build.gradle.kts",
              "engine-tests/build.gradle.kts", "engine-tests/settings.gradle.kts",
              "scripts/verify_local.py", "scripts/test_verify_local.py"]]
    return {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(set(paths))}


def verification_plan(jvm_only=False):
    checks = [
        ("unit", ["-p", "verification/engine-acceptance", "test", "--rerun-tasks"]),
        ("engine-unit", ["-p", "engine-tests", "test", "--rerun-tasks"]),
        ("compile-demo", ["-p", "verification/ui-compile-check", "-Pflavor=demo", "compileKotlin", "--rerun-tasks"]),
        ("compile-pleos", ["-p", "verification/ui-compile-check", "-Pflavor=pleos", "compileKotlin", "--rerun-tasks"]),
        ("compile-ui-tests-demo", ["-p", "verification/ui-compile-check", "-Pflavor=demo", "-PincludeAndroidTests=true", "compileKotlin", "--rerun-tasks"]),
        ("compile-ui-tests-pleos", ["-p", "verification/ui-compile-check", "-Pflavor=pleos", "-PincludeAndroidTests=true", "compileKotlin", "--rerun-tasks"]),
    ]
    if not jvm_only:
        checks += [("pleos-dependencies", [":app:dependencies", "--configuration", "pleosDebugRuntimeClasspath"]),
                   ("assemble-debug", ["assembleDebug"])]
    return checks


def read_test_results(directory, module):
    cases = []
    for path in sorted(directory.glob("TEST-*.xml")):
        suite = ET.parse(path).getroot()
        cases.extend({"module": module, "class": case.attrib["classname"], "test": case.attrib["name"],
                      "passed": case.find("failure") is None and case.find("error") is None and case.find("skipped") is None}
                     for case in suite.findall("testcase"))
    return cases


def summarize_tests(tests):
    modules = {}
    unique_methods = set()
    for case in tests:
        modules[case["module"]] = modules.get(case["module"], 0) + 1
        # JUnit Platform appends () to plain method display names; preserve raw XML labels.
        name = case["test"]
        method = name[:-2] if name.endswith("()") and name[:-2].isidentifier() else name
        unique_methods.add((case["class"], method))
    return {"testCount": len(unique_methods),
            "testExecutions": len(tests), "moduleTestCounts": modules, "testCases": tests}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jvm-only", action="store_true", help="Run deterministic unit and native Kotlin/Compose source checks, not APK/device gates")
    args = parser.parse_args()
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    before = snapshot()
    checks = verification_plan(args.jvm_only)
    test_roots = {"unit": ROOT / "verification/engine-acceptance/build/test-results/test",
                  "engine-unit": ROOT / "engine-tests/build/test-results/test"}
    results = []
    tests = []
    for name, task in checks:
        command = [str(ROOT / "gradlew"), *task, "--no-daemon", "--console=plain"]
        print("Running " + name, flush=True)
        try:
            result = subprocess.run(command, cwd=ROOT, text=True, stdout=subprocess.PIPE,
                                    stderr=subprocess.STDOUT, timeout=600)
            code, output = result.returncode, result.stdout
        except subprocess.TimeoutExpired as exc:
            code = 124
            output = (exc.stdout or b"").decode(errors="replace") if isinstance(exc.stdout, bytes) else (exc.stdout or "")
        if name == "pleos-dependencies" and "FAILED" in output:
            code = code or 1  # Gradle dependency reports can exit zero with unresolved coordinates.
        if name in test_roots and code == 0:
            module_tests = read_test_results(test_roots[name], name)
            tests.extend(module_tests)
            if not module_tests:
                code = 1
                output += "\nVerification failed: no JUnit XML cases found for " + name + "\n"
        (EVIDENCE / (name + ".log")).write_text(output)
        results.append({"name": name, "command": command, "exitCode": code,
                        "log": "docs/evidence/" + name + ".log", "result": "PASS" if code == 0 else "FAILED_OR_BLOCKED"})
        print(name + ": " + str(code), flush=True)

    after = snapshot()
    assembled = any(item["name"] == "assemble-debug" and item["exitCode"] == 0 for item in results)
    apk_files = sorted((ROOT / "app/build/outputs/apk").rglob("*.apk")) if assembled else []
    receipt = {"scope": "jvm-and-compose-source-only" if args.jvm_only else "source-and-apk-build-attempt",
               "checks": results, **summarize_tests(tests),
               "sourceSha256": after, "sourcesStable": before == after,
               "apkProduced": bool(apk_files),
               "apkSha256": {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in apk_files},
               "deviceExecutionVerified": False,
               "notice": "Source compilation is not Android resource linking, APK installation, real SDK callbacks, physical audio, or emulator UI verification."}
    (EVIDENCE / "verification-result.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2))
    print(json.dumps({"checks": results, "testCount": receipt["testCount"],
                      "testExecutions": receipt["testExecutions"], "apkProduced": receipt["apkProduced"]}, indent=2))
    return 0 if before == after and all(item["exitCode"] == 0 for item in results) and tests and all(t["passed"] for t in tests) else 1


if __name__ == "__main__":
    sys.exit(main())

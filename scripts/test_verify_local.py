"""Tests for verification bookkeeping; fixtures are not device/build success evidence."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location("verify_local", Path(__file__).with_name("verify_local.py"))
runner = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(runner)


class VerificationRunnerTest(unittest.TestCase):
    def test_instrumentation_sources_are_compiled_for_both_flavors(self):
        checks = dict(runner.verification_plan(jvm_only=True))
        for flavor in ["demo", "pleos"]:
            command = checks.get("compile-ui-tests-" + flavor, [])
            self.assertIn("-PincludeAndroidTests=true", command)
            self.assertIn("-Pflavor=" + flavor, command)

    def test_snapshot_includes_reference_bitmap_resources(self):
        snapshot = runner.snapshot()
        self.assertIn("app/src/main/res/drawable-nodpi/img_genesis_car_base.webp", snapshot)
        self.assertIn("app/src/main/res/drawable-nodpi/img_warm_fox.webp", snapshot)

    def test_both_independent_jvm_modules_are_executed(self):
        checks = dict(runner.verification_plan(jvm_only=True))
        self.assertIn("unit", checks)
        self.assertIn("engine-unit", checks)
        self.assertEqual(["-p", "engine-tests"], checks["engine-unit"][:2])

    def test_duplicate_cases_are_counted_as_executions_not_unique_tests(self):
        cases = [dict(module=module, **{"class": "Example", "test": "same_case", "passed": True})
                 for module in ["unit", "engine-unit"]]
        summary = runner.summarize_tests(cases)
        self.assertEqual(1, summary["testCount"])
        self.assertEqual(2, summary["testExecutions"])
        self.assertEqual({"unit": 1, "engine-unit": 1}, summary["moduleTestCounts"])

    def test_xml_reader_preserves_module_and_failed_or_skipped_status(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "TEST-example.xml"
            path.write_text('<testsuite><testcase classname="Example" name="pass"/>'
                            '<testcase classname="Example" name="skipped"><skipped/></testcase></testsuite>')
            cases = runner.read_test_results(Path(directory), "engine-unit")
            self.assertEqual([True, False], [case["passed"] for case in cases])
            self.assertTrue(all(case["module"] == "engine-unit" for case in cases))

    def test_junit_platform_empty_parentheses_do_not_inflate_unique_count(self):
        cases = [{"module": "unit", "class": "Example", "test": "same_case", "passed": True},
                 {"module": "engine-unit", "class": "Example", "test": "same_case()", "passed": True}]
        summary = runner.summarize_tests(cases)
        self.assertEqual(1, summary["testCount"])
        self.assertEqual(2, summary["testExecutions"])
        self.assertEqual("same_case()", summary["testCases"][1]["test"])


if __name__ == "__main__":
    unittest.main()

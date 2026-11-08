"""The delivered source archive must contain every verified build input."""
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
from verify_local import snapshot

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location("package_source", Path(__file__).with_name("package_source.py"))
PACKAGER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PACKAGER)


class SourceArchiveTest(unittest.TestCase):
    def test_archive_contains_current_verified_build_inputs(self):
        build_archive = PACKAGER.build_archive(ROOT)
        expected = snapshot()
        with zipfile.ZipFile(build_archive[0]) as archive:
            missing = set(expected) - set(archive.namelist())
            self.assertFalse(missing, f"Source archive is missing build inputs: {sorted(missing)}")
            for name, checksum in expected.items():
                with self.subTest(path=name):
                    self.assertEqual(checksum, hashlib.sha256(archive.read(name)).hexdigest())

    def test_tracked_spec_includes_all_python_scripts(self):
        spec = PACKAGER.load_spec(ROOT / "source-manifest.json")
        names = PACKAGER.resolve_inputs(ROOT, spec)
        self.assertIn("scripts/package_source.py", names)
        self.assertIn("scripts/test_source_archive.py", names)
        self.assertTrue(all(path in names for path in PACKAGER.ROOT.glob("scripts/*.py")
                            for path in [path.relative_to(ROOT).as_posix()]))
        self.assertNotIn(spec["metadataPath"], names)

    def test_rejects_traversal_absolute_symlink_and_sensitive_inputs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "safe.txt").write_text("ok")
            (root / ".env.production").write_text("SECRET=x")
            (root / "link.txt").symlink_to(root / "safe.txt")
            (root / "parent").mkdir()
            (root / "parent" / "nested.txt").write_text("ok")
            (root / "parent-link").symlink_to(root / "parent", target_is_directory=True)
            for name in ["../escape.txt", "/tmp/absolute.txt", ".env.production", "link.txt", "parent-link/nested.txt", "credentials.json", "signing.properties", "id_ed25519", "app/release.jks"]:
                with self.subTest(name=name):
                    with self.assertRaises(ValueError):
                        PACKAGER.validate_input(root, name)

    def test_public_submission_named_files_are_allowed_but_root_is_not(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "scripts").mkdir()
            (root / "scripts" / "submission_preflight.py").write_text("pass")
            (root / "docs").mkdir()
            (root / "docs" / "submission-progress.json").write_text("{}")
            (root / "submission").mkdir()
            (root / "submission" / "private.zip").write_text("no")
            self.assertTrue(PACKAGER.validate_input(root, "scripts/submission_preflight.py"))
            self.assertTrue(PACKAGER.validate_input(root, "docs/submission-progress.json"))
            with self.assertRaises(ValueError):
                PACKAGER.validate_input(root, "submission/private.zip")

    def test_rejects_generated_outputs_and_excludes_them_from_globs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "app/src").mkdir(parents=True)
            (root / "app/src/Main.kt").write_text("source")
            (root / "app/build").mkdir(parents=True)
            (root / "app/build/out.apk").write_text("apk")
            (root / "app/src/.gradle").mkdir()
            (root / "app/src/.gradle/cache").write_text("cache")
            (root / "source-manifest.json").write_text("{}")
            (root / "scripts").mkdir()
            (root / "scripts/package_source.py").write_text("pass")
            (root / "scripts/test_source_archive.py").write_text("pass")
            spec = {"metadataPath": "portable-source-metadata.json", "files": ["app/src/**"]}
            names = PACKAGER.resolve_inputs(root, spec)
            self.assertIn("app/src/Main.kt", names)
            self.assertNotIn("app/src/.gradle/cache", names)
            for name in ["app/build/out.apk", "app/src/.gradle/cache"]:
                with self.assertRaises(ValueError):
                    PACKAGER.validate_input(root, name)

    def test_metadata_path_is_safe_and_cannot_collide(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "source-manifest.json").write_text("{}")
            for metadata in ["../escape.json", "/tmp/metadata.json", "source-manifest.json", "required.txt"]:
                with self.subTest(metadata=metadata):
                    spec = {"metadataPath": metadata, "files": ["source-manifest.json", "required.txt"]}
                    if metadata == "required.txt":
                        (root / metadata).write_text("collision")
                    with self.assertRaises(ValueError):
                        PACKAGER.resolve_inputs(root, spec)

    def test_generated_metadata_does_not_self_reference(self):
        spec = PACKAGER.load_spec(ROOT / "source-manifest.json")
        self.assertNotIn(spec["metadataPath"], PACKAGER.resolve_inputs(ROOT, spec))

    def test_fresh_fixture_requires_packaging_and_rejects_missing_required_input(self):
        with tempfile.TemporaryDirectory() as directory:
            fixture = Path(directory)
            for relative in ["scripts", "app/src", "engine-tests/src", "verification", "gradle/wrapper", "docs"]:
                (fixture / relative).mkdir(parents=True)
            for relative in ["settings.gradle.kts", "build.gradle.kts", "app/build.gradle.kts", "engine-tests/build.gradle.kts", "engine-tests/settings.gradle.kts", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "scripts/verify_local.py", "scripts/package_source.py", "scripts/test_source_archive.py", "source-manifest.json", "docs/SESSION_HANDOFF.md", "app/src/Main.kt", "engine-tests/src/Test.kt"]:
                (fixture / relative).write_text("{}")
            spec = {"metadataPath": "portable-source-metadata.json", "files": ["settings.gradle.kts", "build.gradle.kts", "app/build.gradle.kts", "app/src/**", "engine-tests/src/**", "scripts/*.py", "docs/**"]}
            (fixture / "source-manifest.json").write_text(json.dumps({"kind": "portable-source-spec", **spec, "files": spec["files"]}))
            self.assertFalse((fixture / "artifacts/source-manifest.json").exists())
            archive_path, files = PACKAGER.build_archive(fixture)
            self.assertTrue(archive_path.is_file())
            with zipfile.ZipFile(archive_path) as archive:
                self.assertIsNone(archive.testzip())
                self.assertIn("docs/SESSION_HANDOFF.md", archive.namelist())
                self.assertNotIn("portable-source-metadata.json", files)
            (fixture / "build.gradle.kts").unlink()
            with self.assertRaises(ValueError):
                PACKAGER.resolve_inputs(fixture, spec)
            (fixture / "artifacts" / "source-manifest.json").parent.mkdir(exist_ok=True)
            (fixture / "artifacts" / "source-manifest.json").write_text("legacy")
            (fixture / "build.gradle.kts").write_text("{}")
            legacy = fixture / "artifacts/source-manifest.json"
            before = legacy.read_bytes()
            PACKAGER.build_archive(fixture)
            self.assertEqual(before, legacy.read_bytes())
            with zipfile.ZipFile(archive_path) as archive:
                self.assertNotIn("artifacts/source-manifest.json", archive.namelist())


    def test_metadata_rejects_private_or_sensitive_destinations(self):
        for name in ["submission/metadata.json", "private/metadata.json", ".env.production", "id_ed25519", "package.apk"]:
            with self.subTest(name=name), self.assertRaises(ValueError):
                PACKAGER.validate_metadata_path(ROOT, {"metadataPath": name}, set())

    def test_metadata_rejects_nonportable_or_truncated_names(self):
        for name in ["C:/metadata.json", "bad\x00name.json", "."]:
            with self.subTest(name=name), self.assertRaises(ValueError):
                PACKAGER.validate_metadata_path(ROOT, {"metadataPath": name}, set())

    def test_required_glob_with_only_generated_files_is_missing(self):
        with tempfile.TemporaryDirectory() as directory:
            fixture = Path(directory)
            (fixture / "app/src/.gradle").mkdir(parents=True)
            (fixture / "app/src/.gradle/cache.bin").write_text("generated")
            (fixture / "scripts").mkdir()
            for name in ["scripts/package_source.py", "scripts/test_source_archive.py", "source-manifest.json"]:
                (fixture / name).write_text("fixture")
            with self.assertRaises(ValueError):
                PACKAGER.resolve_inputs(fixture, {"metadataPath": "metadata.json", "files": ["app/src/**"]})



    def test_load_spec_rejects_malformed_json_shapes_with_value_error(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            spec_path = root / "source-manifest.json"
            malformed = [
                [],
                None,
                {"kind": "portable-source-spec", "metadataPath": "metadata.json"},
                {"kind": "portable-source-spec", "metadataPath": "metadata.json", "files": []},
                {"kind": "portable-source-spec", "metadataPath": "metadata.json", "files": "source.txt"},
                {"kind": "portable-source-spec", "metadataPath": "metadata.json", "files": [1]},
                {"kind": "portable-source-spec", "metadataPath": "", "files": ["source.txt"]},
                {"kind": "portable-source-spec", "metadataPath": 1, "files": ["source.txt"]},
            ]
            for value in malformed:
                with self.subTest(value=value):
                    spec_path.write_text(json.dumps(value), encoding="utf-8")
                    with self.assertRaises(ValueError):
                        PACKAGER.load_spec(spec_path)

    def test_resolve_inputs_rejects_missing_or_invalid_files_shape(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "source.txt").write_text("source")
            for files in [None, [], "source.txt", [1]]:
                with self.subTest(files=files):
                    with self.assertRaises(ValueError):
                        PACKAGER.resolve_inputs(root, {"metadataPath": "metadata.json", "files": files})

    def test_private_document_binaries_are_rejected_for_inputs_and_metadata(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for suffix in [".doc", ".docx", ".pdf", ".odt", ".rtf", ".xlsx", ".pptx"]:
                name = "signed-application" + suffix
                (root / name).write_bytes(b"private form")
                with self.subTest(name=name):
                    with self.assertRaises(ValueError):
                        PACKAGER.validate_input(root, name)
                    with self.assertRaises(ValueError):
                        PACKAGER.validate_metadata_path(root, {"metadataPath": name}, set())

if __name__ == "__main__":
    unittest.main()

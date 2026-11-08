"""The delivered source archive must contain every verified build input."""
import hashlib
from pathlib import Path
import unittest
import zipfile

from verify_local import snapshot

ROOT = Path(__file__).resolve().parents[1]


class SourceArchiveTest(unittest.TestCase):
    def test_archive_contains_current_verified_build_inputs(self):
        expected = snapshot()
        with zipfile.ZipFile(ROOT / "artifacts/story-drive-dear-family-source.zip") as archive:
            missing = set(expected) - set(archive.namelist())
            self.assertFalse(missing, f"Source archive is missing build inputs: {sorted(missing)}")
            for name, checksum in expected.items():
                with self.subTest(path=name):
                    self.assertEqual(checksum, hashlib.sha256(archive.read(name)).hexdigest())


if __name__ == "__main__":
    unittest.main()

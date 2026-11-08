#!/usr/bin/env python3
"""Rebuild the portable source archive, including every verified build input."""
import hashlib
import json
from pathlib import Path, PurePosixPath
import zipfile

from verify_local import snapshot

ROOT = Path(__file__).resolve().parents[1]


def main():
    manifest_path = ROOT / "artifacts/source-manifest.json"
    manifest = json.loads(manifest_path.read_text())
    names = set(manifest["files"]) | set(snapshot()) | {
        "scripts/package_source.py", "scripts/test_source_archive.py"
    }
    files = {}
    for name in sorted(names):
        relative = PurePosixPath(name)
        path = ROOT / name
        if (relative.is_absolute() or ".." in relative.parts or "\\" in name
                or path.is_symlink() or not path.is_file()
                or not path.resolve().is_relative_to(ROOT.resolve())):
            raise ValueError(f"Unsafe or missing archive input: {name}")
        if any(part.lower() in {".env", ".git", "id_ed25519", "id_rsa", "local.properties"}
               for part in relative.parts):
            raise ValueError(f"Sensitive archive input: {name}")
        files[name] = hashlib.sha256(path.read_bytes()).hexdigest()
    manifest["files"] = files
    manifest["sourceFileCount"] = len(files)
    text = json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
    archive_path = ROOT / "artifacts/story-drive-dear-family-source.zip"
    with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name in files:
            archive.write(ROOT / name, name)
        archive.writestr("source-manifest.json", text)
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        for name, checksum in files.items():
            assert hashlib.sha256(archive.read(name)).hexdigest() == checksum
    manifest_path.write_text(text)
    print(json.dumps({"archive": str(archive_path), "sourceFileCount": len(files),
                      "sha256": hashlib.sha256(archive_path.read_bytes()).hexdigest()}))


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Build a portable source archive from the tracked source specification."""
import hashlib
import json
from pathlib import Path, PurePosixPath
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SENSITIVE_NAMES = {".git", ".env", "local.properties", "signing.properties", "id_ed25519", "id_rsa"}
SENSITIVE_SUFFIXES = (
    ".jks", ".keystore", ".key", ".p12", ".pfx", ".pem", ".apk",
    ".doc", ".docx", ".pdf", ".odt", ".rtf", ".xlsx", ".pptx",
)
SENSITIVE_PARTS = ("credentials", "client-secret", "private")
GENERATED_DIRS = {".gradle", "build", "cache", "__pycache__", "artifacts"}


def _validate_spec_shape(spec):
    if not isinstance(spec, dict):
        raise ValueError("Invalid portable source specification")
    files = spec.get("files")
    metadata_path = spec.get("metadataPath")
    if (not isinstance(files, list) or not files or
            any(not isinstance(name, str) or not name for name in files) or
            not isinstance(metadata_path, str) or not metadata_path):
        raise ValueError("Invalid portable source specification")


def load_spec(path):
    spec = json.loads(path.read_text(encoding="utf-8"))
    _validate_spec_shape(spec)
    if spec.get("kind") != "portable-source-spec":
        raise ValueError("Invalid portable source specification")
    return spec


def _relative_name(name):
    if not isinstance(name, str) or not name or "\\" in name or "\x00" in name:
        raise ValueError("Archive paths must be portable relative file names")
    relative = PurePosixPath(name)
    if (relative.is_absolute() or ".." in relative.parts or not relative.parts
            or ":" in relative.parts[0]):
        raise ValueError("Archive paths must be portable relative file names")
    return relative


def _sensitive_path(relative):
    parts = relative.parts
    return (parts[0].lower() == "submission"
            or any(part.lower() in SENSITIVE_NAMES or part.lower() in GENERATED_DIRS for part in parts)
            or any(part.lower().startswith(SENSITIVE_PARTS) for part in parts)
            or any(part.lower().startswith(".env.") for part in parts)
            or relative.as_posix().lower().endswith(SENSITIVE_SUFFIXES))


def validate_input(root, name):
    relative = _relative_name(name)
    if _sensitive_path(relative):
        raise ValueError(f"Sensitive or generated archive input: {name}")
    path = root.joinpath(*relative.parts)
    current = root
    for part in relative.parts:
        current /= part
        if current.is_symlink():
            raise ValueError(f"Symlink archive input: {name}")
    if not path.is_file() or not path.resolve().is_relative_to(root.resolve()):
        raise ValueError(f"Unsafe or missing archive input: {name}")
    return path


def validate_metadata_path(root, spec, names):
    relative = _relative_name(spec.get("metadataPath"))
    normalized = relative.as_posix()
    if normalized == "source-manifest.json" or normalized in names:
        raise ValueError(f"Metadata path collides with source input: {normalized}")
    if _sensitive_path(relative):
        raise ValueError("Unsafe metadata destination")
    return normalized


def resolve_inputs(root, spec):
    _validate_spec_shape(spec)
    names = set()
    for pattern in spec["files"]:
        _relative_name(pattern)
        if "*" in pattern:
            if pattern.endswith("/**"):
                base = root / pattern[:-3]
                matches = [path for path in base.rglob("*") if path.is_file()] if base.is_dir() else []
            else:
                matches = [path for path in root.glob(pattern) if path.is_file()]
            matches = [path for path in matches
                       if not any(part.lower() in GENERATED_DIRS
                                  for part in path.relative_to(root).parts)]
            if not matches:
                raise ValueError(f"Missing required source glob: {pattern}")
            for path in matches:
                name = path.relative_to(root).as_posix()
                if not any(part.lower() in GENERATED_DIRS for part in PurePosixPath(name).parts):
                    names.add(name)
        else:
            path = root / pattern
            if path.is_file():
                names.add(PurePosixPath(pattern).as_posix())
            else:
                raise ValueError(f"Missing required build input: {pattern}")
    names.update({"source-manifest.json", "scripts/package_source.py", "scripts/test_source_archive.py"})
    metadata = validate_metadata_path(root, spec, names)
    names.discard(metadata)
    for name in names:
        validate_input(root, name)
    return sorted(names)


def build_archive(root):
    spec = load_spec(root / "source-manifest.json")
    names = resolve_inputs(root, spec)
    metadata_path = validate_metadata_path(root, spec, set(names))
    files = {name: hashlib.sha256(validate_input(root, name).read_bytes()).hexdigest() for name in names}
    metadata = {**spec, "metadataPath": metadata_path, "files": files,
                "sourceFileCount": len(files),
                "metadataKind": "portable-source-package-measurement"}
    text = json.dumps(metadata, ensure_ascii=False, indent=2) + "\n"
    archive_path = root / "artifacts/story-drive-dear-family-source.zip"
    archive_path.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name in names:
            archive.write(root / name, name)
        archive.writestr(metadata_path, text)
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None or metadata_path in files:
            raise ValueError("Invalid generated source archive")
        for name, checksum in files.items():
            if hashlib.sha256(archive.read(name)).hexdigest() != checksum:
                raise ValueError(f"Archive checksum mismatch: {name}")
    return archive_path, files


def main():
    archive_path, files = build_archive(ROOT)
    print(json.dumps({"archive": str(archive_path), "sourceFileCount": len(files),
                      "sha256": hashlib.sha256(archive_path.read_bytes()).hexdigest()}))


if __name__ == "__main__":
    sys.exit(main())

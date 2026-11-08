# Portable source archive

`source-manifest.json`은 저장소에 추적되는 portable source spec이다. `artifacts/`의 legacy manifest나 native verification evidence를 입력으로 사용하지 않는다.

## 생성

깨끗한 checkout에서 다음 명령을 실행한다.

```sh
python3 scripts/package_source.py
```

스크립트는 spec의 필수 빌드 입력과 `scripts/*.py`를 해석하고, 공개 `docs/**`를 포함해 존재하는 파일을 SHA-256으로 기록한 `portable-source-metadata.json`을 ZIP 안에 생성한다. metadata는 입력 파일이 아니므로 자기 자신을 해시하지 않는다. 누락된 필수 입력, traversal/absolute path, 모든 조상 symlink, APK·build/cache, root `submission/`, signing material, credentials와 확장된 secret 파일은 거부한다. `scripts/submission_preflight.py`와 `docs/submission-progress.json`처럼 공개 경로에 있는 일반 소스/문서는 허용한다.

생성 ZIP에는 소스와 빌드 정의만 들어간다. APK/debug/release, keystore, SDK, Gradle cache, private submission은 포함하지 않는다. 생성 후 `zipfile.testzip()`과 모든 입력 해시를 재검증한다.

`source-manifest.json`은 portable spec이고, ZIP의 `portable-source-metadata.json`은 해당 실행에서 생성된 artifact metadata다. 기존 native 검증 기록과 legacy manifest를 삭제하거나 성공으로 위조하지 않는다.

## 공개 입력 경계와 기존 소비자

문서는 tracked portable spec에 명시된 공개 경로만 포함한다. 작업공간에 남아 있는 미추적 역사 로그·연결 안내를 일괄 공개하지 않는다. `scripts/*.py`는 안전 경로 검사 후 자동 포함하며, APK와 영상은 별도의 Git 자료이고 소스 ZIP에는 포함하지 않는다.

ZIP 안의 `source-manifest.json`은 tracked spec이고 파일 해시 사전은 `portable-source-metadata.json`의 `files`다. 예전 Mac 전송 helper가 `source-manifest.json.files`를 해시 사전으로 가정한다면, Mac 재개 시 새 metadata 이름을 읽도록 소비자를 먼저 갱신해야 한다. 기존 `native-build-source-*.zip`과 legacy metadata는 보존했다.

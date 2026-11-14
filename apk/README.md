# 제출용 release APK와 테스트 APK

## 공모전 제출 파일

`apk/심찬영_StoryDriveDearFamily_release.apk`

- 공식 Pleos용 `com.pleos.dearfamily`, versionName 1.0 / versionCode 1.
- 사용자 로컬 release 서명·apksigner v3 검증 성공, debuggable=false, INTERNET 없음.
- Pleos Connect 2.1.0 / API34 ARM64 / user10에 설치·실행하고 설치본 해시 일치를 확인했습니다.
- SHA-256: `38c5b1619ebdf3c8a2475b7aafe7a7a16646072f0af8c33ae79a85cc1c353615`
- 소란/부모님 실제 음성 콜백, 수면 무음, Home/명시적 복귀 smoke 검증. 기존 41개 instrumentation을 release에서 다시 실행한 것은 아닙니다.
- 상세: `docs/design/RELEASE_VERIFICATION_20261114.md`, `docs/evidence/release-native.json`.

설치: `adb install -r apk/심찬영_StoryDriveDearFamily_release.apk` 후 foreground 사용자에서 앱을 실행하세요. 기존 debug 앱과 인증서가 다르면 업데이트가 거부됩니다. 앱 삭제는 저장 데이터를 초기화하므로 반드시 소유자 승인을 받으세요. 실제 Pleos 서비스·좌석 권한과 정차 상태가 필요하며 물리 차량 쓰기·청취 인증은 아닙니다.

## 과거 검증용 debug 파일

아래 debug 파일은 개발 회귀용이며 위 제출용 release와 구분합니다. unsigned release 파일은 업로드하지 않습니다.

> **2026-11-13 검증 빌드:** 실제 ON_DEVICE 음성, 완료 feedback 기반 자동투어, 중지·복귀와 좁은 창 검증을 포함합니다. pleos 기본 계측은 41개 발견/39통과/2조건부 생략/실패0이며, demo 실제 Android TTS 출력이나 실차 제어 성공으로 확대하지 않습니다.

- `apk/StoryDrive_DearFamily_demo_debug.apk`
- `apk/StoryDrive_DearFamily_pleos_debug.apk`

설치: `adb install -r <선택한 APK 경로>`

기존 설치본과 debug 서명이 다르면 업데이트가 거부될 수 있습니다. 앱 삭제는 설정을 초기화하므로 사용자 승인 없이 삭제하지 마세요.

| 파일 | SHA-256 |
|---|---|
| demo | `1933a6737bc6fe2be1938cf68a3f1d62c53f4fd20e3c342945f177fa273a4954` |
| pleos | `c20fccb9603d26683d5b5f8aff2798d6553dcf652265c86c6562e631a6d47b05` |

실제 빌드 source archive: `0299f8be04b2c1783459a02dcafba079ca3e58b408c6388fb389ca7a2797a5b8`.
상세 검증·잔여 제한: `docs/design/SELF_FOLLOWUP_VERIFICATION_20261113.md`, 구조화 증거: `docs/evidence/self-followup-native.json`. 제출용 release 상태는 `docs/SUBMISSION_FINALIZATION.md`를 확인하세요.

`demo`는 실제 차량 SDK 없이 Mock 시연이며 한국어 비네트워크 Android TTS가 없으면 자막으로 대체합니다. `pleos`는 공식 SDK 좌석 read-only 및 ON_DEVICE 음성 경로입니다. 물리 청취·실차 ANC·독립 음장·차량 쓰기는 검증하지 않았습니다. 제출용 release의 서명·설치와 파일명은 이 문서 상단에서 확인하세요.

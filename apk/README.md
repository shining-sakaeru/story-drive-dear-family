# 테스트용 APK

현재 파일은 실제 Mac 빌드에서 생성한 **debug 서명 테스트용 APK**입니다. 공모전 제출용 release APK가 아니며, unsigned release 파일은 업로드하지 않습니다.

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

`demo`는 실제 차량 SDK 없이 Mock 시연이며 한국어 비네트워크 Android TTS가 없으면 자막으로 대체합니다. `pleos`는 공식 SDK 좌석 read-only 및 ON_DEVICE 음성 경로입니다. 물리 청취·실차 ANC·독립 음장·차량 쓰기는 검증하지 않았습니다. 최종 release 서명/설치 검증과 제출용 파일명 확정은 남아 있습니다.

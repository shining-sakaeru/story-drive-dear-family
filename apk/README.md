# 테스트용 APK

현재 파일은 실제 Mac 빌드에서 생성한 **debug 서명 테스트용 APK**입니다. 공모전 제출용 release APK가 아니며, unsigned release 파일은 업로드하지 않습니다.

> **2026-11-12 UI 복원 빌드:** 새 비트맵 UI와 동화/온기/정숙·Spatial 제어를 포함한 실제 Mac 빌드로 교체했습니다. 두 flavor APK 설치·실행, pleos 계측 30개 발견/29통과/1조건부 제외/0실패와 실제 3종 화면 캡처를 확인했습니다. 최종 시각 승인과 실제 음성 검증은 별개입니다.

- `apk/StoryDrive_DearFamily_demo_debug.apk`
- `apk/StoryDrive_DearFamily_pleos_debug.apk`

설치: `adb install -r <선택한 APK 경로>`

기존 설치본과 debug 서명이 다르면 업데이트가 거부될 수 있습니다. 앱 삭제는 설정을 초기화하므로 사용자 승인 없이 삭제하지 마세요.

| 파일 | SHA-256 |
|---|---|
| demo | `679d38dca35837dd8d2c6228883e811108854e22b65e090d667fd4b7ca4d72c2` |
| pleos | `2f93b4f5afe0d737a6c204c18c282efb5884ca31c6b31555b6869da856ebf5e9` |

실제 빌드 source archive: `8a23de0555dc175d93265e7074f2969876df4c9e83ae2561f41c5128d98f7bac`.
상세 검증·잔여 제한: `docs/design/UI_FIDELITY_REFACTOR_REPORT.md`, 구조화 증거: `docs/evidence/ui-fidelity-native.json`.

`demo`는 실제 차량 SDK 없이 Mock 시연, `pleos`는 공식 SDK 좌석 탑승 read-only 경로입니다. 실제 한국어 음성은 현재 에뮬레이터에서 초기화 실패하여 자막으로 대체합니다. 최종 release 서명/설치 검증과 제출용 파일명 확정은 남아 있습니다.

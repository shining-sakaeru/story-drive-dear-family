# 테스트용 APK

현재 파일은 실제 Mac 빌드에서 생성한 **debug 서명 테스트용 APK**입니다. 공모전 제출용 release APK가 아니며, unsigned release 파일은 업로드하지 않습니다.

- `apk/StoryDrive_DearFamily_demo_debug.apk`
- `apk/StoryDrive_DearFamily_pleos_debug.apk`

설치: `adb install -r <선택한 APK 경로>`

`demo`는 실제 차량 SDK 없이 Mock 시연, `pleos`는 공식 SDK 좌석 탑승 read-only 경로입니다. 실제 한국어 음성은 현재 에뮬레이터에서 초기화 실패하여 자막으로 대체합니다. 최종 release 서명/설치 검증과 제출용 파일명 확정은 남아 있습니다.

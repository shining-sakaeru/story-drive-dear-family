# 실제 앱 시연 하이라이트

## 최신 승인 영상

`StoryDrive_DearFamily_live_showcase.mp4`는 공식 Pleos 에뮬레이터의 실제 앱 화면과 ON_DEVICE 음성으로 아이 소란 → 수면·무음 → 부모님 케어를 보여 줍니다. 새 BGM·사전 녹음 대체 내레이션은 추가하지 않았습니다. 원본 앱 음성 트랙의 타임스탬프 간격을 보존해 재인코딩·음량 보정했습니다. 물리 청취 인증, 실차 제어, 고정 30초 자동투어 종료 시연은 아닙니다.

SHA-256: `6f71010bad5c0276e05b7cc613d751dbaa75584312bac963166e911a02895033`

검증 기록: `docs/design/FAMILY_JOURNEY_VERIFICATION_20261113.md`, `docs/design/SELF_FOLLOWUP_VERIFICATION_20261113.md`.

## 과거 무음 영상 기록

아래 설명은 `StoryDrive_DearFamily_demo_captioned.mp4`에만 해당하며 최신 승인 영상의 음성 상태와 혼용하지 않습니다.

- `StoryDrive_DearFamily_demo_captioned.mp4`: 실제 공식 Pleos 에뮬레이터 녹화를 편집한 **28.5초 무음·한국어 자막** 영상. H.264, 1920×1336, 30 fps/855 frames.
- `frames/children.png`, `parents.png`, `cozy.png`, `report.png`: 편집본의 2/9/16/24초 실제 프레임.
- `dearfamily-demo-captioned.ass`: 편집본 자막 원본.
- 계측/빌드 입력과 영상 해시·전체 디코딩·시각 검수 근거: `../docs/evidence/video-editing.json`.

## 영상이 보여 주는 것과 한계

원본의 설정/대기 화면을 제거하고 raw 11.0초부터 28.5초를 발췌했다. 아이 소란, 부모님, 조수석 Cozy, 운전석 리포트 네 단계의 실제 UI를 보존했다. **30초 투어 종료 전환 전체, 수면·게임 조작, 정상 한국어 음성, SDK 값 변경 왕복을 이 영상에서 검증한 것은 아니다.**

영상의 공조·온열·소음/체압 센서·음장 제어는 Mock 상태다. 실제 SDK 영역은 좌석 탑승값 조회/구독 경로이며, 물리적 독립 음장이나 실제 차량 제어를 주장하지 않는다. 녹화 환경에서 한국어 TTS 초기화가 실패해 실제 발화 없이 시연하며, 배경음/합성 음성을 추가하지 않았다.

앱과 Driving View의 원본 프레임은 축소·재배치하지 않고 위/아래 설명 여백을 추가했다. 좌측의 정차 P/0 km/h는 보존하고, 우측 상단의 시스템 사용자 프로필/시계 영역만 가렸다. 새 기기 실행은 하지 않은 서버 편집 산출물이다.

## 재현과 글꼴

편집에는 Noto Sans CJK KR Regular를 사용했다. 원본 글꼴은 `docs/evidence/video-editing.json`에 기록한 공식 Noto CJK 소스/해시로 확인하고, 라이선스는 `OFL-NotoSansCJK.txt`를 참고한다. 글꼴 바이너리와 raw 녹화본은 Git에 포함하지 않는다.

FFmpeg 필터 순서: raw 11.0초 seek → 28.5초 발췌 → top-right `drawbox=1550:0:370:65` → `pad=1920:1336:0:96` → ASS 자막 → 30fps/yuv420p → libx264 CRF20/veryfast → faststart. 원본 재생 속도는 바꾸지 않는다. SHA-256/855프레임/음성 트랙 없음/전체 디코딩 성공은 evidence JSON에 기록했다.

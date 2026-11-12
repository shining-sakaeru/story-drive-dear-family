# UI 동작 계약 — 시안 충실도 복원

## 공통 경계

- 모든 케어 제어는 `MockPleosSdkManager`의 로컬 상태 변경이다. 실제 Vehicle SDK는 기존 좌석 조회 read-only 경로만 유지한다.
- 화면은 재생 요청 상태와 `VoiceStatus`의 실제 준비/발화 상태를 구분한다. 음성 미설치/실패를 재생 성공으로 표현하지 않는다.
- `CarUxRestrictions` 또는 전경/중단 상태가 조작을 허용하지 않으면 ViewModel guard로 변경을 차단한다. 백그라운드·수면에서는 발화와 재생 요청을 중지하고 자동 재개하지 않는다.

## Warm CTA

| 표시 | ViewModel → Mock 경계 | 상태·피드백 | 금지되는 대체 동작 |
|---|---|---|---|
| Play / Stop Story | `toggleStoryPlayback()` | `storyPlaying` 토글, 동화 스크립트/자막 요청; 수면·정숙이면 재생 시작하지 않음 | 조수석 `cozyMode` 변경 |
| Cabin Warmth | `toggleCabinWarmth()` | `cabinWarmth` 토글, Mock 온기/앰버 무드 표시 | 실제 공조·조명 SDK 쓰기 |
| Quiet Cabin | `toggleQuietCabin()` | `quietCabin` 토글, 앱 음성 중지, 버튼 선택 상태 | 실제 ANC/외부 소음 차폐라고 주장 |

CTA의 카드 내부 배치는 최신 사용자 명시 지시에 따른 원본 공통 하단 Dock의 의도적 변경이다.

## Spatial

- Quick Mute는 기존 `AudioNarrator.toggleMute()`를 실제 호출하며 버튼 상태는 `VoiceStatus.muted`를 따른다. 앱 시연 음소거이지 차량 전체/내비 음소거가 아니다.
- 미디어 재생 컨트롤은 동화 재생 요청과 연결한다. 앨범 이미지는 원본 참고 artwork이며 라이선스가 확인된 실제 음악 스트림이 아니다.
- 통풍·열선 등은 범위 제한된 Mock 상태와 선택 피드백만 변경한다. 수직 슬라이더의 접근성 progress 범위와 화면 상태를 일치시킨다.
- 하단 홈/공조/미디어/내비 탭은 선택 상태 및 탭 설명을 바꾼다. 내비 탭 선택은 실제 경로 변경/내비 앱 실행을 뜻하지 않는다.
- Scenario Pill은 선택한 로컬 시나리오를 표시하고 기존 시나리오 상태 전이로 연결한다.

## 검증

각 상태 전이를 독립 JVM 테스트로 검증하고, 버튼/탭/슬라이더→ViewModel 경로는 계측 테스트로 작성한다. Mac 중지 기간에는 후자를 미실행으로 기록한다. 기능 테스트·소스 컴파일만으로 시각 재현을 완료 처리하지 않는다.

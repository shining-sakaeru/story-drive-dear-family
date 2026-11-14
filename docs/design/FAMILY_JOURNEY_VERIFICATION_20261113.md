# 가족 여정 · 실제 앱 음성 시연 검증

검증일: 2026-11-13 UTC. **시청각 시연 마일스톤이며, 실차 통합·release·접수 완료 보고가 아니다.**

## 이번에 확인한 결과

- 기존 승인 Warm 테마를 유지하고 정차용 `가족 여정`에서 아이 소란 → 수면 → 부모님 케어를 직접 선택한다. 동일 대상 좌석의 이전/이후 Mock 상태, 실제 안내 본문, 별도 앱 음성 상태, 선택형 IPO 설명을 표시한다.
- Pleos `ON_DEVICE` SDK 경로에서 실제 초기화·재생 시작·완료·중지·음소거 차단을 관측했다. demo의 Android TTS 경로와 구분한다. 기존 Android TTS engine 초기화 실패를 엔진 설치로 해결했다고 주장하지 않는다.
- SDK AAR가 합병 APK에 추가하던 `INTERNET` 권한을 발견해 Pleos manifest에서 제거했다. 최종 두 APK 모두 권한이 없으며, 그 상태에서 ON_DEVICE 음성 시험과 실제 녹화가 성공했다. 시스템 Speech Service 자체의 네트워크 차단 시험은 별도다.
- 실제 에뮬레이터 화면과 오디오를 함께 기록했다. MP4는 원본 앱 음성만 사용하며 타임스탬프 공백 복원·재인코딩·음량 보정 외에 내레이션/음악을 추가하지 않았다. 음성 트랙 존재와 실제 PCM 신호를 검사했다. **물리 청취·실차 독립 음장·실차 ANC 검증은 아니다.**
- 실제 Android AudioManager의 두 번째 포커스 클라이언트로 포커스 상실 → 중지 → 포커스 복구 후 자동 재생 없음, 허용 철회/복구 후 자동 재생 없음, release 후 늦은 재생 없음까지 관측했다. 같은 앱의 별도 포커스 클라이언트이며 실제 내비 앱과의 상호운용 시험은 아니다.

## 시험 집계 — 서로 다른 종류를 합산하지 않음

| 구분 | 실제 결과 |
|---|---|
| 호스트 JVM | 고유 56개, 여러 하네스 총 90회 실행, 실패 0 |
| Python 스크립트 | 55개 통과 |
| Mac native assemble | demo/pleos debug, pleos androidTest 성공 |
| 양 flavor 앱 unit | 총 10회 실행, 실패/오류/skip 0 |
| 기본 native 전체 시험 | 34개 발견: 33 통과, 1 조건부 skip, 실패 0 |
| 별도 실제 포커스 시험 | 1개 통과 |
| opt-in 실제 녹화 시험 | 1개 통과 |
| 위 세 native 실행의 고유 케이스 합집합 | 36개: 35 통과, 1 skip. 단일 36개 실행이라고 표현하지 않음 |
| 글자 1.5배 | 시연 조작 3개 통과, 별도 6장 캡처 실행 통과 |
| lint | 양 flavor 작업 성공·오류 0. demo 경고 13, pleos 경고 15. Compose custom lint 호환성 문제로 일부 검사가 제외됨 |

skip은 실제 플랫폼이 정차로 알려진 상태라 `unknownStateDoesNotExposeScenarioControls`의 미확인 상태 전제가 성립하지 않은 경우다. 주행/미확인 실기 검증 성공으로 계산하지 않는다.

## 기능별 검증 범위

| 기능 | 이번 근거와 남은 경계 |
|---|---|
| F01 소란 | native 프리셋·소음 임계치 재진입·대표 장면·실제 자막/앱 소리. 입력은 fixture이며 실측 소음 저감이 아님 |
| F02 수면 | native 수면/리셋·story 차단·자막 소거, 원본 영상의 소리/무음 전환. 실제 수면 감지가 아님 |
| F03 청소년 | native 수동 프리셋·대상 존 및 모델 출력. Lo-Fi 실제 음악은 추가하지 않음 |
| F04 부모님 | native 탑승시간 재진입·대표 장면·휴게소 응답. 건강 진단·마사지 실차 명령 아님 |
| F05 조수석 | native 대화 duck/복원 및 운전석 모델 불변. 실제 차량 오디오 덕킹 아님 |
| F06 간식/게임 | 정차에서 실제 UI 선택. 주행/unknown 실기 차단은 아직 별도 |
| F07 음성 | Pleos ON_DEVICE·앱 INTERNET 없음·start/done/stop·실제 디지털 출력 캡처. SDK 직접 출력에 Android PCM 175ms 처리를 적용했다고 주장하지 않음 |
| F08 quiet/mute | native quiet와 story 독립 동작, SDK mute/중지 후 재생 차단. 실제 ANC 아님 |
| F09 온기 | native 이야기·quiet와 독립 토글. 차량 공조 쓰기 아님 |
| F10 풍량/열선 | native 실제 UI 조작과 해당 Mock 좌석 반영. 실차 쓰기 아님 |
| F11 탭/휴게소 | native 탭 선택과 수락 응답. 실제 내비 목적지 전송 아님 |
| F12 SDK read-only | 경로는 유지. Console→DDS→좌석 callback→동일 UI 필드 왕복은 미검증이며 Mock 성공으로 대체하지 않음 |
| F13 안전/복귀 | 실제 정차 관측, 투어 Activity recreate 중지, 실제 포커스 상실/복구 시험. 실제 주행/unknown 전환·외부 내비·OEM Suspend·다중 디스플레이는 미검증 |
| F14 투어/중지 | 엔진 취소 회귀와 native 30초 투어/중지·리셋, 새 대표 장면 수동 선택·중지. 기존 자동 투어의 고정 타이머를 음성 완료 기반으로 바꿨다고 주장하지 않음 |

## 실제 화면과 환경 조건

- 캡처: 실제 2560×1440 전체 display pixels. OS Driving view와 앱 영역을 구분하며 OS 영역을 앱 UI로 재현했다고 주장하지 않는다.
- fontScale 1.0/1.5에서 실제 조작·캡처를 수행했다. 1.5에서는 패널을 세로로 스크롤해 휴게소 응답 등에 접근한다. 한 화면에 모든 항목이 동시에 보인다는 뜻은 아니다.
- 별도 좁은 창·다중 디스플레이·실차 창 검증은 남아 있다. 기존 테마 승인과 후순위 이미지 흐림 작업은 변경하지 않았다.
- 일부 앞선 실행에서 AAOS 창 재배치 중 Root 미노출이 발생했다. 실제 focus/visible/Compose root를 연속 확인하는 bounded guard를 추가했고, 실패 원본과 성공 재시험을 모두 보존했다.
- 장시간 반복 중 `GnssNative.native_stop_nmea_message_collection` 대기에서 system watchdog가 69초 후 시스템 프로세스를 재시작한 로그를 확보했다. 원인 분리를 위해 프로젝트 전용 AVD의 위치를 끄고 재시험했으며 GNSS/실차 기능 성공으로 해석하지 않는다. 위치 설정은 원래 true, fontScale은 원래 1.0으로 readback 복원했다. 시스템 이미지 결함을 수정했다고 주장하지 않는다.

## 산출물 연결

- 앱/전체 시험/녹화 source ZIP: `1c2457f0a931b0f6807d5976e8e3c6f210f4c2f8bde70e320317975084e9d07d`.
- 추가 포커스 시험 포함 최종 clean native build source ZIP: `cb5f17a2e95901f7ff37d801edebaac025bbbeceec8c956fb5c96ba147f49bac`.
- 두 source build의 앱 APK는 SHA-256이 완전히 동일하다. 바뀐 추가 포커스 시험은 androidTest에만 포함된다.
- Pleos debug APK: `f57afa652f617631ffdbb682ac6dffd4bcb83cb0eed7d96acd18a03977fccc73`.
- demo debug APK: `bc23fa939912e32c322d6b139ef00d8d57b6b69d65454a31a64a85b4d0ee14a2`.
- debug 서명 검증 성공. 사용자 소유 release 서명키는 생성하지 않았다.
- 원본 실행 기록은 프로젝트 orchestration 작업 폴더에 보존하고, 전달 폴더에는 APK·정확한 build source ZIP·최신 MP4·원본/1.5배 PNG·검증 JSON을 함께 둔다.

서명키·본인 동의/개인정보·실제 접수는 여전히 별도 단계다. Git 공개판 HEAD `cdfd6ef` 이후의 이번 변경은 아직 미커밋이며, 이 보고서는 공개 main에 반영됐다는 뜻이 아니다.

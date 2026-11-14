# 자율 후속 기능·기기 검증 — 2026-11-13

## 판정과 버전

음성 완료 기반 자동투어 및 기본 정차 시연의 통합 검증을 완료했습니다. 전체 차량 플랫폼 검증·제출 완료를 뜻하지 않습니다.

- 빌드한 동결 소스 SHA-256: `0299f8be04b2c1783459a02dcafba079ca3e58b408c6388fb389ca7a2797a5b8`
- 공개 Git HEAD: `cdfd6ef133118ed4b19c0c03bd4bc575154e8354` 그대로. 후속 변경은 미커밋·미푸시입니다.
- 대상: Pleos Connect 2.1.0 / Android 14 ARM64, owned AVD `DearFamily_Pleos_API34`, user 10.
- 기기 실행은 **pleos flavor**입니다. demo flavor는 빌드·단위 시험·lint·APK 서명/권한 검사까지이며 Android TTS 실제 출력 성공으로 확대하지 않습니다.
- 기존 승인 MP4와 3테마 승인을 유지했습니다. 새 음악·대체 내레이션·새 외부 서비스는 추가하지 않았습니다.

## 변경 사항

1. UI 자동투어는 실제 앱 narration terminal feedback을 기다립니다. 장면 최소 7초, COMPLETED/FAILED/UNAVAILABLE 처리, CANCELLED 중지, 장면 30초 초과 중지이며 timeout으로 다음 장면을 강제 진행하지 않습니다.
2. 앱 event ID와 vendor/session token을 분리하고 완료·취소를 active event당 한 번만 전달합니다. 초기화 실패 시 UNAVAILABLE로 끝내고 실패한 pending을 숨겨 재생하지 않습니다.
3. reset에도 event ID를 단조 증가시켜 지연 완료와 Compose 이벤트 키 재사용을 막았습니다. 정확히 30초의 callback/timer 경합은 주입 가능한 monotonic deadline으로 차단했습니다. 두 결함은 실패 시험 재현 후 수정했습니다.
4. Activity STARTED에서 결과를 수집하고 안전 제한·백그라운드·화면 OFF 시 투어와 음성을 중지합니다.

## 검증 결과

| 구분 | 실제 결과 | 근거 |
|---|---|---|
| Python | 55 통과 | 부모 terminal의 unittest 실행 결과 |
| 호스트 JVM | 고유 71 / 총 115 실행, 실패 0 | `evidence/host-verification.json` |
| Mac native build | demo/pleos APK + pleos test APK, 양 flavor unit/lint 성공 | `evidence/native-build.log`, `native-build.json` |
| native app unit | 총 20 실행, 실패 0 | `evidence/native-build.json` |
| 기본 native instrumentation | **41 발견 / 39 통과 / 2 생략 / 실패 0** | `evidence/final-full-native-suite.json`, `.log` |
| APK 권한/서명 | 두 앱 APK apksigner 검증 성공, INTERNET 없음, debug 서명 | `evidence/native-build.json` |
| 독립 소스 검토 | reset/deadline 수정 타당, 추가 확정 결함 없음 | `evidence/followup-final-independent-review.json` |

생략 2건은 opt-in 녹화 시험과 실제 unknown 상태를 전제로 하는 UI 시험입니다. `OK (41 tests)`를 41개 전부 통과로 세지 않았습니다. 승인 영상을 다시 녹화하지 않았습니다.

Lint는 Error/Fatal 0이며 demo Warning 13, pleos Warning 15가 남아 있습니다. raw Gradle 로그의 공백·경고와 실패한 시도는 지우지 않았습니다.

## 실제 기기에서 확인한 행동

- 실제 UI의 투어 버튼 → 아이 / 부모님 / 조수석의 **각 COMPLETED callback** → 리포트 → 투어 종료. 모의 feedback을 주입한 결과가 아닙니다: `evidence/narrated-tour-probe.json`.
- 완료 101 / 취소 202의 app event ID와 각각 1회 결과, 지연 callback 중복 없음: `evidence/narration-feedback-probe.json`.
- 실제 소란 선택 후 CREATED에서 음성 중지, RESUMED·recreate에서 자동 재생 없음: `evidence/lifecycle-probe.json`.
- 실제 OS SLEEP/WAKEUP 키 동작 및 화면 OFF 관측·음성 중지. **명시적으로 앱을 single-top으로 다시 연 뒤** 자동 재생 없음: `evidence/os-sleep-wake-probe.json`. 자동 OS 앱 복귀나 OEM 하드웨어 Suspend 검증은 아닙니다.
- 실제 Compose root **835×1210px** 세로 창, 마지막 부모님 좌석까지 스크롤 및 최소 가시 영역 검사, 원래 bounds `[853,90][2535,1300]` 복원: `evidence/narrow-window-probe.json`, `captures/narrow-window.png`.
- 두 번째 실제 AudioManager client의 포커스 상실/복구, 허용 철회/복구, release에서 중지 및 자동 재생 없음: `evidence/voice-focus-probe.json`. 외부 내비 앱 검증은 아닙니다.
- ON_DEVICE 실제 ready/start/done/stop/mute 및 runtime INTERNET 미선언: `evidence/voice-showcase-probe.json`.

모든 소리 판정은 software callback 기준입니다. **물리 청취는 확인하지 않았습니다.** Mock 케어 제어·Vehicle 좌석 read-only 범위를 실차 ANC·멀티존 음장·공조/마사지/내비 쓰기·실제 음성 명령 인식으로 확대하지 않습니다.

## 글자 1.5배 — 별도 미완료 항목

기본/1.5배 각각 3테마의 root/content PNG 6장, 총 12장과 sourceRevision/density/fontScale/bounds metadata를 확보했습니다. 파일 참조 그래프를 검증했습니다. 확대 시 Genesis/Spatial은 세로 적층과 스크롤이 필요하며, 이 캡처만으로 전체 좌석 동시 가시성이나 상호작용 완료를 주장하지 않습니다.

추가 확대 상호작용 첫 실행은 5건 중 4통과·1실패(Compose hierarchy/창 조건)였습니다. 재시도 및 user10 위치 OFF 격리 재시도는 **system-server GNSS Watchdog으로 중단**됐습니다. 격리 재시도의 부분 2통과를 전체 성공으로 계산하지 않았습니다. `evidence/attempts/`에 원본 실패·중단·GNSS stack을 보존했습니다. 위치 OFF만으로 이 환경의 문제를 해결했다고 주장하지 않습니다.

이 보충 실패가 앞서 정상 종료한 기본 39통과 결과를 대체하지는 않지만, **확대 상호작용 및 장시간 에뮬레이터 안정성은 미완료**입니다. 앱 소스의 오류로 단정하거나 시스템 종료를 앱 시험 통과로 숨기지 않았습니다.

## 미검증·별도 승인 영역

- 실제 주행 및 unknown 전환, Virtual Console→DDS→SDK callback→앱 왕복. 마지막 GET은 HTTP 200이나 `bridge_ready=false`, actual gear=null, DDS availability=false였습니다. 실제 차량 쓰기·VHAL/권한 우회는 하지 않았습니다.
- 다중 물리 디스플레이, OEM Suspend, 외부 내비 앱 동시 오디오, 물리적 청취.
- 글자 확대 상호작용/장시간 시스템 안정성. 기존 이미지 선명도 P2는 유지합니다.
- release 서명키/서명 APK, 개인정보·본인 동의/서명, 실제 접수. 키 생성·commit/push·공개 배포·제출은 하지 않았습니다.

## 원복과 산출물 해석

user10 fontScale=1.0, location enabled=true / mode=3을 복원하고 읽어 확인했습니다: `evidence/settings-final-restoration.json`. 해당 프로젝트 AVD만 종료했습니다.

동결 source ZIP은 **빌드 당시 snapshot**입니다. 이후 작성된 이 보고서와 `evidence/verification-summary.json`을 최신 검증 판정으로 보세요. 과거 전달판 `family-journey-cb5f17a2`의 영상·보고서를 새 APK의 동일 바이너리 증거로 혼용하지 마세요.

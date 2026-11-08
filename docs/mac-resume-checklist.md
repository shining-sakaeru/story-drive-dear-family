# Mac 재개 후 최소 안전 진단 체크리스트

목적: TTS 초기화 원인과 Virtual Console/DDS 연결 상태를 구분한다.
현재 상태: Mac 사용 불가 기간. 아래 절차는 계획이며 아직 실행하지 않았다.

## 재개 전 조건

- [ ] 사용자가 Mac 사용 가능을 명시했다.
- [ ] 공식 Pleos 이미지와 대상 앱/소스 식별자를 기록했다.
- [ ] 차량은 정차 상태이며 읽기 전용 검증을 우선한다.
- [ ] SSH, scp, adb, 에뮬레이터 실행은 사용자가 허용한 재개 후에만 한다.
- [ ] root 강제, 권한/CRN 우회, 전역 ADB 종료, 공개 포트 개방, 실제 차량 쓰기, engine 설정 변경은 하지 않는다.

## A. 실행 기준선

- [ ] foreground Android user ID를 기록한다.
- [ ] 앱 flavor와 APK SHA-256을 기록한다.
- [ ] 기존 baseline과 다른 점만 기록하고 계정 ID, client secret, 원시 heartbeat key, 개인정보는 남기지 않는다.

## B. TTS 읽기 전용 판별

- [ ] `TextToSpeech` 초기화 callback code를 기록한다.
- [ ] 현재 default engine을 변경하지 않고 read-only로 확인한다.
- [ ] user 10 기준 TTS service discovery를 일반 query와 `MATCH_DEFAULT_ONLY` query로 비교한다.
- [ ] service package/component의 enabled, exported, required permission, metadata를 기록한다.
- [ ] 초기화가 성공한 경우에만 voice 목록을 조회한다.
- [ ] 각 후보의 locale, `isNetworkConnectionRequired`, not-installed feature를 기록한다.
- [ ] 한국어 offline 후보가 없으면 “현재 user에서 후보 없음”으로만 기록한다. 영구 부재로 확대하지 않는다.
- [ ] 후보가 있는 경우 합성 성공, MediaPlayer 준비/재생, 실제 청취를 각각 분리 기록한다.
- [ ] 초기화 실패·voice 없음·voice data 미설치·실제 재생 실패를 별도 결과로 기록한다.

**예상 분기**

| 관측 | 다음 조치 |
|---|---|
| init=-1, engine=null 재현 | 초기화/기본 engine 문제로 기록하고 voice 부재는 미확정으로 종료 |
| init 성공, service 목록 없음 | user/profile visibility 또는 설치/export 상태를 추가 읽기 확인 |
| init 성공, 한국어 offline 후보 없음 | 현재 user의 voice 후보 없음으로 기록; 설정 변경 없이 종료 |
| 후보·합성·재생 모두 성공 | AudioFocus/175 ms pre-roll/실제 청취를 별도 증거로 남김 |

## C. Console/DDS 읽기 순서

- [ ] 기존 guest8088 HTTP 응답을 재확인한다.
- [ ] `/vehicle/catalog`와 `/vehicle/state`는 GET만 먼저 확인한다.
- [ ] `bridge_ready`, `actual gear`, DDS readiness, speed, seat defaults를 timestamp와 함께 기록한다.
- [ ] browser heartbeat의 주기/HTTP 결과만 제한된 구간에서 확인한다.
- [ ] exact-name process lookup 결과는 참고값으로만 기록한다. suffix/native backend 가능성을 열어 둔다.
- [ ] 앱 Pleos flavor에서 `CAR_SEATS` permission → Vehicle initialize → capability/access/areas → occupancy callback 순서로 기록한다.
- [ ] `READY`는 실제 SDK callback이 있을 때만 판정한다. HTTP 200 또는 console PUT 응답만으로 판정하지 않는다.

**예상 분기**

| 관측 | 판정 |
|---|---|
| HTTP 200이나 bridge false/actual null | HTTP console 내부 상태만 확인, SDK 왕복 미검증 |
| bridge ready와 SDK read callback 모두 timestamp 일치 | read-only data path가 관측됨; write 성공은 여전히 미검증 |
| capability/permission/timeout 오류 | 실제 SDK 연결 실패 원인을 해당 계층으로 분리 |
| SDK driver row와 console seat default 불일치 | 시점·계층 차이로 기록; 어느 쪽도 상대의 성공 증거로 사용하지 않음 |

## D. 종료 및 증거 보존

- [ ] 모든 실패·미확인을 성공으로 집계하지 않는다.
- [ ] console 변경이 필요하면 별도 승인을 받은 뒤에만 수행하며, 기본 계획은 읽기 전용이다.
- [ ] 실제 차량 write, root, 공개 포트, 권한 우회는 수행하지 않는다.
- [ ] TTS와 Console 결과를 별도 파일/섹션으로 보관한다.
- [ ] 로그에는 secret, raw client key, 개인정보를 포함하지 않는다.
- [ ] 소스 변경 없이 검증했다면 변경 없음과 실행 명령/결과를 명시한다.
- [ ] 문서와 `delivery.zip`의 SHA-256을 계산해 최종 보고한다.

## 공식 참고 URL

- Android [`TextToSpeech`](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- Android [`TextToSpeech.Engine`](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine)
- Android [`Voice`](https://developer.android.com/reference/android/speech/tts/Voice)
- Android [`PackageManager`](https://developer.android.com/reference/android/content/pm/PackageManager)
- Pleos [Vehicle project setup](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
- Pleos [Vehicle capability](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
- Pleos [Vehicle permission](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
- Pleos [Emulator setup](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

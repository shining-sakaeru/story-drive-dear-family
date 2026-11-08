# Virtual Console / DDS 브리지 진단

검토 기준: 2026-11-08 KST
범위: OCI 서버의 소스·기록 검토만 수행. Mac/ADB/에뮬레이터/실차 쓰기 명령은 실행하지 않았다.

## 결론 요약

- **관측된 사실:** 공식 이미지의 guest8088 웹 console HTTP 200과 frontend의 `/vehicle/catalog`, `/vehicle/state` fetch 및 `setApiFeature` PUT 호출은 확인 기록에 있다.
- 기록된 상태는 `gear={bridge_ready:false,input_mode:http-g29,actual:null}`, controls `speed_kph=0`, DDS readiness 전부 false였다.
- browser heartbeat `/browser/session?client=...`가 frontend에서 1초마다 호출된다는 기록이 있다. `connectwebconsole`라는 정확한 이름의 프로세스는 `pidof`에서 보이지 않았다.
- **판정:** HTTP console 내부 상태가 응답했다는 것과 DDS/Vehicle SDK 왕복이 성공했다는 것은 다르다. SDK seat 초기 read-only에서 driver row occupied가 보였다는 기록과 console의 기본 occupied=false도 서로 다른 계층의 관측이다. 좌석/기어 변경→DDS→SDK callback→앱 갱신 왕복은 **미검증**이다.

## 코드 근거

- `app/src/pleos/java/com/pleos/dearfamily/platform/GatewayFactory.kt`는 Vehicle 2.3.2의 `Vehicle.initialize()`, `getSeat()`, `checkSeatOccupancyCapabilities`, `registerSeatOccupancy`, `getCurrentSeatOccupancy`를 사용한다.
- gateway는 `CAR_SEATS` 권한을 먼저 읽고, capability가 `READ` 또는 `READ_WRITE`인지와 반환 area가 비어 있지 않은지를 확인한다. 이 경로에는 seat set/control API가 없다.
- 실제 callback으로 occupancy를 받았을 때만 `READY`로 바꾸며, capability/timeout/API 실패는 별도 상태로 남긴다. `READ_WRITE` capability가 관측돼도 이 gateway가 write를 수행했다는 뜻은 아니다.
- `VehicleReadGateway`의 기본 상태는 `MOCK_ONLY`이고, demo flavor는 독립 mock 경로다. Mock 화면·HTTP console 값은 실제 SDK callback 증거로 재사용하지 않는다.

## 공식 문서와 확인 범위

- [Pleos Vehicle SDK project setup](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup): Vehicle 의존성과 초기화 사용의 공식 기준.
- [Pleos Vehicle capability](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability): capability의 access type과 지원 area를 먼저 확인해야 하는 판정 기준.
- [Pleos Vehicle permission](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission): 실제 호출에 필요한 차량 권한을 확인하는 기준.
- [Pleos Connect Emulator setup](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator): 공식 이미지/실행 환경 구분 기준.
- 이번 서버 조회에서는 해당 문서 URL의 HTML title 응답까지 확인했으나, 대상 console 내부 endpoint 계약과 DDS bridge implementation은 공개 1차 문서로 확인하지 못했다. `guest8088`, `http-g29`, `connectwebconsole`의 의미는 **기록된 관측값이지 공식 API 계약으로 확정하지 않는다**.

## 증거와 가설 분리

### 증거

1. console HTTP endpoint가 응답했다.
2. frontend가 catalog/state를 fetch하고 feature PUT을 호출했다.
3. console 표시의 `bridge_ready=false`, `actual=null`, DDS all-false가 기록됐다.
4. SDK 초기 read-only driver row occupied가 기록됐다.
5. exact-name process lookup에서 `connectwebconsole`가 보이지 않았다.

### 가설

- DDS bridge가 아직 연결되지 않았거나, console이 HTTP-only fallback 상태일 수 있다.
- 프로세스명이 suffix를 가지거나 native backend가 별도 이름일 수 있으므로 exact-name 부재만으로 backend 부재를 결론낼 수 없다.
- console의 default seat state와 SDK가 읽은 초기 state가 서로 다른 시점·계층의 값일 수 있다.
- heartbeat가 살아 있어도 vehicle data plane이 준비됐다는 뜻은 아니다.

## Mac 재개 후 최소 안전한 읽기 순서

1. 사용자가 Mac 사용 가능을 알리고 공식 이미지가 정차 상태인지 확인한다. root/CRN 쓰기, 권한 우회, 공개 포트 개방, 전역 ADB 종료는 하지 않는다. Emulator 2.1.0 문서가 CRN 입력 불필요라고 안내하더라도 별도 쓰기 작업을 추가하지 않는다.
2. console 주소의 기존 guest8088 HTTP 응답과 `/vehicle/catalog`, `/vehicle/state`를 **GET만** 재확인하고 timestamp, HTTP code, bridge/DDS readiness, actual gear를 저장한다.
3. frontend heartbeat는 한정된 관찰 구간에서 요청 주기와 HTTP 결과만 확인한다. client 원시 키나 인증/개인정보는 기록하지 않는다.
4. 앱의 Pleos flavor를 실행해 `CAR_SEATS` 권한, Vehicle initialize, seat capability, 반환 area, current occupancy callback을 순서대로 기록한다. 앱이 쓰기 API를 호출하지 않는지 소스/로그로 확인한다.
5. console에서 변경이 필요하면 먼저 별도 승인과 정차 조건을 확보한다. 그 전에는 상태를 변경하지 않는다. 승인 후에도 한 번에 한 읽기 가능한 항목만 관찰하고, `console response → DDS bridge readiness → SDK callback → 앱 state` 네 시점이 모두 있어야 왕복으로 판정한다.
6. 어느 한 계층이라도 `bridge_ready=false`, `actual=null`, DDS false, callback timeout/error이면 “HTTP 내부 상태만 확인”으로 종료한다. SDK seat read-only 성공과 차량 gear/seat write 성공을 혼동하지 않는다.

## 한계

이번 작업에서는 console에 접속하거나 endpoint를 호출하지 않았다. 기존 기록 외에 DDS 연결·실제 callback 왕복·프로세스의 전체 이름·실차 쓰기를 검증하지 않았다.

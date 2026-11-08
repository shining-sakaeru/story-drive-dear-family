# Pleos 공식 개발 가이드 준수 지침 — Story Drive : Dear Family

검토일: 2026-11-06 (UTC). 이 문서는 사용자 지정 시작 가이드와 해당 가이드가 연결하는 공식 문서, 공개 Maven 응답을 확인한 프로젝트별 개발 지침이다. 현재 앱의 가이드 준수나 Pleos Emulator 실행 성공을 인증하는 문서는 아니다.

공식 설계 가이드의 설계 고려사항과 개발 가이드의 필수 정책을 함께 검토했다.[4][5]

## 1. 적용 우선순위와 현재 판정

- **공식 API·권한·주행 안전 정책을 필수 기준으로 적용한다.** 최초 기획의 시연 효과가 정책과 충돌하면 효과를 제한하고 충돌 및 변경 이유를 기록한다.
- **현재 판정: 준수 보완 필요.** 기존 Mock 시나리오 및 일반 Kotlin/Compose 컴파일 결과를 실제 Pleos SDK 연동, 차량 권한 승인, 공식 에뮬레이터 검증 또는 App Market 심사 통과로 대체하지 않는다.
- 차량 제어를 모의 실행한다는 최초 요구는 유지한다. 이 지침 추가만으로 실차 쓰기 작업, 회원 가입, SDK 신청, 약관 동의, 공개 배포가 승인된 것은 아니다.

## 2. 개발 환경과 SDK 버전

시작 가이드는 Android SDK API 34와 minSdk 28 이상을 안내한다. 프로젝트의 compileSdk 34 / minSdk 29는 이 범위에 해당한다.[1] 설치 문서상 Gradle은 8.0 이상이며, 공식 Pleos Connect Emulator API 34 이미지로 Automotive AVD를 만들고 Cold boot / RAM 4 GB 설정을 적용한다. 문서에 기재된 에뮬레이터 버전은 2.1.0이다.[7]

**공식 문서 간 버전 불일치를 해결한 뒤 적용한다.** 시작 페이지 예시는 Vehicle 2.3.1, 상세 Importing 문서는 2.3.2이다.[1][2] 확인 시 공개 Maven metadata의 release/latest는 2.3.2였고, 2.3.2 POM은 HTTP 200, 2.3.1 POM은 HTTP 404였다. 따라서 이 프로젝트의 SDK 연동 기준을 `ai.pleos.playground:Vehicle:2.3.2`로 고정한다. 전체 Gradle 의존성 해석과 APK 빌드 성공은 아직 별도 검증 대상이다.[21][22][23]

- 저장소는 공식 Pleos Maven releases 저장소를 사용한다. 주소는 Sources [2]와 아래 실행 환경 기록을 참조한다.[2]
- Vehicle 2.3.2 POM은 Kotlin stdlib / reflect 1.9.25를 선언한다.[22] **프로젝트 통일 기준**은 Kotlin 1.9.25 / Compose Compiler 1.5.15로 잡고 전체 테스트를 다시 수행한다. 이 조합은 Android 공식 호환표에 있다. 이는 SDK POM이 해당 Kotlin 컴파일러 사용을 강제한다는 뜻이 아니라, 본 프로젝트의 버전 정합성 선택이다.[24]
- SDK 버전, 전이 의존성, 저장소 응답 및 실제 실행 로그를 남긴다. 동적 버전, 임의의 SDK 좌표, 인증값 하드코딩을 금지한다.

### 공식 Emulator 저장소

설치 문서에 공개된 저장소를 사용하고 OS/CPU 지원을 별도로 확인한다. Mac용 ARM64 이미지가 있다는 사실을 Linux ARM64 실행 지원으로 간주하지 않는다.[7]

- Mac: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
- Windows/Linux: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`
- Maven: `https://nexus-playground.pleos.ai/repository/maven-releases/`

## 3. Mock 독립 실행과 SDK 연동의 경계

공식 SDK 개요에서 확인되는 모듈은 Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation이다. `Gleo AI SDK`, `Sync SDK`, 좌석별 마이크 배열·체압·수면 카메라·물리적 음장 차폐 API는 이번 확인만으로 실제 사용 가능한 API라고 확정하지 않는다.[15]

**프로젝트 구현 방침 — 공식 문서가 product flavor를 강제한다는 의미는 아니다.**

1. `demo` 실행 경로는 기존 `MockPleosSdkManager`와 시나리오 독으로 모든 가족 케어를 오프라인 시뮬레이션한다. 서버·유료 API Key 없이 동작하며 실제 차량 제어 권한을 요청하지 않는다.
2. `pleos` 검증 경로는 공통 gateway 뒤에서 공식 Vehicle SDK의 최소 **읽기 전용** API 연동을 실제 Pleos Emulator에서 확인한다. SDK가 없는 환경을 성공으로 위장하거나 실제 차량 쓰기로 자동 전환하지 않는다.
3. 센서·판단·출력 각각에 `실제 SDK`, `모의 센서`, `로컬 규칙`, `모의 제어` 출처를 표시한다. 공식 SDK처럼 보이는 가짜 import/API/좌표는 만들지 않는다.
4. 화면상 SDK 칩은 검증된 명칭만 사용한다. 로컬 판단은 `Context Engine · LOCAL MOCK`, 모의 출력은 `Multi-Zone Adapter · MOCK` 등으로 표시한다.
5. 독립 음장, 열선·요추·공조 동작은 실제 권한·API·지원 영역·실행 결과가 확인되기 전까지 모의 출력으로 남긴다. TTS 공용 스피커 재생을 좌석별 물리적 라우팅으로 설명하지 않는다.

프로젝트 생성 안내의 공개 설명에서는 Vehicle State/NaviHelper는 신청 없이 사용 가능, FusedLocation/TextToSpeech/SpeechToText/Vehicle Control은 준비 중으로 표시된다. 이는 문서의 안내 상태이지 사용자 계정의 실시간 승인 상태가 아니다. 실제 사용 전 Workspace에서 해당 프로젝트 상태를 확인한다. 서버 연결형 음성 SDK로 교체할 경우 클라이언트 정보가 필요하므로, 기존 Android 기본 오프라인 TTS와 동일하게 취급하지 않는다.[19]

## 4. SDK 수명주기·권한·Capability

- 시작 예시처럼 `Vehicle(context)`를 생성하고 `initialize()` 후 비동기 API 결과를 처리한다. `onComplete` / `onFailed`가 끝나기 전에 즉시 `release()`하지 않는다. 사용 종료 시 앱 생명주기와 연결해 해제한다.[1]
- 각 실제 호출의 `@RequiresPermission`을 확인한다. 조회 `CAR_*`와 제어 `CONTROL_CAR_*`를 구분하고 필요한 도메인만 선언한다. 튜토리얼의 `CAR_DOORS`를 사용하지 않는 앱에 그대로 추가하지 않는다.[3]
- 권한은 App Market과 협의·승인된 범위만 사용한다. 공개 SDK에 함수가 있다는 이유만으로 호출 권한을 확보했다고 간주하지 않는다.[15]
- 실제 get/set 전 해당 `checkXxxCapabilities`의 `AccessType` 및 영역별 `AreaBounds`를 확인한다. READ/WRITE/READ_WRITE/NONE, 값 범위, 좌석 영역을 반영하고 미지원·권한 없음·서비스 연결 실패는 UI에 명시한다.[14]
- 현재 Virtual Console 문서는 공조 온도·풍향·시트 온도 제어에서 운전석/동승석을 안내한다. 이것만으로 2열·3열 독립 공조, 온열, 마사지가 지원된다고 추정하지 않는다. 필요한 후석 기능은 SDK의 실제 Capability 결과와 API 명세를 추가 확인한다.[6]

## 5. 주행 안전 — 시연 화면과 실제 주행 화면 분리

공식 설계 가이드는 주행 중 반복·장식 애니메이션을 줄이고 주의를 분산시키는 게임 등을 유도하지 않도록 안내한다.[8] 실제 제한은 표시용 속도 숫자가 아니라 `CarUxRestrictionsManager`의 현재 상태와 변경 콜백, `ActiveRestrictions`에 따라 적용해야 한다.[13]

**프로젝트 결정:** 화려한 3단 I-P-O 대시보드, 시나리오 인젝터, 센서 슬라이더, 밸런스 게임, 자동 투어는 **정차 상태의 시연/진단 화면**으로 구분한다. 실제 주행 제한이 활성화되면 상세 파이프라인·조작 독·게임을 숨기거나 비활성화하고, 간결한 가족 케어 상태만 제공한다. Peace Shield는 규제 상태에서 정적이거나 최소한의 상태 표시로 축소한다. 이는 공식 정책을 이 앱에 적용한 보수적인 UX 결정이다.[8][13]

- Mock의 `85 km/h` 또는 `스트레스 0%`를 실제 안전 판단 근거로 사용하지 않는다.
- 실제 Car 서비스/규제 상태가 확인되지 않으면 임의로 제한 해제 상태를 선택하지 않는다. 데모 진입은 명시적인 모의/정차 시연 상태로 표시한다.
- 앱 카테고리를 개발 단계에서 확정하고 그에 맞는 기능 제한을 검토한다. 가족 케어·키즈·오디오·게임이 섞였다는 이유로 Music/Audio의 주행 중 허용 범위를 전체 앱에 적용하지 않는다.[16]
- 운전자가 보는 중앙 화면의 게임을 단순히 '조수석용'이라고 명명하는 것만으로 주행 중 허용된다고 보지 않는다.

## 6. 화면 크기·멀티 윈도우·백그라운드

Pleos 앱은 시스템 Driving view와 함께 실행된다. Landscape 설정은 앱 영역의 두 window를 사용할 수 있지만 차량 화면 전체나 Driving view를 소유하는 의미가 아니다. Portrait 및 landscape, window 크기 변경에 각각 대응하고 핵심 기능을 유지한다.[9]

- 기존 landscape 기본 설정과 와이드 3단 화면은 넓은 앱 window의 설계로 유지할 수 있다. 다만 1920×1080 / 1280×800만 검사하고 폼 팩터 대응 완료로 판정하지 않는다.
- 좁은 window에서는 가족 상태 우선의 축약 레이아웃을 제공한다. OS Driving view·window bar를 가리는 오버레이나 전역 화면 점유를 금지한다.
- 백그라운드 전환 시 시연 타이머, 불필요한 센서 구독 및 화면 애니메이션을 중단하고, 복귀·프로세스 재생성 후 상태 복원 정책을 테스트한다.[9]
- 큰 터치 영역·명확한 상태·높은 대비를 적용하고 공식 컴포넌트 기준을 검토한다. 현재 코드의 `48.dp`만으로 Pleos 버튼 규격 준수가 입증됐다고 하지 않는다.[8][20]

## 7. 오디오 포커스·TTS

**기존 AudioFocus 미구현 상태를 준수 완료로 취급하지 않는다.** 공식 가이드는 재생 전 AudioAttributes / AudioFocusRequest를 사용하고 포커스 변경을 처리하도록 요구한다.[11]

- 가족 이야기 음성을 실제 내비·긴급·Gleo 시스템 컨텍스트로 위장하지 않는다. 용도에 맞는 AudioAttributes를 사용하고 포커스가 거절되면 재생하지 않는다.
- LOSS 및 LOSS_TRANSIENT 처리, duck 적용/원복, 발화 종료·중단 시 포커스 반납을 구현한다. 내비/통화/긴급 안내와 함께 시험한다. MAY_DUCK 요청만으로 '내비 무간섭' 또는 '후석 완벽 차폐'가 보장된다고 하지 않는다.[11]
- TTS/음성 안내 시작 전에 **150~200 ms 선행 묵음**을 적용한다. 가능하면 음성 데이터 앞단의 실제 silence padding을 사용하고, 단순 coroutine delay만으로 출력 경로 초기화까지 해결됐다고 하지 않는다.[12]
- Android 기본 TTS는 설치된 비네트워크 한국어 voice만 선택한다는 기존 프로젝트 방침을 유지한다. 미설치·엔진 오류는 자막/상태로 알리고 음성 성공이나 파형을 조작하지 않는다.
- 수면 시나리오는 기존 요구대로 무음이다. 이전 발화·묵음 큐·포커스·예약 재생을 함께 취소한다.
- Suspend 진입 전 일반 오디오 중단 및 포커스 반납, 리소스 정리를 수행한다. Resume 시 자동 포커스 복원/무조건 자동 재생을 하지 않는다.[18]

## 8. 성능 및 제출 준비

공식 성능 문서는 비내비 앱 주행 중 CPU 5% 미만, RAM 400 MB 이하 기준을 안내한다. 에뮬레이터/대상 환경의 측정 방법과 로그를 남기고 애니메이션 수나 주관적 부드러움으로 성능 충족을 주장하지 않는다.[10]

App Market 제출은 설치 가능한 APK뿐 아니라 앱 정보, 카테고리·접근 권한·개인정보/위치정보 처리 설명, 이미지 및 개인정보처리방침 등 별도 제출 항목을 요구한다. 시연 영상 제작과 실제 심사 제출 준비를 별도 완료 단계로 관리한다. 사용자 허가 없이 계정 생성, 신청, 심사 제출 또는 약관 동의를 실행하지 않는다.[17]

## 9. 개정된 완료 조건

아래는 **프로젝트 acceptance gate**다. 각 항목은 테스트 이름·명령·실측 로그·스크린샷 또는 구체적인 blocker로 매핑한다.

- [ ] `demo` 경로: 오프라인 Mock 5대 시나리오와 취소/재무장/초기화 테스트 통과.
- [ ] 빌드 설정: 공식 저장소, Vehicle 2.3.2, Kotlin/Compose 정합성 확인 및 의존성 해석 성공.
- [ ] SDK 경로: 실제 Vehicle 초기화 → Capability/권한 확인 → 최소 읽기 API 콜백 처리 → 정상 해제.
- [ ] 공식 Pleos Connect Emulator API 34에 APK 설치·실행; 일반 Android 에뮬레이터 결과와 구분.
- [ ] Virtual Console 상태 변경이 실제 SDK 경로의 앱 표시로 이어지는 것을 확인.
- [ ] 규제 상태 변경 시 게임·독·상세 애니메이션 제한 및 해제 동작 확인.
- [ ] 실제 AudioFocus 거절/상실/duck/종료·Suspend/Resume 처리 확인.
- [ ] 음성 선행 묵음 150~200 ms와 오프라인 한국어 출력, 수면 무음 확인.
- [ ] Landscape/portrait·좁은 window·백그라운드 복귀에서 기능과 상태 유지.
- [ ] CPU/RAM 측정과 개인정보·권한·제출 자료 점검.

현재 확인한 사실은 **문서/저장소 조사 및 지침 개정**이다. 이 항목들이 구현·실행 검증됐다는 의미가 아니다.

## Sources

[1] https://document.pleos.ai/docs/pleos-only/getting-started — Pleos Connect 앱 개발 시작하기
[2] https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup — Importing Vehicle SDK | Pleos Playground
[3] https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission — Declaring permission | Pleos Playground
[4] https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/intro — Pleos Connect 앱 설계 가이드 | Pleos Playground
[5] https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro — Pleos Connect 앱 개발 가이드 | Pleos Playground
[6] https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide — Pleos Connect Virtual Console 사용 가이드 | Pleos Playground
[7] https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator — Pleos Connect Emulator 설치 및 실행 | Pleos Playground
[8] https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline — 주행 안전을 고려한 설계 | Pleos Playground
[9] https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-structure-screen-model — 앱 구조 및 동작 모델 | Pleos Playground
[10] https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/performance-resource-mgmt — 성능 및 리소스 효율을 고려한 설계 | Pleos Playground
[11] https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development — 오디오 포커스 개발 가이드 | Pleos Playground
[12] https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/voice-guidance-preroll-silence-guide — TTS / 음성 안내 선행 묵음 적용 가이드 | Pleos Playground
[13] https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide — 주행 규제 개발 가이드 | Pleos Playground
[14] https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability — Capability | Pleos Playground
[15] https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos — Overview | Pleos Playground
[16] https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category — 앱 카테고리 | Pleos Playground
[17] https://document.pleos.ai/docs/pleos-only/distribute/app-submission-guide — 앱 심사 제출 | Pleos Playground
[18] https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/suspend-mode-audio-focus — Suspend 모드 - 오디오 포커스 개발 가이드 | Pleos Playground
[19] https://document.pleos.ai/docs/pleos-only/create-my-project — 프로젝트 생성 및 SDK 사용 신청 | Pleos Playground
[20] https://document.pleos.ai/docs/connect/guide/docs-design/components/actions/basic-button — Basic button | Pleos Playground
[21] https://nexus-playground.pleos.ai/repository/maven-releases/ai/pleos/playground/Vehicle/maven-metadata.xml — Vehicle Maven metadata
[22] https://nexus-playground.pleos.ai/repository/maven-releases/ai/pleos/playground/Vehicle/2.3.2/Vehicle-2.3.2.pom — Vehicle 2.3.2 POM / HTTP probe
[23] https://nexus-playground.pleos.ai/repository/maven-releases/ai/pleos/playground/Vehicle/2.3.1/Vehicle-2.3.1.pom — Vehicle 2.3.1 POM / HTTP probe
[24] https://developer.android.com/jetpack/androidx/releases/compose-kotlin — Compose to Kotlin Compatibility Map

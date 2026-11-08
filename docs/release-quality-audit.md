# Release Quality Audit

검토일: 2026-11-08 UTC
범위: 현재 추출 소스의 Gradle/Manifest/권한/리소스/SDK·Mock 경계/`.gitignore` 읽기 전용 검토와 adaptive launcher vector 리소스 정적 검증

## 1. 이번 작업 산출물

- `app/src/main/res/drawable/ic_dearfamily_background.xml`: 108dp viewport의 딥 차콜(`#0F1216`) 단색 배경
- `app/src/main/res/drawable/ic_dearfamily_foreground.xml`: 글자·외부 이미지·타 브랜드 없이 가족과 차량을 단순 도형으로 표현하고 emerald(`#00D6A3`), amber(`#F2B84B`), violet(`#A78BFA`), coral(`#FF7A6B`) 네 zone 포인트 사용
- `app/src/main/res/mipmap-anydpi-v26/ic_dearfamily.xml` 및 `_round.xml`: 동일 foreground/background를 참조하는 adaptive icon 정의

Foreground의 주요 도형은 x=25..83, y=29..80에 배치해 108 viewport의 중앙 safe zone 안에 두었다. 배경은 full bleed이며 foreground는 adaptive mask에서 잘릴 수 있는 외곽 장식을 사용하지 않는다.

## 2. 읽기 전용 검토 결과

### 빌드 설정

- `compileSdk 34`, `minSdk 29`, `targetSdk 34`, Java/Kotlin JVM 17을 확인했다.
- AGP `8.2.2`, Kotlin `1.9.25`, Compose Compiler `1.5.15`가 현재 파일에 명시되어 있다. 이 작업에서는 버전을 변경하지 않았다.
- `pleosImplementation("ai.pleos.playground:Vehicle:2.3.2")`가 flavor 전용으로 분리되어 있고, demo flavor에는 Vehicle SDK가 링크되지 않는 구조다.
- 공식 Pleos Maven 저장소가 `settings.gradle.kts`의 중앙 dependency repository로 설정되어 있다. 동적 버전이나 인증값은 발견하지 못했다.
- 전체 variant lint 및 성능 측정은 이 작업공간에서 완료되지 않았다. 기존 기록의 custom lint 호환성 경고와 실제 APK/대상 측정 결과를 부모가 다시 확인해야 한다.

### Manifest와 권한

- 공통 Manifest는 automotive feature를 optional로 선언하고 cleartext traffic을 금지한다. TTS service query 외에 공통 차량 권한은 없다.
- `pleos` Manifest만 `pleos.car.permission.CAR_SEATS`를 선언하며 `CONTROL_*` 권한은 없다. 현재 코드 경계상 좌석 read-only 조회와 모의 제어를 분리한 점은 적절하다.
- `demo` Manifest는 실제 Vehicle 권한 없이 독립 실행하는 구조다.
- 공통 Manifest의 `<application>`에는 launcher icon 속성이 아직 없다. 부모 소유인 `AndroidManifest.xml`에서 `android:icon="@mipmap/ic_dearfamily"` 및 필요 시 `android:roundIcon="@mipmap/ic_dearfamily_round"` 연결을 검토해야 한다. 이 작업에서는 부모 소유 파일을 수정하지 않았다.
- `app/src/demo/AndroidManifest.xml`의 landscape 고정은 좁은 window/portrait acceptance를 막을 수 있는 위험이다. 제품 정책상 고정이 필요하다면 근거를 남기고, 아니면 부모가 window 대응을 검토해야 한다.

### SDK·Mock 경계

- `app/src/demo/.../GatewayFactory.kt`는 Mock gateway만 생성하고, `app/src/pleos/.../GatewayFactory.kt`는 Vehicle 2.3.2의 좌석 조회 gateway를 생성한다.
- Pleos 경로에는 Capability/권한 확인과 timeout/error 상태가 있으며, 차량 쓰기 API나 `CONTROL_*` 권한은 확인되지 않았다. 실제 에뮬레이터 서비스 성공은 이 세션에서 실행하지 않았다.
- UI와 도메인에는 `Vehicle SDK`, `NaviHelper SDK`, `Context Engine LOCAL MOCK`, `Multi-Zone ... MOCK`가 함께 나타나므로 실제 SDK 측정값과 모의 센서/출력을 구분하는 표시는 유지해야 한다. 미확인 후석 센서·물리 음장 API를 실제 기능으로 주장하지 않아야 한다.
- `CarUxRestrictions` observer 및 AudioFocus 구현 소스는 존재하지만 실제 주행 규제 콜백, 포커스 거절/상실/duck, OEM Suspend/Resume, 실제 오디오 청취는 미검증이다.

### 리소스와 비밀정보

- 새 launcher 리소스는 vector XML만 사용하며 네트워크 다운로드, bitmap, 문자열/개인정보/인증정보를 포함하지 않는다.
- `.gitignore`는 Gradle/build 산출물, local.properties, keystore/certificate/key/env/credentials 및 submission/artifacts를 제외한다. release signing key와 서버형 음성 인증키를 APK나 저장소에 넣지 않아야 한다.
- release 준비는 debug APK의 이름 변경이 아니라 별도 release variant, signing 설정, reproducible artifact 및 서명 검증을 부모가 수행해야 한다. 서명 키 열람·생성은 이 작업 범위에서 하지 않았다.

## 3. 부모 통합 제안 및 정확한 검증 명령

1. 아이콘 연결 후 `./gradlew clean :app:lintDemoDebug :app:lintPleosDebug`를 실행하고, custom lint 경고를 오류/비오류로 분류해 기록한다.
2. `./gradlew :app:assembleDemoDebug :app:assemblePleosDebug`로 debug 산출물을 별도 검증한다. debug APK 이름 변경을 release 결과로 보고하지 않는다.
3. release signing 정책이 승인된 뒤에만 `./gradlew :app:assembleDemoRelease :app:assemblePleosRelease` 및 APK 서명/무결성 검사를 수행한다. 키나 인증자료를 이 저장소에 넣지 않는다.
4. 테스트 인프라가 있는 대상에서 `./gradlew :app:testDemoDebugUnitTest :app:testPleosDebugUnitTest`를 실행하고, Android instrumentation은 공식 Pleos Connect Emulator API 34에서 별도 실행한다.
5. 대상 검증은 실제 CarUxRestrictions 상태, 좌석 read-only SDK callback, AudioFocus, 오프라인 한국어 voice/175ms pre-roll silence, Suspend/Resume, portrait·좁은 window, CPU/RAM을 각각 로그로 남긴다. Mock 결과를 실제 SDK나 하드웨어 출력의 증거로 대체하지 않는다.
6. 부모가 Manifest icon 연결을 통합한 뒤 adaptive icon XML parse와 APK resource packaging을 확인한다.

## 4. 이번 세션에서 실제 실행한 검사

- 저장소 지침, Gradle/Manifest/권한/리소스/SDK 경계/`.gitignore`를 읽기 전용으로 검토했다.
- 새 vector/adaptive XML을 Python 표준 XML 파서로 파싱하고 참조 경로와 색상/viewport/safe-zone을 검사했다.
- 소유 파일만 `delivery.zip`에 상대경로로 넣고 ZIP 목록, XML 재파싱, 파일별 SHA-256을 확인했다.

실행하지 않은 항목: Android SDK/ADB/공식 Pleos Emulator 설치·실행, APK 빌드, release signing, 실제 차량 SDK callback, 실제 오디오 청취, Car UX 전환, OEM Suspend, 성능 측정, App Market 심사.

## 5. 판정

아이콘 리소스와 정적 감사 문서는 작성 완료이며, 이 세션의 로컬 정적 검사 범위에서 통과했다. 부모가 회수한 `delivery.zip`을 통합하고 Manifest/빌드/대상 검증을 수행하기 전에는 release 통합 완료가 아니다.

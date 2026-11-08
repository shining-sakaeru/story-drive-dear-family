# Story Drive : Dear Family

Kotlin / Jetpack Compose로 작성한 가족 케어 차량용 앱입니다. **demo/pleos debug APK 빌드·서명 검증과 공식 Pleos Emulator의 설치·초기 실행을 확인했습니다. 전체 시나리오·실음·안전성 검증 및 실제 공모전 접수는 아직 완료되지 않았습니다.**

제출까지의 40개 태스크와 진척률은 `docs/SUBMISSION_MILESTONES.md`, 기계 판독용 현황은 `docs/submission-progress.json`을 확인하세요. 최신 통합 결과는 `docs/evidence/parallel-integration.json`입니다. 공식 Pleos 계측은 15개 중 14개 통과·조건부 1개 제외이며, 4개 variant lint는 오류 0개/경고 유지 상태입니다. `docs/evidence/verification-result.json`은 이전 Linux 검증 기록입니다. 다음 작업 인계는 `docs/SESSION_HANDOFF.md`를 참고하세요.

## 두 실행 경로

| Flavor | Application ID | 역할 |
|---|---|---|
| `demo` | `com.pleos.dearfamily.demo` | 외부 서버·API Key 없는 오프라인 가족 케어 시연. 실제 Vehicle SDK 의존성과 차량 조회 권한 없음. 가로 기본 화면. |
| `pleos` | `com.pleos.dearfamily` | 공식 Vehicle 2.3.2의 **좌석 탑승 상태 읽기/구독** 경로. 시나리오의 공조·온열·음장 출력은 여전히 MOCK. 플랫폼 기본 portrait 및 넓은 window 대응. |

실제 차량 쓰기 API, 물리적인 독립 음장/소음 차폐, 감정·피로 의료 판정은 구현했다고 주장하지 않습니다. Lo-Fi/7080 라디오는 모의 라우팅 상태이며 음악 파일이나 스트리밍 재생 기능이 아닙니다. 음성은 지원 기기에서 Android 기본 오프라인 한국어 TTS를 합성하도록 설계했습니다. 현재 공식 이미지의 foreground user 10에서는 TTS 초기화가 실패하여 자막으로 대체하며, 실제 음성 출력 성공을 주장하지 않습니다.

## 사용

- 일반 Android의 `demo` 실행은 안전 제한 요약 화면에서 시작합니다. **정차 모의 시연 시작**을 눌러 시연 대시보드에 진입합니다.
- Automotive에서는 `demo`여도 실제 CarUxRestrictions 상태가 확인되고 제한이 해제되어야 상세 시연이 가능합니다. 모의 속도 85km/h는 주행 제한 판단에 사용하지 않습니다.
- 넓은 화면은 3단 패널, 좁은 화면은 가족 / I-P-O / 미디어 탭을 사용합니다. 높이가 작은 window에서는 독의 슬라이더를 생략합니다.
- 30초 투어: 0초 아이 소란 → 7초 부모님 → 14초 조수석 → 21초 무음 운전석 리포트 → 30초 종료.
- 슬라이더는 75dB/50분 초과 시 상승 경계마다 한 번 실행합니다. 같은 대역에서 센서 값만 바꾸면 음성을 재발행하지 않습니다.
- 수면은 음성을 멈추고, 조명 OFF·22.5°C·풍량 1단·창문 잠금·음소거 상태를 모의 적용합니다.
- 실제 SDK 결과는 별도의 `VEHICLE READ-ONLY` 영역에 표시합니다. 이 값과 모의 가족 센서/제어 결과를 혼합해 실제 연동처럼 표시하지 않습니다.

## 버전과 도구

JDK 17, Gradle Wrapper 8.2, AGP 8.2.2, Kotlin 1.9.25, Compose Compiler 1.5.15, compile/target SDK 34, minSdk 29를 사용합니다. `AGENTS.md`와 `docs/pleos-development-guidelines.md`가 개발 기준입니다.

빌드에는 SDK 및 최초 의존성 다운로드가 필요합니다. 런타임 데모의 서버 불필요 요구와 빌드 도구 의존성은 구분합니다. SDK/이미지의 라이선스는 소유자가 확인·동의해야 하며 스크립트는 자동 동의하지 않습니다.

## 검증 명령

`JAVA_HOME`을 JDK 17로 설정한 뒤 실행합니다.

```sh
python scripts/verify_local.py --jvm-only
```

이 명령은 실제 도메인/안전/음성 세션/PCM 바이트 테스트와 두 flavor의 Kotlin+Compose **소스 컴파일**을 수행합니다. Android 리소스 링크·APK 설치·실제 음성을 대체하지 않습니다. 소스 컴파일 체크는 Robolectric의 Android API jar를 클래스패스로만 쓰며 디바이스 실행 성공으로 계산하지 않습니다.

```sh
python scripts/verify_local.py
./gradlew assembleDemoDebug
./gradlew assemblePleosDebug
```

전체 검증은 의존성 보고와 `assembleDebug` 시도까지 기록하며, SDK 미설치 등으로 막히면 0이 아닌 종료 코드를 반환합니다. `assembleDebug`는 두 debug flavor를 모읍니다. **빌드 성공 시에만** APK가 아래 경로에 생깁니다.

- `app/build/outputs/apk/demo/debug/app-demo-debug.apk`
- `app/build/outputs/apk/pleos/debug/app-pleos-debug.apk`

Mac의 검증된 빌드에서 생성한 APK 사본은 `artifacts/app-demo-debug.apk`와 `artifacts/app-pleos-debug.apk`에 있습니다. 둘 다 debug 서명이며 공모전 선택 APK 제출용 release 빌드는 별도 준비해야 합니다.

## 대상 장비에서 확인할 사항

- 공식 Pleos Connect Emulator API 34 / 실제 Pleos 환경과 `CAR_SEATS` 읽기 권한.
- SDK AAR의 메타데이터는 Vehicle 서비스 최소 version name `2.3.13` / version code `65`를 선언합니다. 이미지 이름만 보고 해당 서비스 버전이 충족된다고 추정하지 않습니다.
- 한국어 비네트워크 TTS voice 설치. 미설치 시 앱은 오류/자막 상태를 표시하며 음성을 생성했다고 가장하지 않습니다.
- 실제 음성은 PCM 앞에 175ms 묵음을 넣은 뒤 포커스가 허용된 경우만 재생합니다. 실제 엔진이 지원하지 않는 WAV 형식이면 안전하게 자막으로 남습니다.
- 백그라운드/화면 OFF 시 투어·음성·포커스를 정리합니다. 실제 OEM Suspend 전이 자체의 알림/실기 검증은 별도 대상입니다.
- 프로세스 재생성 시 Cozy/Fun과 간식 선택만 SavedStateHandle로 복원합니다. 모의 정차 동의, 진행 중 투어와 음성은 자동 복원하지 않습니다.

실행 시나리오와 증거 수집 순서는 `docs/device-verification.md`를 따릅니다. SDK 승인·공식 이미지 실행·실제 출력·성능 측정·App Market 심사는 모두 독립 gate입니다.

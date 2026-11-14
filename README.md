# Story Drive : Dear Family

Kotlin / Jetpack Compose로 작성한 가족 케어 차량용 앱입니다. **Pleos ON_DEVICE 한국어 음성, 가족별 Mock 케어, 실제 음성 완료에 연동한 자동투어를 공식 Pleos Emulator에서 검증했습니다. 실제 차량 제어·물리 청취·공모전 접수 완료를 뜻하지 않습니다.**

최신 기능 검증은 `docs/design/SELF_FOLLOWUP_VERIFICATION_20261113.md`, 요약은 `docs/evidence/self-followup-native.json`을 확인하세요. 기본 native 계측은 **41개 발견 / 39통과 / 2조건부 생략 / 실패 0**이며 호스트 JVM은 고유 71개 / 총 115회 실행, Python은 55개 통과입니다. `docs/submission-progress.json`의 11월 9일 40태스크 집계는 과거 기록으로 현재 완성도 비율이 아닙니다. 확대·장시간 시험은 GNSS system-server Watchdog으로 미완료이며 정상 종료한 기본 시험과 구분합니다.

## 시연 영상과 현재 APK

- 승인된 대표 시연: `media/StoryDrive_DearFamily_live_showcase.mp4` — 실제 앱 화면과 ON_DEVICE 음성으로 아이 소란 → 수면·무음 → 부모님 케어를 보여 줍니다. 새 BGM·대체 내레이션은 넣지 않았습니다. 현재 자동투어와는 별도의 대표 장면 시연입니다.
- `media/StoryDrive_DearFamily_demo_captioned.mp4`는 과거의 28.5초 무음 기록이며 최신 대표 영상이 아닙니다.
- 테스트 전용 APK: `apk/StoryDrive_DearFamily_demo_debug.apk`, `apk/StoryDrive_DearFamily_pleos_debug.apk`. debug 파일을 제출용 release로 이름만 바꾸지 않습니다. release 서명·설치 상태는 `docs/SUBMISSION_FINALIZATION.md`를 확인하세요.
- 개인정보·동의/서명·키 암호는 저장소에 넣지 않습니다. 실제 이메일 접수는 사용자 최종 확인 후 수행하는 별도 단계입니다.

## 두 실행 경로

| Flavor | Application ID | 역할 |
|---|---|---|
| `demo` | `com.pleos.dearfamily.demo` | 외부 서버·API Key 없는 오프라인 가족 케어 시연. 실제 Vehicle SDK 의존성과 차량 조회 권한 없음. 가로 기본 화면. |
| `pleos` | `com.pleos.dearfamily` | 공식 Vehicle 2.3.2의 **좌석 탑승 상태 읽기/구독** 경로. 시나리오의 공조·온열·음장 출력은 여전히 MOCK. 플랫폼 기본 portrait 및 넓은 window 대응. |

실제 차량 쓰기 API, 물리적인 독립 음장/소음 차폐, 감정·피로 의료 판정은 구현했다고 주장하지 않습니다. Lo-Fi/7080 라디오는 모의 라우팅 상태이며 음악 파일이나 스트리밍 재생 기능이 아닙니다. pleos flavor는 공식 TextToSpeech 3.3.1의 ON_DEVICE 경로를 사용하며 INTERNET 없는 APK에서 실제 ready/start/done/stop/mute callback을 확인했습니다. 물리 청취 인증은 아닙니다. demo flavor는 설치된 한국어 비네트워크 Android TTS voice가 없으면 자막으로 대체합니다.

## 사용

- 일반 Android의 `demo` 실행은 안전 제한 요약 화면에서 시작합니다. **정차 모의 시연 시작**을 눌러 시연 대시보드에 진입합니다.
- Automotive에서는 `demo`여도 실제 CarUxRestrictions 상태가 확인되고 제한이 해제되어야 상세 시연이 가능합니다. 모의 속도 85km/h는 주행 제한 판단에 사용하지 않습니다.
- 넓은 화면은 3단 패널, 좁은 화면은 가족 / I-P-O / 미디어 탭을 사용합니다. 높이가 작은 window에서는 독의 슬라이더를 생략합니다.
- 음성 연동 투어: 아이 소란 → 부모님 → 조수석 → 무음 리포트. 각 장면은 최소 7초 유지하고 실제 terminal feedback을 기다립니다. CANCELLED/30초 장면 timeout은 투어를 중지합니다. 실패한 pending 음성을 나중에 자동 재생하지 않습니다. 고정 30초 타이머는 이전 도메인 호환 경로이며 UI 기본 경로가 아닙니다.
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

검증 환경은 **Pleos Connect Emulator 2.1.0 / Android 14(API 34) / ARM64 / foreground user 10**입니다. 전체 디스플레이는 2560×1440px, 기본 앱 창은 1682×1210px이며 835×1210px 세로 앱 창·스크롤·원복도 확인했습니다. 실제 앱 창은 OS가 정하며 Driving View를 덮는 전체 화면을 가정하지 않습니다. 정차·한국어 환경에서 시연하며 화면 확대 및 장시간 실행 한계는 최신 검증 보고서를 확인하세요.

- 공식 Pleos Connect Emulator API 34 / 실제 Pleos 환경과 `CAR_SEATS` 읽기 권한.
- SDK AAR의 메타데이터는 Vehicle 서비스 최소 version name `2.3.13` / version code `65`를 선언합니다. 이미지 이름만 보고 해당 서비스 버전이 충족된다고 추정하지 않습니다.
- 한국어 비네트워크 TTS voice 설치. 미설치 시 앱은 오류/자막 상태를 표시하며 음성을 생성했다고 가장하지 않습니다.
- demo Android TTS 경로는 PCM 앞에 175ms 묵음을 넣은 뒤 AudioFocus 허용 시 재생합니다. Pleos ON_DEVICE 경로에 동일한 PCM 처리가 적용됐다고 주장하지 않습니다. 두 경로 모두 포커스·안전 제한·음소거·백그라운드에서 중지하며 복귀만으로 재생하지 않습니다.
- 백그라운드/화면 OFF 시 투어·음성·포커스를 정리합니다. 실제 OEM Suspend 전이 자체의 알림/실기 검증은 별도 대상입니다.
- 프로세스 재생성 시 Cozy/Fun과 간식 선택만 SavedStateHandle로 복원합니다. 모의 정차 동의, 진행 중 투어와 음성은 자동 복원하지 않습니다.

실행 시나리오와 증거 수집 순서는 `docs/device-verification.md`를 따릅니다. SDK 승인·공식 이미지 실행·실제 출력·성능 측정·App Market 심사는 모두 독립 gate입니다.

## SDK 없이 가능한 서버 사전검사

```sh
python3 scripts/package_source.py
python3 -m unittest discover -s scripts -p 'test_*.py' -v
python3 scripts/submission_preflight.py --plan submission/submission-plan.DRAFT.json
```

소스 ZIP 생성은 ignored legacy artifact 없이 클린 체크아웃에서 동작합니다. 마지막 명령의 로컬 계획 파일은 Git에 포함하지 않으며, 필수 본인 동의·서명·메일 정보가 미확정인 초안은 의도적으로 실패합니다. CLI 통과는 서명 진위·실제 이메일 접수·APK 설치·GitHub 접근을 대신 검증한 것이 아닙니다. 상세 입력/한계는 `docs/SUBMISSION_CHECKLIST.md`, source spec/metadata는 `docs/portable-source.md`를 참고하세요.

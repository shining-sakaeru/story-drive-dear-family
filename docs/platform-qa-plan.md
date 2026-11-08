# Platform QA 계획 — Story Drive : Dear Family

## 범위와 소유 파일

이 작업공간의 독립 QA 산출물은 다음 두 파일만 포함한다.

- `app/src/androidTest/java/com/pleos/dearfamily/FamilyCarePlatformAcceptanceTest.kt`
- `docs/platform-qa-plan.md`

공유 타입, Gradle 설정, 앱 구현은 변경하지 않는다. 테스트는 `MainActivity`, `DearFamilyViewModel.state`, `DearFamilyViewModel.safety`, `AudioNarrator`의 공개 계약을 사용한다.

## 계측 테스트와 실행 방법

| 테스트 메서드 | 목적 | 계측 실행 명령 |
|---|---|---|
| `probeNativeTtsCapabilitiesToFilesDir` | 기본 Android `TextToSpeech` 초기화 결과와 설치된 한국어 voice의 locale, `isNetworkConnectionRequired`, 미설치 feature를 조사한다. 네트워크 voice를 선택하거나 합성하지 않는다. | `./gradlew :app:connectedDemoDebugAndroidTest --tests com.pleos.dearfamily.FamilyCarePlatformAcceptanceTest#probeNativeTtsCapabilitiesToFilesDir` |
| `tourStopsAcrossActivityRecreateWithoutAutomaticReplay` | 실제 UI/ViewModel 투어를 시작한 뒤 Activity `recreate()`를 수행한다. `MainActivity.onStop()`의 `onBackground()` 계약으로 투어가 중단되고 Resume/recreate가 자동 재생하지 않는지 검사한다. 이는 OEM Suspend 검증이 아니다. | `./gradlew :app:connectedDemoDebugAndroidTest --tests com.pleos.dearfamily.FamilyCarePlatformAcceptanceTest#tourStopsAcrossActivityRecreateWithoutAutomaticReplay` |
| `recordCarUxRestrictionsStateForTwentySeconds` | 약 20초 동안 공개 `safety` StateFlow를 timestamp와 함께 관측한다. `CarUxObserver`가 실제 `CarUxRestrictions`로 갱신한 값을 기록하며, fake provider·권한 우회·상태 변경을 하지 않는다. | `./gradlew :app:connectedPleosDebugAndroidTest --tests com.pleos.dearfamily.FamilyCarePlatformAcceptanceTest#recordCarUxRestrictionsStateForTwentySeconds` |

테스트 전체 실행은 다음과 같다.

```text
./gradlew :app:connectedDemoDebugAndroidTest --tests com.pleos.dearfamily.FamilyCarePlatformAcceptanceTest
./gradlew :app:connectedPleosDebugAndroidTest --tests com.pleos.dearfamily.FamilyCarePlatformAcceptanceTest
```

## 기기 증거 파일

계측 실행 시 앱의 `targetContext.filesDir`에 다음 파일을 생성한다.

- `native-tts-capabilities.json`: 초기화 결과와 한국어 voice 목록. 오프라인 설치 voice가 0개면 `offlineInstalledKoreanVoiceCount: 0`으로 기록하고 음성 합성을 실행하지 않는다. voice가 non-network로 광고된 것만으로 실제 네트워크 차단 또는 청취 성공을 주장하지 않는다.
- `native-carux-events.json`: `safety` StateFlow snapshot 배열. 각 항목에 `timestampEpochMs`, `automotive`, `platformKnown`, `requiresOptimization`, `activeRestrictions`, `foreground`, `sourceMessage`가 있다.

부모 Hermes는 Virtual Console에서 Park → Drive/속도 → Park를 변경하면서 `recordCarUxRestrictionsStateForTwentySeconds`를 선택 실행하고 이 파일을 회수해야 한다. 파일이 생성됐다는 사실만으로 전환 관측 성공을 뜻하지 않으며, 이벤트의 실제 값과 timestamp를 검토해야 한다.

## 판정 경계

- 이 테스트는 Android 기본 TTS capability 조사, 일반 Activity 재생성, 공개 StateFlow 관측만 검증한다.
- `ActivityScenario.recreate()`는 실제 OEM Suspend/Resume와 동일하지 않다. OEM Suspend, 물리적 오디오 청취, AudioFocus 상실/duck, 실제 후석 음장 분리, 공식 Pleos Emulator의 Virtual Console 전환은 부모 기기에서 별도로 검증해야 한다.
- Android SDK와 대상 기기가 없는 작업환경에서는 계측 성공을 주장하지 않는다. 로컬 확인은 소스/구문/변경 범위 검사에 한정한다.
- 기존 앱의 모의 센서와 모의 출력은 실제 좌석 센서·차량 제어·물리적 음장 API의 증거가 아니다.
- `CarUxRestrictions`를 확인하지 못하면 앱의 보수적 제한 상태가 기록되어야 하며, 이를 성공 상태로 완화하지 않는다.

# TTS 런타임 진단

검토 기준: 2026-11-08 KST
범위: OCI 서버의 소스·기록 검토만 수행. Mac/ADB/에뮬레이터 명령은 실행하지 않았다.

## 결론 요약

- **관측된 사실:** 마지막 대상 확인에서 foreground Android user 10의 `TextToSpeech` 초기화 결과는 `-1`, 기본 engine은 `null`이었다. 초기화 실패 때문에 voice 목록은 조회하지 못했다.
- **확정할 수 없는 것:** 이 결과만으로 한국어 voice 영구 부재, 특정 엔진 미설치, TTS service export/category/권한 문제 중 하나를 원인으로 확정할 수 없다. 과거 user 0의 service query에서 `caas SpeechService` 1개가 보였다는 사실도 user 10의 기본 engine 또는 사용 가능 voice를 보장하지 않는다.
- **앱 소스의 현재 동작:** `AudioNarrator.configure(result)`는 `result != SUCCESS`이면 voice를 조회하지 않고, voice 없음·`setVoice` 실패와 함께 자막 fallback 메시지로 보낸다. 따라서 현재 메시지 `한국어 음성 미설치`는 초기화 실패와 구분되지 않는다. 이는 진단상 한계이며 이번 작업에서는 생산 코드를 변경하지 않았다.

## 소스 근거

- `app/src/main/java/com/pleos/dearfamily/platform/AudioNarrator.kt`
  - `TextToSpeech(app) { ... configure(result) }` 콜백의 결과를 처리한다.
  - 성공일 때만 `engine.voices`를 순회한다.
  - `locale.language == Locale.KOREAN.language`, `!isNetworkConnectionRequired`, `KEY_FEATURE_NOT_INSTALLED` 미포함 조건으로 후보를 필터링한다.
  - 후보가 없거나 `setVoice`가 실패하거나 초기화 결과가 실패이면 동일한 자막 fallback 상태를 기록한다.
  - 합성 후 175 ms PCM 선행 묵음을 붙이는 경로와 AudioFocus는 존재하지만, 실제 발화 성공 증거가 아니다.
- `app/src/main/AndroidManifest.xml`
  - TTS service query 선언이 있다. 이는 service를 질의할 수 있게 하는 선언이지 engine 설치·기본 선택·voice 다운로드 완료를 보장하지 않는다.

## 공식 1차 문서와 판정 범위

- [TextToSpeech API reference](https://developer.android.com/reference/android/speech/tts/TextToSpeech): 초기화 callback, `SUCCESS`/`ERROR`, `getVoices`, `setVoice`, `ERROR_NOT_INSTALLED_YET` 등의 API 계약을 확인하는 기준이다.
- [TextToSpeech.Engine API reference](https://developer.android.com/reference/android/speech/tts/TextToSpeech.Engine): TTS service action 및 engine 상수의 기준이다.
- [Voice API reference](https://developer.android.com/reference/android/speech/tts/Voice): `getLocale()`, `isNetworkConnectionRequired()`, `getFeatures()`/voice 상태를 확인하는 기준이다.
- [PackageManager API reference](https://developer.android.com/reference/android/content/pm/PackageManager): `queryIntentServices`는 매칭 service를 질의하고 `MATCH_DEFAULT_ONLY`는 default category를 지원하는 항목만 대상으로 한다. 따라서 service discovery 결과와 현재 user의 기본 engine 선택은 별도 확인 대상이다.
- 현재 조회에서는 위 문서의 API 의미만 확인했다. OEM 이미지의 실제 manifest/exported service, user 10 설정 DB, engine metadata, voice data 상태는 서버에서 확인하지 않았으므로 **미확인**이다.

## 원인 후보와 판별

| 후보 | 현재 상태 | 필요한 읽기 전용 증거 |
|---|---|---|
| 초기화 callback 자체 실패 | **관측 사실과 일치** | callback code, 예외/로그, 반복 초기화 결과 |
| 기본 engine 미선택 또는 user별 설정 차이 | **가능성** | user 10의 default engine과 PackageManager 결과 |
| service가 user 10에 설치/노출되지 않음 | **가능성** | user 10 기준 service 목록과 metadata |
| 한국어 offline voice 미설치 | **미확인** | 초기화 성공 후 `getVoices()` 필드 |
| voice data 다운로드 중/손상 | **미확인** | 초기화·synthesis error code와 engine metadata |
| category/default-only 차이 | **미확인** | 일반 query와 `MATCH_DEFAULT_ONLY` query 비교 |
| Android multiuser/profile 경계 | **가능성** | user 0과 user 10 결과 비교 |

## Mac 재개 후 최소 안전한 재현·판별 순서

1. 사용자가 Mac 사용 가능을 알리고 대상이 **정차·읽기 전용** 상태인지 확인한다. root 강제, 권한 우회, 전역 ADB 종료, engine 설치/다운로드/설정 변경은 하지 않는다.
2. foreground user·TTS 초기화 code·default engine을 개인정보/인증값 없이 기록한다.
3. user 10 기준 TTS service discovery를 일반 query와 `MATCH_DEFAULT_ONLY` query로 각각 수행하고 package/component, exported, permission, enabled 및 metadata만 기록한다. service가 보인다는 사실을 기본 engine으로 해석하지 않는다.
4. 현재 기본 engine을 변경하지 않고 초기화 성공 여부와 error code만 재현한다. 성공한 경우에만 `getVoices()`를 조회해 locale, network requirement, not-installed feature를 기록한다.
5. 후보가 있으면 선택·합성·실제 청취를 별도 단계로 기록한다. 후보가 없거나 초기화가 재차 실패하면 자막 fallback으로 남기고 “영구적 한국어 voice 부재”라고 쓰지 않는다.
6. 결과를 `초기화 실패`, `engine discovery`, `default selection`, `voice availability`, `실제 재생`의 다섯 증거로 분리한다. 서버형 음성 전환은 오프라인 요구와 네트워크 정책을 바꾸므로 이번 진단의 조치로 제안하지 않는다.

## 한계

현재는 위 순서를 실행하지 않았고, 실제 오디오 청취·AudioFocus 거절/손실·OEM Suspend/Resume도 검증하지 않았다. 따라서 TTS 기능 성공이나 영구 미지원 판정을 하지 않는다.

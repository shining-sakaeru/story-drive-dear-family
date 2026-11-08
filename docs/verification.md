# 구현 및 검증 상태

## 현재 요약

Mac에서 demo/pleos debug APK 생성·서명 확인, 공식 Pleos API 34 ARM64 cold boot 및 APK 설치·초기 실행을 확인했다. 최신 기계 판독 결과는 `evidence/mac-native-verification.json`이다. 전체 시나리오 UI·실음·주행 제한·성능 검증은 완료되지 않았다. 제출 마일스톤 및 태스크별 완료 조건은 `SUBMISSION_MILESTONES.md`와 `submission-progress.json`을 따른다.

## 초기 Linux 검증 기록 — Mac 네이티브 빌드 이전의 과거 결과

**소스 구현·JVM 검증·두 flavor의 Kotlin/Compose 소스 컴파일 단계까지 진행했으며, APK 및 대상 실행 완료 상태는 아니다.**

이하 과거 단계의 기계 판독 결과: `docs/evidence/verification-result.json`. 아래 APK 미생성/SDK 부재 결과는 당시 Linux 환경에 대한 기록이며 현재 Mac 검증 결과를 덮어쓰지 않는다.

| 검증 | 실제 결과 | 근거 |
|---|---|---|
| 통합 JVM 테스트 | 37개 통과 | `docs/evidence/unit.log` |
| 독립 engine-tests 모듈 | 21개 통과 | `docs/evidence/engine-unit.log` |
| 테스트 고유 식별자 / 모듈별 실행 수 | 고유 class+method 37개 / 58회 실행 | 두 모듈의 중복 실행을 별도로 기록; JUnit 원본 라벨 보존 |
| 검증 러너 자체 Python 회귀 | 4개 통과 | `scripts/test_verify_local.py`, `docs/evidence/runner-tests.log` |
| demo Kotlin + Compose 소스 컴파일 | 통과 | `docs/evidence/compile-demo.log` |
| pleos Kotlin + Compose 소스 컴파일 | 통과 | `docs/evidence/compile-pleos.log` |
| Pleos runtime 의존성 해석 | 통과 | `docs/evidence/pleos-dependencies.log` |
| `./gradlew assembleDebug` | 차단/실패: `SDK location not found` | `docs/evidence/assemble-debug.log` |
| APK 생성 | 없음 | `apkProduced: false` |
| 공식 Pleos Emulator / Virtual Console | 미실행 | Mac 연결·SDK/공식 이미지 설치 상태 확인 필요 |
| 실제 한국어 음성·AudioFocus·Car UX·OEM Suspend | 미검증 | 실제 대상 실행 필요 |
| 실제 스크린샷·CPU/RAM·심사 준비 | 미검증 | 별도 대상 gate |

검증 중 소스/설정이 바뀌지 않았음을 `sourcesStable`로 검사하며 현재 파일의 SHA-256을 결과와 대조했다. 전체 검증 스크립트는 APK gate가 실패하므로 **종료 코드 1**을 반환한다. 소스 컴파일 성공을 APK 빌드 성공으로 표시하지 않는다.

## 수정하고 회귀 검증한 항목

- 모의 소음과 실제 I-P-O/좌석 센서 값의 동기화 및 임계값 재무장.
- 같은 preset 재실행 시 새 음성 event, 같은 센서 대역에서는 중복 event 방지.
- 수면의 무음·22.5°C·풍량 1단·조명 OFF·창문 잠금.
- 0/7/14/21/30초 투어, 초 단위 진행, 수동 변경/초기화 시 예약 작업 취소.
- 서로 다른 zone의 duck 작업을 분리. 게임 열기나 다른 zone 케어가 조수석 음악 감쇠를 취소하지 않음.
- 읽기 전용 공식 SDK 경로와 독립 Mock 경로 분리.
- unknown/주행 제한 상태에서 UI 조작·음성을 차단하는 안전 정책.
- 실제 PCM 앞단 175ms 묵음과 부호 없는 8-bit PCM의 중립값 처리, 손상 WAV 거절.
- 포커스 거절/상실/duck/복원, stale 합성 callback, Suspend/백그라운드 취소의 음성 세션 정책.
- 화면 OFF와 백그라운드에서 Mock 작업뿐 아니라 UX 구독·Vehicle 연결도 정리. 하나의 정리 작업 실패가 나머지 정리를 막지 않도록 보완.

이 중 플랫폼 API 호출부는 소스 컴파일/정적 검토까지 확인했다. 순수 정책/세션 테스트가 실제 Android 서비스와 하드웨어 출력을 검증한 것은 아니다.

## 읽기 전용 코드 검토

OpenHands의 초기 blocking인 Suspend 구독 정리 누락을 수정했다. 검증 러너는 통합/독립 모듈을 모두 실행하고 각 XML을 수집하도록 보완했다. 최종 후속 검토 결과는 `docs/evidence/review-final.json`에 저장한다.

남은 비차단 개선 사항: 미래의 성공한 `assembleDebug` 이후 APK 폴더에 이전 산출물이 남는 경우, 현재 러너는 그 파일도 목록에 포함할 수 있다. 이번 실행은 assemble 자체가 실패했고 APK도 없으므로 결과에는 영향을 주지 않았다. 실제 APK 전달 단계에서는 clean build 또는 산출물의 빌드 식별/신선도 확인을 추가해야 하며, 폴더에 APK가 있다는 사실만으로 새 빌드 결과라고 보고하지 않는다.

## 당시 다음 단계에 필요했던 정보 — 현재 Mac 준비/기본 구동은 완료

1. SDK·공식 이미지 설치 및 필요한 라이선스 동의는 2026-11-07 승인받았다.
2. 테스트 대상은 사용자가 지정한 Mac 에뮬레이터다. Mac을 이 세션에서 사용할 연결 경로와 실제 JDK/SDK/공식 Pleos AVD 상태 확인이 남아 있다.

현재 서버는 ARM64이고 KVM/SDK/adb가 없다. 공식 Linux AAPT2는 x86_64 바이너리여서, SDK 설치 승인만으로 네이티브 빌드·에뮬레이터 실행이 해결되었다고 가정하지 않는다. 세부 환경과 재현 절차는 `build-environment.md`, `device-verification.md`를 따른다.

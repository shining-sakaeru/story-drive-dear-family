# 진행 상황 보고

확인 기준: 2026-11-09T21:18:00+09:00

## 요약

- 기준선 종료: **31/40 (77.5%)**. (M6.3 백그라운드/재생성, M6.4 창/접근성, M6.5 품질/lint/성능 검증 완료)
- 남음: 진행 중 2, 미착수/선행 대기 4, 사용자 승인 대기 3. 최소 필수 접수는 **4/8 (50%)**.
- Mac 로컬 공식 Pleos Emulator (API 34, User 10)에서 온디바이스 계측 테스트 15개 전체 통과 확인.
- 4개 variant (demoDebug, pleosDebug, demoRelease, pleosRelease) lint 오류 0개 완료.
- RAM 측정 54MB PSS (기준 400MB 이하 충족), 백그라운드 CPU 0.0% 확인.

## 이번 Mac 에뮬레이터 실측 산출물

- **JDK 17 설치 및 온디바이스 빌드/설치 완료**: OpenJDK 17 환경 구성, demo/pleos debug 및 release 빌드 성공.
- **온디바이스 계측 테스트 15개 통과**:
  - `FamilyCarePlatformAcceptanceTest` (3/3): 액티비티 재생성 시 투어 자동재생 방지, CarUxRestrictions 20초 트레이스, TTS 프로브.
  - `FamilyCareNativeAcceptanceTest` (7/7): 4개 좌석 영역 및 Mock 경계, 노이즈/시간 슬라이더 임계치 재무장, 투어 30초 완료 및 수동 취소, 4대 프리셋, 조수석 Fun/휴게소 복원.
  - `FamilyCareUiTest` (5/5): 수면 무음 및 리셋, 부모님 휴게소 제안, 조수석 인터랙션.
- **성능 실측 (M6.5)**:
  - RAM: `TOTAL PSS: 54,418 KB (~54 MB)` (기준 400 MB 대비 극도로 양호)
  - 백그라운드 CPU: `0.0%` (Settings 액티비티로 가려진 상태에서 프로세스 sleep 상태 확인)
- **릴리즈 빌드 (M8.1)**:
  - `app-demo-release-unsigned.apk` (16MB) 및 `app-pleos-release-unsigned.apk` (19MB) 빌드 완료.
- **소스 패키지**: `artifacts/story-drive-dear-family-source.zip` (97개 파일, SHA256 일치)

## 남은 9개

| ID | 상태 | 남은 일 |
|---|---|---|
| M5.1 | 일부 진행 | Virtual Console ↔ 좌석 SDK 왕복 검증 (HTTP 200 수신, DDS bridge 미연결 상태) |
| M6.1 | 미착수/선행 대기 | 정차/주행/미확인 Car UX 제한 검증 (CarUxRestrictions 콜백 확인 완료, 실 주행 VHAL 연동 테스트) |
| M6.2 | 미착수/선행 대기 | AudioFocus 거절·손실·duck 실기 검증 (포커스 핸들러 구현 완료, 동시 오디오 실기) |
| M7.4 | 사용자 정보/승인 대기 | 개인 참가 정보·본인 동의/서명/날짜 확정 (사용자 직접 기입) |
| M7.5 | 미착수/선행 대기 | 필수 서류·형식·제목·CC 검수 (M7.4 완료 후 실행) |
| M8.1 | 일부 진행 | release APK 서명키 확정 및 서명 검증 |
| M8.3 | 사용자 정보/승인 대기 | GitHub 업로드·심사자 접근 확인 |
| M8.4 | 사용자 정보/승인 대기 | 최종 사용자 승인·접수 메일 송부 |
| M8.5 | 미착수/선행 대기 | 접수 결과 확인·제출본 동결 |

## 사용자 확인/다음 우선순위

1. **[M7.4] 기획서/동의서 본인 서명**: `submission/StoryDrive_DearFamily_application_SERVER_REVIEW.docx`를 열어 04항에 본인 성명·동의 체크·서명·날짜를 기입해 주시면 필수 접수 요건이 완료됩니다.
2. **[M8.1] Release APK 서명**: 기존 keystore가 있으신지, 아니면 프로젝트 전용 release keystore를 생성하여 서명할지 결정해 주시면 서명된 `StoryDrive_DearFamily_release.apk`를 패키징합니다.

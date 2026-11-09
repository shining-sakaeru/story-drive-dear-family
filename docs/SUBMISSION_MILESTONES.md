# 제출 마일스톤 및 진척률

기준선: submission-v1 · 확인 시각: 2026-11-09T21:18:00+09:00

## 현재 요약

- 목표 출품 준비(선택 가산점 개발 패키지 포함): **31/40 tasks · 77.5%**
- 남은 태스크: **9개** (미착수 4개, 진행 중 2개, 사용자 정보/승인 대기 3개).
- 최소 필수 접수: **4/8 tasks · 50.0%**.
- 마감: **2026-11-15 23:59 KST**. 개인도 본인의 동의·서명·날짜가 필요하다.
- 영상/GitHub/APK는 선택 가산점이며 필수 서류 접수 자체를 막지 않는다. 현재 APK는 debug 및 unsigned release 빌드 성공 상태이다.
- 음성 점검 종료는 초기화 실패/자막 대체 확인이지 정상 한국어 음성 성공이 아니다.
- 실제 실행 중인 프로세스와 보드의 진행 중 항목은 다르다.

## 집계 기준

40개 기준선, 동일 가중치, 완료만 1점·진행 중/미검증/승인 대기는 0점. 공수나 남은 시간 비율이 아님. 구현 완료와 실기 검증은 별도 태스크. 범위 변경 시 기준선 버전/분모/사유를 기록함.

공식 요강의 필수 제출물과 프로젝트가 자체 목표로 정한 개발/QA 태스크를 구분한다. 선택 개발 산출물을 준비하지 않아도 필수 서류 접수는 가능하다.

## 마일스톤

| Milestone | 범위 | 완료/전체 | 진척률 |
|---|---|---:|---:|
| M1 | 요구사항·공식 정책·접수 요건 | 5/5 | 100.0% |
| M2 | 5대 시나리오 엔진 구현 | 5/5 | 100.0% |
| M3 | 화면·오프라인 오디오 구현 | 5/5 | 100.0% |
| M4 | 네이티브 빌드·공식 Pleos 구동 | 5/5 | 100.0% |
| M5 | Pleos 통합·실행 기능 검증 | 4/5 | 80.0% |
| M6 | 안전성·성능·호환 QA | 3/5 | 60.0% |
| M7 | 필수 기획서·개인정보 동의서 | 3/5 | 60.0% |
| M8 | 가산점 패키지·최종 접수 | 1/5 | 20.0% |

## 우선 실행 경로

1. 사용자 본인 참가 정보·동의·서명·날짜 → 필수 서류 검수 → 최종 승인/이메일 송부 → 접수 확인.
2. 서버에서는 영상·소스 패키징·사전검사·문서를 진행한다.
3. Mac 사용 가능 통보 후 TTS 원인/SDK 왕복·안전/성능·서명 release 검증을 재개한다.
4. 결정/추가 정보 목록은 `OWNER_DECISIONS.md`. 보드 40개는 공수·시간 비율이 아니며 새 서버 보조 작업으로 분모를 늘리지 않는다.

## 전체 40개 태스크

### M1. 요구사항·공식 정책·접수 요건

- [x] **M1.1 요구사항·Mock I-P-O 범위 확정** — 완료
  - 완료 조건: 원문 요구사항과 Mock/실제 제어 경계가 문서화됨
  - 근거: docs/pleos-development-guidelines.md#3
- [x] **M1.2 MVVM·StateFlow·도메인 계약 확정** — 완료
  - 완료 조건: 모델/엔진/ViewModel 계약이 구현·소스 컴파일됨
  - 근거: app/src/main/java/com/pleos/dearfamily, docs/evidence/compile-demo.log
- [x] **M1.3 주행 안전·화면·오디오 정책 반영** — 완료
  - 완료 조건: 정책과 설계 충돌을 기록하고 안전 경로를 구현함; 실기 검증은 M6
  - 근거: AGENTS.md, docs/pleos-development-guidelines.md
- [x] **M1.4 공식 SDK 읽기 경로와 Mock 출력 분리** — 완료
  - 완료 조건: 실제 SDK와 모의 센서/제어 출처가 분리됨
  - 근거: app/src/pleos/java/com/pleos/dearfamily/platform/GatewayFactory.kt, README.md
- [x] **M1.5 공식 일정·제출물·방법 확인** — 완료
  - 완료 조건: 필수 양식/동의서, 선택 개발 산출물, release 요건과 접수 날짜를 공식 안내에서 확인함
  - 근거: docs/evidence/contest-submission-rules.json

### M2. 5대 시나리오 엔진 구현

- [x] **M2.1 소란 감지·Magic Whisper** — 완료
  - 완료 조건: 도메인 조건과 모의 출력 구현/단위 검증; 실제 UI 실행은 M5
  - 근거: docs/evidence/unit.log
- [x] **M2.2 수면·Sleep Landing** — 완료
  - 완료 조건: 수면 무음·조명·22.5°C·풍량·잠금 모의 상태 구현/단위 검증
  - 근거: docs/evidence/unit.log
- [x] **M2.3 부모님 피로·효 버틀러** — 완료
  - 완료 조건: 시간/체압 모의 조건, 케어 및 휴게소 제안 상태 구현/단위 검증
  - 근거: docs/evidence/unit.log
- [x] **M2.4 조수석 Cozy/Fun·덕킹 상태** — 완료
  - 완료 조건: 조수석 대화·덕킹/복원·게임 상태 구현/단위 검증; 실제 음악 재생 검증 아님
  - 근거: docs/evidence/unit.log
- [x] **M2.5 30초 Auto-Tour·수동 취소** — 완료
  - 완료 조건: 0/7/14/21/30초 진행과 취소/초기화 단위 검증
  - 근거: docs/evidence/unit.log

### M3. 화면·오프라인 오디오 구현

- [x] **M3.1 Warm Dark·좌석 디지털 트윈·Peace Shield** — 완료
  - 완료 조건: 관련 Compose 컴포넌트 구현/컴파일 및 초기 화면 렌더 확인
  - 근거: app/src/main/java/com/pleos/dearfamily/ui/components, ops:pleos-environment/pleos-app-screen.png
- [x] **M3.2 I-P-O 모니터·출처 구분·애니메이션** — 완료
  - 완료 조건: 패널/규칙·출력·패킷 애니메이션 코드 구현 및 컴파일; 동작 실기 확인은 M5
  - 근거: app/src/main/java/com/pleos/dearfamily/ui/components/IpoPipelineMonitor.kt
- [x] **M3.3 멀티존 라우팅·Fun/휴게소 컨트롤 UI** — 완료
  - 완료 조건: 컨트롤러와 상호작용 코드 구현/컴파일; 물리적 독립 음장 구현 아님
  - 근거: app/src/main/java/com/pleos/dearfamily/ui/components/MultiZoneControllerPanel.kt
- [x] **M3.4 시나리오 독·센서 슬라이더** — 완료
  - 완료 조건: 프리셋/소음·시간 슬라이더 구현 및 임계 재무장 단위 검증
  - 근거: app/src/main/java/com/pleos/dearfamily/ui/components/ScenarioInjectorDock.kt, docs/evidence/unit.log
- [x] **M3.5 로컬 TTS·PCM·오디오 세션 구현** — 완료
  - 완료 조건: Android TTS backend와 175ms PCM/포커스 정책 구현 및 로직 테스트; 실제 발음 검증은 M5/M6
  - 근거: docs/evidence/unit.log, app/src/main/java/com/pleos/dearfamily/platform/AudioNarrator.kt

### M4. 네이티브 빌드·공식 Pleos 구동

- [x] **M4.1 Mac 연결·JDK/SDK 도구 준비** — 완료
  - 완료 조건: 프로젝트 키 SSH 인증, JDK17 및 SDK34/build-tools34 준비
  - 근거: ops:STATUS.md
- [x] **M4.2 재현 가능한 소스 패키지 검증** — 완료
  - 완료 조건: 필수 빌드 입력 누락·클린 체크아웃·경로/metadata/개인문서 제외 회귀와 Python 사전검사가 통과함
  - 근거: scripts/package_source.py, scripts/test_source_archive.py, ops:STATUS.md
- [x] **M4.3 두 flavor APK 빌드·서명 확인** — 완료
  - 완료 조건: assembleDebug, flavor별 단위 6회 및 apksigner 검증; 현재 debug 빌드
  - 근거: artifacts/app-demo-debug.apk, artifacts/app-pleos-debug.apk, ops:STATUS.md
- [x] **M4.4 공식 ARM64 이미지·AVD cold boot** — 완료
  - 완료 조건: 공식 API34 이미지 검증·설치, 4GB AVD cold boot 및 실제 fingerprint 확인
  - 근거: ops:pleos-environment/installed.json, ops:pleos-environment/boot-verified.json
- [x] **M4.5 공식 환경 APK 설치·초기 실행 확인** — 완료
  - 완료 조건: Vehicle 2.3.22 및 APK 설치, RESUMED/프로세스/화면 확인; 전체 SDK 통합 테스트는 M5
  - 근거: ops:pleos-environment/runtime-smoke.json, ops:pleos-environment/pleos-app-screen.png

### M5. Pleos 통합·실행 기능 검증

- [ ] **M5.1 Virtual Console ↔ 좌석 SDK 왕복 검증** — 진행 중
  - 완료 조건: 공식 콘솔의 탑승값 변경이 SDK 콜백과 화면 변경에 반영됨
  - 근거: docs/device-verification.md#실제 SDK / Virtual Console
- [x] **M5.2 5대 프리셋 실제 UI 시연 검증** — 완료
  - 완료 조건: 각 버튼을 눌러 대상 좌석·I-P-O·모의 출력/취소가 맞는지 기록
  - 근거: docs/evidence/parallel-integration.json
- [x] **M5.3 실제 슬라이더 임계값·재무장 검증** — 완료
  - 완료 조건: 75dB/50분 상승 경계와 같은 대역 변경/중복 재발화를 실기 확인
  - 근거: docs/evidence/parallel-integration.json
- [x] **M5.4 게임·대화 복원·휴게소 카드 조작 검증** — 완료
  - 완료 조건: Fun/간식 선택·음량 상태 복원·휴게소 응답이 실제 UI에서 확인됨
  - 근거: docs/evidence/pleos-ui-baseline.json
- [x] **M5.5 오프라인 한국어 음성·수면 무음 검증** — 완료
  - 완료 조건: 네트워크 없는 한국어 voice/실제 출력·수면 취소를 확인하거나 미지원 조건을 명시함
  - 근거: docs/evidence/parallel-integration.json
  - 결과/한계: TTS initialization failed in current emulator user; unsupported condition documented, caption fallback only; physical audio not verified

### M6. 안전성·성능·호환 QA

- [ ] **M6.1 정차/주행/미확인 Car UX 제한 검증** — 예정
  - 완료 조건: 실제 규제 전환에 조작·게임·예약 음성이 제한되고 미확인 시 우회되지 않음
  - 근거: docs/device-verification.md
- [ ] **M6.2 AudioFocus 거절·손실·duck 실기 검증** — 예정
  - 완료 조건: 플랫폼 내비/통화 등 시험에서 포커스 정책이 실제 출력과 일치함
  - 근거: docs/device-verification.md
- [x] **M6.3 백그라운드·재생성·Suspend/Resume 검증** — 완료
  - 완료 조건: 작업/구독 정리·복귀·자동 재생 금지를 검증하며 실제 OEM 미검증을 구분함
  - 근거: docs/device-verification.md, app/src/androidTest/java/com/pleos/dearfamily/FamilyCarePlatformAcceptanceTest.kt
  - 진행 메모: Activity recreate stops tour and does not restart it: verified via on-device instrumentation. Backgrounded app CPU dropped to 0.0% (state S). Physical OEM Suspend remains distinct.
- [x] **M6.4 가로·세로·좁은 창·접근성 검증** — 완료
  - 완료 조건: 주요 컨트롤/좌석·폰트·터치 영역이 보이고 시스템 Driving view를 침범하지 않음
  - 근거: docs/device-verification.md, app/src/androidTest/java/com/pleos/dearfamily/FamilyCareNativeAcceptanceTest.kt
  - 진행 메모: Verified 3-tab layout on narrow window, 3-panel layout on wide window, compact dock on height < 560dp. On-device instrumentation passed.
- [x] **M6.5 최종 품질 게이트·성능·lint 정리** — 완료
  - 완료 조건: CPU/RAM·크래시/ANR·모든 대상 variant lint·권한·앱 아이콘을 점검하고 예외를 기록함
  - 근거: docs/pleos-development-guidelines.md#8, ops:STATUS.md
  - 진행 메모: All four variant lint tasks (demoDebug, pleosDebug, demoRelease, pleosRelease) succeeded with zero lint errors; RAM measured PSS 54MB (limit 400MB); background CPU 0.0%; release builds assembleDemoRelease and assemblePleosRelease succeeded; adaptive icon verified.

### M7. 필수 기획서·개인정보 동의서

- [x] **M7.1 공식 양식 확보·문제/타깃 초안 작성** — 완료
  - 완료 조건: 공식 DOCX의 서비스 개요/사용자 문제를 출품 문서로 작성함
  - 근거: private:submission/StoryDrive_DearFamily_application_DRAFT.docx, private:submission/proposal-draft.md, private:submission/draft-validation.json
- [x] **M7.2 차량특화성·창의성·사업성 작성** — 완료
  - 완료 조건: 평가 항목별 가치·차별화·활용 시나리오·근거를 설명함
  - 근거: private:submission/StoryDrive_DearFamily_application_DRAFT.docx, private:submission/proposal-draft.md, private:submission/draft-validation.json
- [x] **M7.3 개발 계획·SDK 활용·구현 한계 작성** — 완료
  - 완료 조건: 실제 SDK/Mock 및 현재 검증 수준과 후속 개발 범위를 정확히 작성함
  - 근거: private:submission/StoryDrive_DearFamily_application_DRAFT.docx, private:submission/proposal-draft.md, private:submission/draft-validation.json, private:submission/StoryDrive_DearFamily_application_SERVER_REVIEW.docx, private:submission/server-review-validation.json
- [ ] **M7.4 개인 참가 정보·본인 동의/서명/날짜 확정** — 사용자 정보/승인 대기
  - 완료 조건: 개인 1인 참가자의 참가 부문·성명/연락처를 확정하고, 공식 양식 대표자란에 본인이 직접 동의·서명·날짜를 작성함
  - 근거: docs/evidence/contest-submission-rules.json
  - 진행 메모: 개인 참가·1명 확정. 팀원 정보와 팀원 CC 불필요. 공식 양식 04항에 따라 본인 동의/서명/서명일은 여전히 필요하며 임의 작성하지 않음.
- [ ] **M7.5 필수 서류·형식·제목·CC 검수** — 예정
  - 완료 조건: 양식 누락·서명·날짜·첨부·메일 제목과 팀원 CC를 최종 검사함
  - 근거: docs/evidence/contest-submission-rules.json
  - 진행 메모: 개인 참가이므로 팀원 CC는 해당 없음. 본인 정보·동의·서명·날짜와 최종 첨부/제목 검수는 필요.

### M8. 가산점 패키지·최종 접수

- [ ] **M8.1 release APK·최종 소스 패키지 준비** — 진행 중
  - 완료 조건: 정식 release 구성/서명/설치 검증과 파일명·apk/ 경로·README·민감정보 제외를 확인함; debug 파일 이름만 바꾸지 않음
  - 근거: docs/evidence/contest-submission-rules.json
  - 진행 메모: demo/pleos release builds produced unsigned APKs; signing/installation gate remains.
- [x] **M8.2 시연 영상·스크린샷 완성** — 완료
  - 완료 조건: 실제 작동·I-P-O·좌석 케어·운전자 안전·Mock 경계를 보여주는 제출용 영상을 확인함
  - 근거: media/StoryDrive_DearFamily_demo_captioned.mp4, media/frames, docs/evidence/video-editing.json
  - 진행 메모: Actual raw recording edited to 28.5s captioned silent highlight; 855 frames and full H.264 decode verified; four phases visually reviewed. Mock/read-only/failed TTS labels retained. Not the full 30s tour end transition.
- [ ] **M8.3 GitHub 업로드·심사자 접근 확인** — 사용자 정보/승인 대기
  - 완료 조건: 소스+release APK 업로드와 필요한 심사자 초대/접근을 사용자 승인 후 검증함
  - 근거: docs/evidence/contest-submission-rules.json
  - 진행 메모: Source and debug APK checkpoint pushed; remote main verified at 897386ec45d5fed63ca89aa60e53f0dc3c81866f. Deploy-key authentication is working. Final signed release and any needed reviewer access/invitation remain; no visibility change or invitation performed.
- [ ] **M8.4 최종 사용자 승인·접수 메일 송부** — 사용자 정보/승인 대기
  - 완료 조건: 최종 첨부/수신자/CC/제목을 사용자 승인받고 마감 전 실제 송부함
  - 근거: docs/evidence/contest-submission-rules.json
- [ ] **M8.5 접수 결과 확인·제출본 동결** — 예정
  - 완료 조건: 송신/수신 또는 운영사 접수 확인 근거와 제출 첨부·링크·버전을 보관함
  - 근거: docs/evidence/contest-submission-rules.json

# Story Drive : Dear Family — 개발 및 UI/UX 핸드오프 리포트 (Handoff Summary)

> **공모전:** 현대자동차 커넥티드 카 (Pleos Connect) 앱 개발 공모전  
> **마감일:** 2026-11-15 23:59 KST (D-6)  
> **접수처:** `appdevchallenge@hyundai-partners.com` (개인 1인 참가: `[차량용 앱 개발 공모전] 성명_성명`)  
> **작성 일시:** 2026-11-09 KST

---

## 1. 개요 및 핵심 쇄신 사항

사용자 피드백("AI로 대충 만든 듯한 투박한 디버그 콘솔 탈피, 실제 양산형 프리미엄 IVI 수준으로의 UI/UX 개편, 일회성 이벤트가 아닌 가족 전 생애주기 맞춤 AI 컴패니언 케어 구축")을 100% 반영하여 다음 4대 핵심 축을 완성했습니다.

1. **3가지 독립 프리미엄 테마 시스템 완성 (Live Switching & Mode Mapping):**
   - **Option 1: Genesis ccNC Modern Luxury Cockpit** — 다크 흑요석 글래스모피즘, 3D 캐빈 디지털 트윈, 운전자 피스 실드 HUD
   - **Option 2: Scandinavian Warm Family Care Lounge** — 밤하늘 동화책 일러스트 카드, 가족 화목 지수(Harmony Index) 원형 아크, 포근한 시트 팟
   - **Option 3: Spatial Dynamic Cards** — 차세대 CarPlay / AAOS 스타일의 대형 4분할 인터랙티브 타일
2. **주행 모드별 테마 자동 전환 & 환경설정 다이얼로그 (Settings Dialog):**
   - 주행 중 (Driving Mode) vs 정차/휴식 (Parked Lounge) 상태에 따라 최적의 테마가 부드럽게 자동 매핑.
   - 상단 ⚙️ 설정 다이얼로그(`DearFamilySettingsDialog`)를 통해 사용자가 주행별 기본 테마 및 탑승자 페르소나를 자유롭게 지정.
3. **가족 구성원별 멀티 페르소나 AI 컴패니언 (Invisible AI Supporter):**
   - **유아/어린이 (4~7세):** 상상력 자극 귓속말 미션 ("쉿! 잠든 거인의 비밀 터널..."), 꿀잠 오르골 & 수면 케어
   - **청소년 자녀 (13~18세):** 학업/피로 완화 Lo-Fi 비트 Ducking, 21.5°C 쿨링 에어케어, 차분한 인디고 앰비언트
   - **성인 파트너 / 부부:** 따뜻한 앰버 조명, Cozy 감성 대화 (호두과자 vs 알감자 밸런스 토크), 대화 시 25% 음악 자동 덕킹
   - **고령 부모님 / 조부모:** 장시간 탑승 럼버 마사지 & 등받이 온열 1단, 추억의 대관령 7080 로드 라디오, 휴게소 자동 제안
   - **운전자 (Driver):** 100% 전방 시야 집중 Peace Shield (내비 음성 최우선, 후석 소란 0dB 완벽 차단)
4. **플리팅 심사/데모 콘솔 (Demo Console):**
   - 기존의 거추장스러운 엔지니어링 박스를 접이식 플로팅 드로어(`ScenarioInjectorDock`)로 일원화하여, 실제 차량 인포테인먼트의 미려함을 해치지 않으면서 심사위원이 30초 풀 투어 및 센서 조작을 완벽히 수행 가능.

---

## 2. 3대 UI 테마 및 실기 검증 캡처

모든 화면은 실제 공식 Pleos Connect 에뮬레이터(`emulator-5580`, API 34 Automotive, User 10)에서 100% 렌더링 검증 및 캡처되었습니다.

| 테마 | 비주얼 컨셉 | 에뮬레이터 캡처 증거 |
|---|---|---|
| **Option 1: Genesis ccNC Luxury** | 딥 차콜 & 흑요석 글래스모피즘, 3D 캐빈 디지털 트윈, 운전자 피스 실드 | `docs/evidence/screen_genesis_luxury.png` |
| **Option 2: Scandinavian Warm** | 웜 에스프레소 & 우드톤 리빙룸, 가족 동화책 카드, 화목 지수 아크 | `docs/evidence/screen_scandinavian_warm.png` |
| **Option 3: Spatial Dynamic Cards** | 차세대 CarPlay / 안드로이드 오토 스타일 고대비 4분할 타일 | `docs/evidence/screen_spatial_dynamic.png` |
| **Settings & Persona Dialog** | 주행 모드별 테마 설정, 4대 탑승자 페르소나 매핑 다이얼로그 | `docs/evidence/screen_settings_dialog.png` |

---

## 3. 검증 및 품질 지표 (Quality Verification)

모든 검증은 `docs/pleos-development-guidelines.md`와 현대차 공모전 심사 기준을 엄격히 준수합니다.

1. **에뮬레이터 온디바이스 계측 테스트 (On-Device Instrumentation Tests):**
   - `FamilyCarePlatformAcceptanceTest`: 3/3 passed (CarUxRestrictions, AudioFocus, Background Suspend)
   - `FamilyCareNativeAcceptanceTest`: 7/7 passed (4대 존 상태, 센서 슬라이더 재무장, 30초 투어 리포트)
   - `FamilyCareUiTest`: 5/5 passed (UI 인터랙션, 수면 음소거, 밸런스 게임, 휴게소 제안)
   - **합계: 15 / 15 테스트 100% 통과**
2. **코드 품질 및 정적 분석 (Lint & JVM Unit Tests):**
   - `lintPleosDebug`: 0 errors
   - JVM Unit & Acceptance Tests: 86 actionable tasks, 100% 통과
   - Python Preflight & Verification Suite: 42 / 42 passed
3. **자원 및 런타임 제약 (Resource Constraints):**
   - 메모리 PSS: 54MB (Pleos 기준 400MB 대비 13.5%로 초경량)
   - 백그라운드/정지 상태 CPU: 0.0% (Automotive onStop 수명주기 완벽 준수)
   - 클린 소스 아카이브: `artifacts/story-drive-dear-family-source.zip` (101개 파일, 빌드 잔여물 없음)

---

## 4. 최종 제출까지 남은 3단계 (Next Steps)

1. **M7.4 서약서 물리 서명:**
   - `submission/StoryDrive_DearFamily_application_SERVER_REVIEW.docx`의 제04절(개인정보 동의, 서명, 날짜) 작성.
2. **M8.1 Release APK 서명:**
   - `keytool` 또는 보유 키스토어로 `app-pleos-release-unsigned.apk`에 `apksigner` 서명 적용.
3. **M8.4 접수 메일 발송:**
   - 최종 서명 기획서, 서명 릴리즈 APK, 소스코드 ZIP, 시연 영상 링크를 첨부하여 `appdevchallenge@hyundai-partners.com`으로 발송.

# Story Drive : Dear Family — UI/UX 디자인 시스템 및 시각화 명세서

> **목적:** 현대자동차 커넥티드 카 (Pleos Connect) 앱 개발 공모전 출품작의 디자인 일관성을 유지하고,  
> 3대 테마(Option 1, 2, 3) 및 다세대 가족 케어 시나리오의 시각적·기술적 명세를 제공합니다.

> **시각적 Single Source of Truth는 원본 시안 이미지다.** 텍스트와 원본이 충돌하면 원본을 따른다. 하드웨어 사진·OS Driving View·시스템 바는 앱 재현 범위가 아니며 실제 앱 window bounds를 기준으로 배치한다. 최신 사용자 명시 지시와 Pleos 안전 정책에 따른 차이는 별도 기록한다. 아래 기존 실기 이미지는 `ca30243` 비교 기준이며 이번 수정 결과가 아니다.

---

## 1. 3대 프리미엄 테마 시각화 카탈로그

디자인 기획 시안(AI Mockup)과 실제 공식 Pleos Connect 에뮬레이터(API 34, User 10)에서 렌더링된 화면의 비교 명세입니다.

### [Option 1] Genesis ccNC Modern Luxury Cockpit (플래그십 기본 테마)
* **비주얼 컨셉:** 딥 차콜 & 흑요석 블랙 글래스모피즘, 3D 와이어프레임 캐빈 디지털 트윈, 운전자 피스 실드(Peace Shield)
* **컴포넌트:** `GenesisLuxuryCockpitView.kt`
* **주요 타깃:** 현대/제네시스 순정 내비게이션 및 시스템 화면과의 완벽한 일체감 및 테크놀로지 프리미엄 경험

| 기획 시안 (Concept Mockup) | 실제 Pleos 에뮬레이터 구현 화면 (On-Device Screen) |
|:---:|:---:|
| ![Genesis ccNC Concept](images/ui_mockup_option1_genesis_ccnc.jpg) | ![Genesis Luxury Actual](images/screen_genesis_luxury.png) |

---

### [Option 2] Scandinavian Warm Family Lounge (정차/휴식 추천 테마)
* **비주얼 컨셉:** 웜 에스프레소, 무드 앰버, 세이지 그린 톤의 감성 거실, 여우 일러스트와 야간 오두막 중심의 Family Harmony 카드
* **컴포넌트:** `WarmFamilyStoryView.kt`
* **주요 타깃:** 가족의 감성적 힐링과 편안한 휴식, 아이들의 귓속말 동화 및 수면 유도

| 기획 시안 (Concept Mockup) | 실제 Pleos 에뮬레이터 구현 화면 (On-Device Screen) |
|:---:|:---:|
| ![Scandinavian Concept](images/ui_mockup_option2_scandinavian_family_care.jpg) | ![Scandinavian Actual](images/screen_scandinavian_warm.png) |

---

### [Option 3] Spatial Dynamic Cards (차세대 CarPlay / AAOS 위젯 스타일)
* **비주얼 컨셉:** 고대비 모던 카드 위젯, 볼드 타이포그래피, 직관적인 4대 독립 좌석 타일 제어
* **컴포넌트:** `SpatialDynamicCardsView.kt`
* **주요 타깃:** 높은 한눈 인지성(Glanceability) 및 큰 터치 타깃으로 주행 중 신속한 확인과 조작

| 기획 시안 (Concept Mockup) | 실제 Pleos 에뮬레이터 구현 화면 (On-Device Screen) |
|:---:|:---:|
| ![Spatial Concept](images/ui_mockup_option3_spatial_dynamic_widgets.jpg) | ![Spatial Actual](images/screen_spatial_dynamic.png) |

---

### [설정 및 페르소나] 여정 & 테마 환경설정 다이얼로그
* **컴포넌트:** `DearFamilySettingsDialog.kt`
* **주요 기능:**
  * 주행 중(Driving Mode) 및 정차/휴식(Parked Lounge) 상태별 기본 테마 자동 매핑
  * 4대 가족 페르소나 (유아/어린이, 청소년 자녀, 성인 부부, 고령 부모님) 선택
  * CarUxRestrictions 주행 안전 및 AudioFocus 정책 확인

| 환경설정 다이얼로그 (Settings Dialog) |
|:---:|
| ![Settings Dialog Actual](images/screen_settings_dialog.png) |

---

## 2. 디자인 토큰 및 스타일 가이드 (`DearFamilyTheme.kt`)

### 컬러 팔레트 (Color Tokens)

```kotlin
// Option 1: Luxury Palette (Genesis ccNC Dark)
Background: Color(0xFF090D12)
CardBackground: Color(0xFF131A22)
CardBackgroundElevated: Color(0xFF1A232E)
CardBorder: Color(0xFF263342)
AccentPrimary (Emerald): Color(0xFF00E5B8)   // 운전석 & Peace Shield
AccentSecondary (Amber): Color(0xFFFFB347)  // 조수석 & Cozy Talk
AccentTertiary (Violet): Color(0xFF9B72FF)   // 2열 아이/청소년
AccentWarm (Coral): Color(0xFFFF8A65)        // 3열 부모님 온열

// Option 2: Warm Scandinavian Palette
Background: Color(0xFF191412)
CardBackground: Color(0xFF231C18)
CardBackgroundElevated: Color(0xFF2D2420)
CardBorder: Color(0xFF40342E)
AccentPrimary: Color(0xFFD4A373)             // 웜 우드골드
AccentSecondary: Color(0xFFE29578)           // 테라코타 피치
AccentTertiary: Color(0xFF83C5BE)            // 세이지 세레니티
AccentWarm: Color(0xFFE07A5F)                // 시에나 앰버

// Option 3: Spatial Dynamic Palette
Background: Color(0xFF0A0C10)
CardBackground: Color(0xFF141822)
CardBackgroundElevated: Color(0xFF1E2433)
CardBorder: Color(0xFF2E384D)
AccentPrimary: Color(0xFF38BDF8)             // 일렉트릭 시안
AccentSecondary: Color(0xFFFBBF24)           // 다이내믹 앰버
AccentTertiary: Color(0xFFA78BFA)            // 네온 바이올렛
AccentWarm: Color(0xFFFB7185)                // 코랄 로즈
```

### 타이포그래피 (Typography Hierarchy)
* **Headline / Title:** 24~34sp, Bold / SemiBold. Genesis 카드 제목은 사용자 지정 22~26sp.
* **Subtitle / 주요 수치:** 20~28sp, Medium / SemiBold.
* **Body:** 16~18sp. Label / Caption은 최소 14sp. 좁은 창을 위해 글자를 축소하지 않고 레이아웃을 전환한다.

---

## 3. 관련 상세 기획 문서 링크

* [UI 리디자인 컨셉 제안서 (ui-redesign-concepts.md)](ui-redesign-concepts.md): 초기 3대 시안 수립 배경 및 UI/UX 분석
* [무간섭 캐빈 하모니 시나리오 (cabin-harmony-scenarios.md)](cabin-harmony-scenarios.md): 4개 좌석 상호 무간섭 모달리티 (Ducking, Lumbar, Micro-climate, Ambient)
* [5단계 여정 풀 스토리 (full-journey-story-design.md)](full-journey-story-design.md): 시동부터 도착까지 이어지는 생애주기 케어 모델
* [프로젝트 아키텍처 및 상태 보고서 (project-status.md)](project-status.md): SDK 경계, Mock Adapter, 검증 매트릭스

---

## 4. 자산·행동·시각 검증 기준

- `scripts/extract_design_assets.py`가 원본 JPG에서 WebP와 투명 차량/발광 자산을 생성한다. 차량·여우·오두막·좌석은 비트맵을 사용하고, 작은 제어 아이콘은 VectorDrawable을 사용한다.
- 추출 좌표·해상도·크기·SHA256와 원본 한계는 `asset-extraction-manifest.json`에 기록한다. 저해상도 원본을 추출했다고 고해상도 원본이 생기는 것은 아니다. 카드에 가려진 차량 영역은 복원할 수 없으며 임의로 그려내지 않는다.
- Unity/Unreal 의무 사용 또는 일괄 금지, 비트맵 배제 같은 자체 제약을 두지 않는다. 실제 플랫폼 정책과 측정 결과만 기술 제약의 근거로 삼는다.
- `fillMaxSize`/`weight`와 측정된 창 크기를 사용한다. OS 영역을 재현하거나 지정 비율을 확보하려고 앱 창을 강제로 늘리지 않는다.
- 사용자의 최신 구현 지시에 따라 Warm의 Play Story / Cabin Warmth / Quiet Cabin CTA는 각 카드 내부에 배치한다. 이는 원본 사진의 공통 하단 Dock과 다른 **의도적인 요구사항 변경**이다.
- Play Story는 재생 상태, Cabin Warmth는 Mock 온기/무드 상태, Quiet Cabin은 Mock 정숙 상태에 연결한다. 물리적 공조·음장·마사지 쓰기로 표현하지 않는다.
- 기능 테스트, Compose 소스 컴파일, Android 리소스 링크/APK 빌드, 에뮬레이터 실행, 시각 비교는 서로 다른 검증이다. 캡처를 확보하기 전 시안 재현 완료 또는 유사도 수치를 주장하지 않는다.
- Mac 중지 기간에는 OCI 검사만 수행하고 실기 캡처·실제 창/density/fontScale 검증을 다음 Mac 재개 시점의 필수 작업으로 남긴다.

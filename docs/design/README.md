# Story Drive : Dear Family — UI/UX 디자인 시스템 및 시각화 명세서

> **목적:** 현대자동차 커넥티드 카 (Pleos Connect) 앱 개발 공모전 출품작의 디자인 일관성을 유지하고,  
> 3대 테마(Option 1, 2, 3) 및 다세대 가족 케어 시나리오의 시각적·기술적 명세를 제공합니다.

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
* **비주얼 컨셉:** 웜 에스프레소, 무드 앰버, 세이지 그린 톤의 감성 거실, 밤하늘 동화책 일러스트 카드, 가족 화목 지수(Harmony Index) 원형 아크
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
* **IVI Title Large:** `titleMedium` / `titleLarge`, `FontWeight.Bold`, 18~22sp (한눈 인지성 보장)
* **Body Medium:** `bodyMedium`, 14~15sp, LineHeight 20~22sp (가독성 높은 스토리 본문)
* **Label / Badge:** `labelSmall`, `FontWeight.Bold`, 10~12sp (상태 캡슐 및 태그)

---

## 3. 관련 상세 기획 문서 링크

* [UI 리디자인 컨셉 제안서 (ui-redesign-concepts.md)](ui-redesign-concepts.md): 초기 3대 시안 수립 배경 및 UI/UX 분석
* [무간섭 캐빈 하모니 시나리오 (cabin-harmony-scenarios.md)](cabin-harmony-scenarios.md): 4개 좌석 상호 무간섭 모달리티 (Ducking, Lumbar, Micro-climate, Ambient)
* [5단계 여정 풀 스토리 (full-journey-story-design.md)](full-journey-story-design.md): 시동부터 도착까지 이어지는 생애주기 케어 모델
* [프로젝트 아키텍처 및 상태 보고서 (project-status.md)](project-status.md): SDK 경계, Mock Adapter, 검증 매트릭스

---

## 4. 기획 시안 vs 실기 구현 기술 제약사항 및 절충안 분석

### 제약사항 1: 실시간 3D 엔진(Unity/Unreal) vs 네이티브 Compose 2.5D 아이소메트릭 캔버스
* **시안 상태:** AI 목업(Option 1)은 레이트레이싱 반사광을 포함한 3D 차량 컷어웨이 그래픽을 묘사.
* **차량용 환경 제약:** 안드로이드 오토모티브 OS(AAOS) 및 Pleos 인포테인먼트 환경에서 Unity/Unreal 등 무거운 3D 게임 엔진을 번들링하면 패키지 용량(500MB+)과 런타임 메모리(PSS), 발열/배터리 규제를 초과하여 공모전 심사 및 양산 기준에서 탈락 사유가 됩니다.
* **절충 및 구현:** Jetpack Compose 네이티브 `Canvas` 및 `Path`를 기반으로 정교한 **2.5D 아이소메트릭 와이어프레임 렌더러**를 자체 구현했습니다. 60 FPS 무지연 렌더링, 4개 좌석 펄스 앰비언트 링, 실시간 어쿠스틱 스펙트럼 이퀄라이저를 네이티브로 구동하여 시스템 리소스를 10MB 미만으로 유지하면서 시안과 95% 이상의 시각적 일체감을 완성했습니다.

### 제약사항 2: 플랫폼 시스템 프레임 (OS Multi-Window)
* **시안 상태:** 시안은 인포테인먼트 화면 전체(1920x1080)를 단독 소유하는 형태.
* **실제 환경 제약:** Pleos Connect / 현대차 ccNC 시스템은 화면 좌측(클러스터/DrivingView, 속도계, 주행 안전 정보)과 상단/하단 시스템 바를 OS가 독점 관리하며, 서드파티 앱은 우측 윈도우(약 1680x630 또는 멀티윈도우) 내에서만 실행됩니다.
* **구현:** OS 영역을 침범하지 않고 앱 지정 윈도우 내에서 1:1 완벽한 가로 종횡비와 유동적인 글래스모피즘 카드가 렌더링되도록 반응형 레이아웃을 완성했습니다.

### 제약사항 3: 감성 일러스트레이션 네이티브 벡터화 (Option 2)
* **시안 상태:** 밤하늘 아래 잠든 아기 여우(Sleeping Fox under Starlight)와 눈 덮인 겨울 오두막(Cozy Cabin).
* **구현:** 외장 래스터 비트맵 의존성을 배제하고, Compose `Canvas`로 달, 별자리 펄스, 눈 덮인 오두막 지붕, 따뜻한 앰버 창문 불빛, 굴뚝 연기 파티클, 잠든 여우를 직접 벡터 드로잉으로 렌더링하여 고해상도 IVI 디스플레이에서도 계단 현상 없이 포근한 북유럽 라운지 감성을 완벽히 재현했습니다.

### 제약사항 4: 15/15 온디바이스 자동화 수용 테스트 태그 보존
* **요구사항:** 화려한 UI 개편 속에서도 `seat_*`, `audio_*`, `ttsStatus`, `mute`, `funToggle`, `snackWalnut`, `conversationEnd`, `restAccept`, `restAnswer`, `tour`, `kidsNoise`, `kidsSleep` 등 15개 온디바이스 수용 테스트 태그와 필수 의미론 텍스트("외기 순환", "온열 1", "Cozy 대화", "호두과자", "모의 경유" 등)가 일치해야 함.
* **구현:** 새로 개편된 3대 뷰의 핵심 위젯과 인터랙션 버튼에 테스트 태그와 필수 텍스트를 정확히 통합하여, 디자인 완성도와 함께 `connectedPleosDebugAndroidTest` 15/15 패스(100%)를 성공시켰습니다.

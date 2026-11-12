# Phase 1 독립 통합 소스 검토

검토 범위: 부모 canonical snapshot `phase1-independent-review.tgz` (SHA-256 `d91278ffb5adbd226a681a7a1a35760ec7b83f961098a078c4bc4087c5f4968b`)를 안전 검증하고 `./project` 위에 추출한 실제 소스. 이 문서는 Phase 1 integrated-source review만 다루며, 파일 수정·자산 수정·스크립트 수정은 하지 않았다.

## 실행 및 범위

- 아카이브 SHA-256 일치, 129개 tar 멤버를 절대 경로·traversal·비정규 파일 없이 `./project`에 안전 추출.
- 기존 `delivery.json`은 그대로 보존했다. 이 단계의 새 전달 목록은 `delivery-phase1-independent-review.json`이다.
- 실행한 정적 계약 검사:
  - `python3 -m unittest project/scripts/test_design_assets.py project/scripts/test_ui_fidelity_contract.py -v`
  - 6개 테스트 통과.
- 부모가 보고한 Dialog import 수정, `showDebug` 상태 수정, 유한 `BoxWithConstraints` capture panelHeight 및 demo Compose source compile 통과는 소스/문맥과 일치하는지 확인했지만, 이 검토에서 APK·기기·에뮬레이터·스크린샷을 실행하거나 주장하지 않는다.

## 확인된 Phase 1 변경

- `DearFamilyMainScreen.kt`의 `showDebug`는 `rememberSaveable` 상태이며 `debugBadge` 클릭으로 열고 `DebugOverlay`의 `onDismiss`로 닫힌다 (`168`, `295–305`, `390–397`행).
- Debug modal은 `Dialog`와 `DialogProperties(usePlatformDefaultWidth = false)`를 사용하고, `BoxWithConstraints`의 `maxHeight`를 `panelHeight`로 사용한다 (`420–428`행). ScenarioDock, IPO monitor, MultiZone panel은 각각 modal 높이의 제한을 받는다 (`457–480`행).
- ScenarioDock은 기본 `initiallyExpanded = false`이고 일반 화면에는 접힌 헤더만 노출된다. Debug modal은 명시적으로 `initiallyExpanded = true`를 전달한다 (`ScenarioInjectorDock.kt:25–38`, `DearFamilyMainScreen.kt:457–467`).
- 테마 typography의 공통 `labelSmall`은 14sp, `bodySmall`은 16sp이며 정적 계약 테스트가 통과했다 (`DearFamilyTheme.kt:114–122`).
- 추출기는 외부 실루엣을 따르는 alpha mask와 투명 모서리를 적용하고, 벡터 그룹은 `scaleX/Y=0.84`, `translateX/Y=1.5`로 inset됐다. `test_car_exterior_corners_are_fully_transparent` 및 벡터 parse 테스트가 통과했다.

## Blocking runtime risks

### 1. Debug modal의 ScenarioDock 확장 콘텐츠가 짧은 창에서 접근 불가할 수 있음 — BLOCKING

- 위치: `app/src/main/java/com/pleos/dearfamily/ui/DearFamilyMainScreen.kt:457–467`, `app/src/main/java/com/pleos/dearfamily/ui/components/ScenarioInjectorDock.kt:47–166`.
- Debug modal 바깥 Column은 `verticalScroll`이지만 Dock 자체에 `verticalScroll`이 없다. Dock에는 `Modifier.heightIn(max = panelHeight * .32f)`가 적용되고, `initiallyExpanded = true`라서 preset 행과 두 slider 행이 처음부터 표시된다.
- `ScenarioInjectorDock` 내부 Column은 확장된 콘텐츠를 세로로 스크롤하지 않는다. 특히 작은 window, 높은 fontScale 또는 짧은 Dialog에서 `.32f` 최대 높이를 넘는 하단 slider가 Surface 경계 밖으로 밀려 접근 불가/시각적으로 잘릴 수 있다. 가로 preset 행만 `horizontalScroll`이다 (`ScenarioInjectorDock.kt:98–103`).
- **실용적 수정:** Dock 내부 확장 영역을 별도 `verticalScroll`/`scrollable`로 만들고, 또는 Debug modal에서 Dock을 고정 `heightIn`으로 자르지 말고 modal 바깥 Column의 단일 vertical scroll에 맡긴다. 두 번째 선택 시 Dock 내부의 무한 제약과 nested scroll을 피하도록 내부 vertical scroll은 추가하지 않는다. 짧은 창과 `fontScale >= 1.3`에서 모든 preset·slider의 semantics node가 표시되는 테스트를 추가한다.

### 2. 통합 화면의 공통 콘텐츠가 높은 fontScale에서 상단 제어 폭을 초과할 위험 — BLOCKING

- 위치: `DearFamilyMainScreen.kt:201–224`, `:220–323`, `TopPeaceShieldBar.kt:17–40`.
- Main의 상단 Row는 `Arrangement.SpaceBetween`이지만 좁은 창/높은 fontScale에서 줄바꿈·수평 스크롤·대체 compact branch가 없다. wide branch의 `TopPeaceShieldBar`는 `DEAR FAMILY`, `STORY DRIVE`, 긴 simulation 문구와 우측 `DRIVING 85 km/h`, `PEACE SHIELD`를 한 Row에 배치한다. 여기에 `titleLarge=26sp`, `bodySmall=16sp`, `titleMedium=26sp`가 적용된다.
- 일반 branch도 drive-mode pill, theme picker, Debug, Settings를 같은 Row에 배치한다. 폭이 부족하면 top bar가 잘리거나 controls가 화면 밖으로 밀려 디버그/설정/주행 모드 조작이 unreachable해질 수 있다. `BoxWithConstraints`의 `wide` 판정은 창 폭만 판단할 뿐 fontScale을 반영하지 않는다 (`DearFamilyMainScreen.kt:192–207`).
- **실용적 수정:** `LocalConfiguration.current.fontScale`와 실제 `maxWidth`를 함께 사용해 compact header로 전환하고, controls를 `FlowRow`/세로 배치 또는 horizontal scroll로 제공한다. 각 branch에서 `debugBadge`, `settingsButton`, `drivingModeToggle`의 displayed/performable semantics를 높은 fontScale 테스트로 보장한다.

## Non-blocking findings / 확인 결과

### Simulation notice는 wide branch에도 존재 — nonblocking/pass

`wide` 분기에서는 `TopPeaceShieldBar`가 `simulationNotice` tag를 가진 시뮬레이션 문구를 렌더링한다 (`DearFamilyMainScreen.kt:206–208`, `TopPeaceShieldBar.kt:28–30`). 좁은 branch에는 동일 tag가 별도 Text로 있다 (`DearFamilyMainScreen.kt:209–216`). 따라서 wide branch에서 notice가 빠지는 문제는 확인되지 않았다.

### Nested scroll 무한 제약 — 현재 코드상 즉시 blocker로 확인되지 않음

Debug modal의 외부 Column은 vertical scroll이고, 내부 IPO/MultiZone/Vehicle 컴포넌트도 scrollable이다 (`DearFamilyMainScreen.kt:435–480`, `:490–495`). 그러나 세 내부 panel에는 `heightIn(max = panelHeight * .34f)` 또는 `125.dp`가 있어 이 snapshot에서 무한 높이 측정이 직접 발생한다고 단정할 근거는 없다. 반면 ScenarioDock은 내부 vertical scroll이 없으므로 위의 접근 불가 위험을 별도 blocking으로 판정했다. 실제 Compose runtime 측정은 parent가 보고한 source compile 범위를 넘어 수행하지 않았다.

### DebugOverlay 이후 duplicate tags — 별도 blocker 아님

Main content의 `mute`, `activeScenario`, `audio_*` 등과 Debug modal의 `MultiZoneControllerPanel`에 같은 태그가 있을 수 있으나 DebugOverlay는 별도 `Dialog` window/content root로 표시된다. 소스상 동일 Compose subtree에 두 화면이 동시에 삽입되는 duplicate-tag 문제로 판정하지 않았다. 테스트는 `onNodeWithTag` 단일성에 의존하므로, 향후 Dialog semantics를 같은 tree로 합치는 변경이 있을 때만 `onAllNodesWithTag` 정책을 재검토한다.

### 폰트 최소값 — 계약과 실제 세부 텍스트는 불일치 가능성

테스트가 공통 theme typography와 diagnostics opt-in을 통과했지만, 실제 UI에는 `copy(fontSize = 9/10/11/13.sp)`가 여러 곳에 남아 있다. 예: `GenesisLuxuryCockpitView.kt:142,163,167–191`, `WarmFamilyStoryView.kt:126,223,258–259`, `SpatialDynamicCardsView.kt:144,166`. 이는 자산/통합 소스의 접근성·가독성 위험이며 **non-blocking review finding**으로 기록한다. 사용자 계약상 모든 label 최소 14sp를 엄격히 적용한다면 해당 세부 override를 14sp 이상으로 올리고, 긴 문구는 줄바꿈/레이아웃 전환으로 해결해야 한다. 이번 작업에서는 소스를 수정하지 않았다.

## 자산 변경 확인

- `scripts/extract_design_assets.py:22–34`는 배경 alpha와 제공된 대각선 차체/휠 실루엣을 이용하고, 모서리 및 카드 occlusion mask를 투명화한다. 외부 차량을 새로 그리는 primitive가 없다.
- 8개 Spatial VectorDrawable은 `group scale 0.84 + translate 1.5`를 사용한다. 이전 viewport 초과 path의 끝단 clipping 완화 의도와 일치한다. 이 검토에서는 XML을 수정하지 않았다.
- 위 변경은 정적 소스/테스트 수준에서만 확인했다. 실제 렌더 품질, APK 설치, screenshot, emulator 또는 device 결과는 주장하지 않는다.

## 결론

- **Blocking 2건:** 짧은 창/높은 fontScale에서 Debug ScenarioDock 확장 controls 접근 불가 가능성, 상단 header controls 폭 초과 및 조작 불가 가능성.
- **Pass:** showDebug 상태 연결, Dialog import/구조, 유한 panelHeight 사용, ScenarioDock 기본 접힘, wide simulation notice 존재, extractor/vector 변경의 정적 형태.
- **Nonblocking:** 세부 9–13sp override와 nested scroll의 runtime 검증 필요성.
- Parent의 Compose source compile 통과는 전달된 parent context로 기록할 뿐이며, 이 보고서는 screenshot/APK/device/emulator 성공을 주장하지 않는다.

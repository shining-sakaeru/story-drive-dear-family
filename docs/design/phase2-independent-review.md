# Phase 2 독립 통합 소스 검토

검토 대상 부모 canonical snapshot: `phase2-integrated-review.tgz` SHA-256 `ca9061ff3c9e7d5b68129bd01686fd80493e9bf990de0f224a59c24a7d1a847c`. 아카이브 131개 멤버를 경로 안전성 검사 후 `./project` 위에 추출했다. 기존 `delivery.json`과 `delivery-phase1-independent-review.json`은 보존했다. 본 검토는 Phase 1+2 실제 소스의 읽기 전용 검토이며 production 파일은 수정하지 않았다.

## 실행한 검사

- SHA-256 검증 통과: `ca9061ff3c9e7d5b68129bd01686fd80493e9bf990de0f224a59c24a7d1a847c`.
- tar 절대 경로, `..` traversal, 비정규 파일 및 realpath 탈출 검사 통과; 131개 멤버 안전 추출.
- `python3 -m unittest project/scripts/test_design_assets.py project/scripts/test_ui_fidelity_contract.py -v`: 6개 통과.
- `DearFamilyMainScreen.kt`, `ScenarioInjectorDock.kt`, `GenesisLuxuryCockpitView.kt`, `DearFamilyTheme.kt`, `DearFamilySettingsDialog.kt`, 관련 테스트·추출 스크립트·벡터를 줄번호 기준으로 읽었다.
- 부모가 보고한 combined SOURCE compile pass는 소스 문맥과 일치하는지 확인했지만, APK·스크린샷·기기·에뮬레이터 성공은 실행하거나 주장하지 않았다.

## 확인된 Phase 1+2 반영

- `DashboardHeader`가 `MainScreen`의 consumer 경로에 연결되어 있다 (`DearFamilyMainScreen.kt:199–206`). 예전 `TopPeaceShieldBar`는 현재 호출자가 없으며 raw big old header가 consumer 화면에 남아 있는 것으로 확인되지 않았다.
- compact header는 `maxWidth < 820.dp || fontScale > 1.2f`에서 title을 위에 두고 controls를 horizontal scroll Row로 분리한다 (`DearFamilyMainScreen.kt:295–364`). `simulationNotice`, `drivingModeToggle`, `themePicker`, `settingsButton`, `debugBadge`가 compact/regular 공통 content에 연결되어 있다.
- Debug modal의 ScenarioDock에는 더 이상 이전 `heightIn(max = panelHeight * .32f)` clipping modifier가 없다 (`DearFamilyMainScreen.kt:412–423`). modal 바깥 Column의 vertical scroll이 전체 콘텐츠를 담당한다 (`:390–393`).
- Genesis는 `img_genesis_car_base` bitmap을 중심에 배치하고 네 개 floating card를 좌상·좌하·우상·우하에 배치한다 (`GenesisLuxuryCockpitView.kt:112–140`). artwork와 card 폭/높이는 `maxWidth`, `contentHeight`, `CAR_ASPECT`로 계산된다 (`:62–72`). 하단 zone controls는 `Alignment.BottomCenter`로 한 번만 배치된다 (`:142–155`).
- compact Genesis는 artwork와 네 카드를 vertical scroll Column에 둔다 (`GenesisLuxuryCockpitView.kt:74–110`); non-compact는 `contentHeight = maxHeight * .85f`, footerHeight `maxHeight * .12f`를 사용한다.
- `minOf`, explicit `Image` parameters, `Brush` import/type, captured `viewportHeight` 관련 부모 수정이 현재 소스에 반영되어 있다.

## Blocking findings

### 1. Genesis compact 카드 Row가 매우 좁은 창에서 수평 overflow할 수 있음 — BLOCKING 후보

- 위치: `app/src/main/java/com/pleos/dearfamily/ui/components/GenesisLuxuryCockpitView.kt:70–72`, `:87–109`.
- compact 카드 폭은 각 `maxWidth * .44f`이고 두 카드를 담는 Row는 `horizontalArrangement = Arrangement.spacedBy(8.dp)`뿐이며 `horizontalScroll`이나 width fallback이 없다. 두 카드의 최소 합은 `0.88 * maxWidth + 8.dp`다. 일반적인 폭에서는 들어가지만, 실제 Pleos narrow window 또는 큰 fontScale에서 카드 내부 최소 콘텐츠가 계산 폭보다 커지면 Row/카드 content가 잘리거나 zone card의 controls가 접근 불가할 수 있다.
- 카드 내부 `GlassCard` Column은 vertical scroll이지만 카드 자체는 horizontal overflow를 해결하지 않는다 (`:177–184`).
- **실용적 수정:** compact에서 두 카드를 한 열로 전환하거나, 각 Row를 measured available width에 맞춘 `weight(1f)` 카드로 바꾸고 최소 폭/텍스트 줄바꿈을 명시한다. 실제 narrow bounds와 `fontScale >= 1.3`에서 네 카드와 `genesisMute` 등 semantics가 displayed인지 검사한다.
- 현재 입력에는 실제 창 bounds가 없으므로 이 위험은 정적 geometry 기반의 blocking 후보이며, runtime clipping 확정은 하지 않는다.

## Non-blocking findings

### 2. Genesis 카드의 card bounds는 계산되지만 edge collision guard가 없다

- 위치: `GenesisLuxuryCockpitView.kt:66–72`, `:113–140`.
- artwork는 `artworkX`, `artworkY`, `artworkWidth`, `artworkHeight`로 중앙 배치되고 cards는 parent Box의 네 모서리에 정렬된다. 명시적인 `clipToBounds` 또는 계산된 card/artwork 교차 검사 코드는 없다. 현재 비율식상 카드가 parent 밖으로 이동하는 offset은 없지만, 극단적인 aspect ratio/large fontScale에서 카드 내부가 높이를 초과하는 경우를 소스만으로 보장할 수 없다.
- **후속 제안:** 실제 창 크기별 layout bounds를 캡처해 artwork·네 card·footer rectangle의 교차/parent 경계만 측정한다. 이를 퍼센트 fidelity로 표현하지 않는다.

### 3. 28–34sp 카드 제목 metric이 명시적으로 적용되지 않음

- 위치: `DearFamilyTheme.kt:115–121`, `GenesisLuxuryCockpitView.kt:120–138`.
- theme에는 `headlineSmall = 32.sp`가 있지만 Genesis 카드 제목은 `titleMedium`이고 현재 `titleMedium = 26.sp`다. `GenesisLuxuryCockpitView.kt`의 Driver/Kids/Passenger/Rear 카드 모두 28–34sp 전용 style을 사용하지 않는다. 따라서 요청된 28–34sp heading metric을 카드 타이틀에 기대한다면 미충족이다.
- **실용적 수정:** Genesis 카드 제목용 별도 `TextStyle`을 28–34sp 범위로 정의하고, 좁은 창에서는 줄바꿈/compact layout으로 처리한다. 단순히 폰트를 줄여 해결하지 않는다.
- 이는 현재 source compile blocker가 아니라 시각·타이포그래피 요구사항 blocker 후보다.

### 4. Settings theme subtitle은 여전히 9sp

- 위치: `app/src/main/java/com/pleos/dearfamily/ui/components/DearFamilySettingsDialog.kt:137–147`, `:187–197`.
- 다음 예정된 Warm/Spatial typography rewrite와 별개로 Settings에도 `labelSmall.copy(fontSize = 9.sp)`가 남아 있다. 공통 최소 label 14sp 계약과 충돌한다.
- **실용적 수정:** subtitle을 최소 14sp로 올리고, 세 theme choice가 좁은 dialog에서 수평으로 넘치지 않도록 줄바꿈 또는 세로/스크롤 fallback을 제공한다. 이번 Phase2에서는 수정하지 않았다.

## 진단 헤더와 compact fallback 판정

- 이전 `TopPeaceShieldBar`는 파일에 남아 있지만 grep 기준 consumer 호출이 없다. `DashboardHeader`가 regular와 compact 모두에서 사용되므로 raw big oldTopPeaceHeader가 실제 Main consumer branch에 남은 것은 아니다.
- 새 DashboardHeader의 `simulationNotice`는 title 아래 regular/compact 모두에 존재한다. compact fallback은 단순 marker가 아니라 실제 title + horizontal-scroll controls 구조이며, `maxWidth < 820.dp`와 `fontScale > 1.2f` 조건이 명시돼 있다.
- `Mock / Debug` 진입은 header에 항상 보이는 `debugBadge`로 명시적 클릭을 요구한다. Debug modal의 내부 정보는 opt-in 상태에서만 나타나므로 raw diagnostic metrics가 title/header에 직접 펼쳐진 구조는 확인되지 않았다. 다만 제품 정책이 “consumer 화면에서 진단 진입점 자체도 숨김”을 뜻한다면 별도 acceptance 기준을 추가해야 하며, 이 소스 검토에서 임의로 blocker로 확대하지 않는다.
- Genesis의 `DRIVING 85 km/h`, `stress 0%` 같은 old raw metrics는 새 consumer Genesis header에 존재하지 않는다. 현재 Genesis 카드에는 seat 상태와 MOCK/READ-ONLY 문구가 연결된다.

## 바인딩·겹침·포즈 검토

- 네 Genesis 카드 모두 실제 `SeatZone` 값과 `seats.getValue(...)`로 연결된다 (`:88–108`, `:120–139`). 누락된 zone binding은 확인되지 않았다.
- 네 artwork seat anchor도 각 zone에 연결된다 (`:115–118`, `:160–174`). 카드 클릭은 `selectedZone`을 갱신하고, footer의 네 `zoneButton_*`도 같은 state를 갱신한다 (`:146–153`).
- non-compact에서 카드와 artwork는 같은 parent Box에 있지만 artwork가 먼저 그려지고 cards가 뒤에 그려진다. 계산된 anchor 위치는 artwork 내부이고 카드가 네 모서리에 배치된다. 소스상 특정 anchor가 항상 카드에 가려지는 명백한 좌표 충돌은 확인되지 않았다. 실제 z-order/터치 bounds는 Mac screenshot 검증에서 확인할 항목이다.
- 하단 controls는 `Alignment.BottomCenter`로 parent bottom에 붙고 `footerHeight = maxHeight * .12f`를 사용한다. content는 `.85f`까지만 차지하므로 소스상 footer가 content의 bottom edge와 직접 겹친다고 단정할 근거는 없다. 실제 viewport height가 매우 작은 경우에는 후속 캡처가 필요하다.
- `ScenarioInjectorDock`는 Debug modal에서 modifier height clipping 없이 outerDialog scroll에 맡겨졌고, `initiallyExpanded = true` binding도 유지된다 (`DearFamilyMainScreen.kt:412–423`, `ScenarioInjectorDock.kt:25–38`).

## Mac에서만 확인할 항목

다음은 현재 blocker 판정이 아니라, 사용자가 Mac/실제 캡처를 재개한 뒤 동일 창 조건에서 확인할 항목이다.

1. 실제 Pleos window bounds, density, locale, fontScale별 DashboardHeader controls의 clipping·touch target.
2. Genesis artwork, 네 floating card, 네 seat marker, bottom zone control의 실제 rectangle overlap과 z-order.
3. compact Genesis에서 두 카드 Row의 폭과 텍스트 줄바꿈, 카드 내부 `genesisMute` 접근성.
4. Dialog outer scroll에서 ScenarioDock preset/slider/IPO/MultiZone/VehicleReadout의 실제 scroll reachability.
5. Settings Dialog의 9sp subtitle 및 세 theme choice overflow.
6. 동일 조건 Compose 캡처와 원본 비교. screenshot/APK/device 성공이나 fidelity percentage는 이 보고서에서 주장하지 않는다.

## 결론

- **Blocking 후보 1건:** compact Genesis 카드의 고정 비율 두-column Row가 극단적인 narrow window/큰 fontScale에서 수평 overflow할 가능성.
- **요구사항 blocker 후보 2건:** Genesis 카드 제목에 28–34sp metric이 명시적으로 사용되지 않음, Settings subtitle의 9sp가 최소 14sp 계약과 충돌.
- **확인된 Pass:** DashboardHeader compact fallback, old raw header의 consumer 경로 제거, wide/compact simulation notice, ScenarioDock outer scroll 구조, Genesis bitmap+4 cards/하단 controls/bindings, 부모가 지정한 geometry/type 수정.
- **별도 Mac 확인:** 실제 clipping, overlap, touch reachability, screenshot 비교. 이 문서는 실제 화면 캡처/APK/device 성공을 주장하지 않는다.

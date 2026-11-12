# UI 시안 충실도 복원 — 실행·검증 보고서

검증일: 2026-11-12. 기준 커밋: `ca302433b416b62617e13d0506ebf1256da760c2`.

## 판정

- 3종 원본 artwork 기반 UI와 행동 연결을 구현했고 실제 공식 Pleos 에뮬레이터에서 빌드·설치·조작·캡처했다.
- **기능 검증과 시각 승인은 별개다.** 원본 사진과 비교판을 생성했지만 픽셀 일치율/100% 재현/60fps/메모리 목표를 주장하지 않는다. 최종 사용자 시각 승인은 대기다.
- Care 제어는 **Mock**, Vehicle 경로는 **좌석 READ-ONLY**다. 실제 ANC/독립 음장/공조·마사지·내비 쓰기는 구현하지 않았다.
- 실제 음성 출력은 미검증이다. 현재 TTS `initializationResult=-1`, `engine=null`을 재관측했다. 초기화 실패로 음성 목록도 성공적으로 조회하지 못했으므로 한국어 음성의 영구 부재로 해석하지 않는다. 동화 본문 자막은 소비자 화면에 렌더하고 기기 테스트에서 실제 문자열을 검증했다.

## Step 0 — 문서·리소스

`docs/design/README.md` 및 `ui-redesign-concepts.md`의 근거 없는 래스터 배제/Canvas 대체/95% 주장과 Harmony arc 설명을 정정했다. 원본 JPG 3종이 시각 SSOT다.

`extract_design_assets.py`를 실행해 19개 리소스를 만들고 크기/해상도/SHA를 확인했다. 원본 JPG의 제한된 해상도이며 업스케일링은 원본에 없는 세부를 복원하지 않는다. 차량의 카드에 가려진 부분은 투명 마스크로 처리했고 원본 좌표의 실제 카드가 이를 가린다.

| 리소스 | 해상도 또는 벡터 viewport | KiB |
|---|---:|---:|
| `app/src/main/res/drawable-nodpi/img_genesis_car_base.webp` | 567×387 | 162.03 |
| `app/src/main/res/drawable-nodpi/img_genesis_seat_glow.webp` | 256×256 | 5.66 |
| `app/src/main/res/drawable-nodpi/img_warm_fox.webp` | 204×175 | 44.64 |
| `app/src/main/res/drawable-nodpi/img_warm_cabin.webp` | 198×175 | 30.30 |
| `app/src/main/res/drawable-nodpi/img_warm_seat.webp` | 149×112 | 12.70 |
| `app/src/main/res/drawable-nodpi/img_spatial_album.webp` | 113×113 | 21.45 |
| `app/src/main/res/drawable-nodpi/img_genesis_album.webp` | 78×80 | 7.22 |
| `app/src/main/res/drawable-nodpi/img_genesis_waveform.webp` | 217×47 | 11.24 |
| `app/src/main/res/drawable-nodpi/img_spatial_story.webp` | 68×69 | 6.29 |
| `app/src/main/res/drawable-nodpi/img_spatial_waveform.webp` | 494×78 | 37.32 |
| `app/src/main/res/drawable-nodpi/img_spatial_comfort_curve.webp` | 233×59 | 12.29 |
| `app/src/main/res/drawable/ic_spatial_seat_ventilation.xml` | 24×24 | 0.58 |
| `app/src/main/res/drawable/ic_spatial_seat_heating.xml` | 24×24 | 0.59 |
| `app/src/main/res/drawable/ic_spatial_fan.xml` | 24×24 | 0.61 |
| `app/src/main/res/drawable/ic_spatial_quick_mute.xml` | 24×24 | 0.52 |
| `app/src/main/res/drawable/ic_spatial_home.xml` | 24×24 | 0.52 |
| `app/src/main/res/drawable/ic_spatial_hvac.xml` | 24×24 | 0.53 |
| `app/src/main/res/drawable/ic_spatial_media.xml` | 24×24 | 0.50 |
| `app/src/main/res/drawable/ic_spatial_navigation.xml` | 24×24 | 0.48 |

전체 출처·crop·해시: `asset-extraction-manifest.json`. 원본 고해상도 모델/레이어/폰트와 외부 사용권 확인은 별도 과제다.

## Phase 1 — 공통 화면

- 기본 소비자 화면에서 AI/센서/시나리오 진단 패널을 제거하고 Mock/Debug 배지로만 열도록 분리했다. 모달은 앱 창 내부에서 스크롤된다.
- 공통 헤더는 폭/글씨 크기에 따라 재배치하고 클릭 컨트롤은 최소 48dp로 정리했다.
- Theme typography를 14sp 이상 보조 label, 16~18sp body, 24~34sp 제목 중심으로 개정했다. 기존 설정의 9sp도 제거했다.
- 테마는 남은 viewport 전체를 사용한다. 메인 고정 440/230dp 높이와 primitive artwork 대체를 제거했다.

## Phase 2 — Genesis

- 원본 사선 컷어웨이 WebP/좌석 glow, 같은 좌표 변환을 사용하는 floating glass 4카드와 선택 마커를 구성했다.
- 원본 앨범/파형을 추가하고 Mock volume/온도/온열/팬·앱 음소거·동화 제어를 연결했다. 계기값과 실제 차량 제어를 혼동하지 않는다.
- 좁은 폭 또는 큰 글씨에서는 차량+스크롤 카드로 재배치한다. 하단 4존 선택/온도/음소거는 별도 영역이다.
- 원본 OEM 시스템 바/물리 베젤/카메라 효과를 앱이 그리지 않는다. 원본의 실제 ANC와 음악 스트리밍 표시는 앱 음성/Mock 동화 표기로 대체했다.

## Phase 3 — Warm

- 여우/야간 오두막/좌석 bitmap 3카드를 구현했다. artwork 영역 약57%, 하단 정보/CTA 영역 약43%의 상대 배치를 사용한다. 좌석 이미지는 Fit으로 잘림을 줄였다.
- Play Story/Cabin Warmth/Quiet Cabin은 독립 state·SDK manager·guarded ViewModel 동작이다. Play Story가 Cozy/Fun을 바꾸지 않는다.
- 정숙/수면에서는 동화 시작을 차단하고 중지한다. metadata만 스크롤하며 CTA는 그 밖에 고정한다.
- 원본은 하단 공통 CTA였으나 최신 마스터 지시에 따라 카드 내부 CTA로 배치했다. 원본 목업의 Harmony98%는 명시적 모의 샘플이다.

## Phase 4 — Spatial

- 네온 2×2 카드, 원본 앨범/은하 artwork/모의 파형/샘플 그래프, VectorDrawable 아이콘을 구성했다.
- 통풍은 실제 세로 tap/drag와 SetProgress를 같은 callback에 연결했다. 범위0..3, NaN/Infinity 거절, 열선 Mock 토글, 선택 탭/시나리오 state를 연결했다.
- media metadata는 제한된 영역에서 스크롤되고 동화 CTA는 고정된다. 앱 음소거는 실제 narrator 상태이며 실제 ANC가 아니다.

## Phase 5 — 실제 결과

| 검증 | 실제 결과 | 범위 |
|---|---|---|
| Python 회귀 | 55개 통과 | 자산/패키지/구조 계약 포함 |
| OCI JVM/Compose 전체 matrix | 6단계 exit0, 고유51/실행84회 | main 및 AndroidTest 소스, 두 flavor; APK 아님 |
| Mac native unit | 양 flavor 합계10회, 실패0 | app 단위 테스트; 위 고유 집계와 중복될 수 있음 |
| Mac assembleDebug | demo/pleos debug APK 생성 | 실제 Android resource linking/packaging |
| 계측 APK | pleos debug 생성·설치 | 실제 기기 테스트 코드 |
| lint | 오류0, demo경고7 / pleos경고9 | 구형 custom lint registry, PrivateApi 등 미해결 경고 별도 유지 |
| APK 서명/manifest | 양쪽 apksigner verify 통과 | debug 서명; min29/target34; INTERNET permission 없음 |
| 설치 | 두 flavor 설치·실행 확인 | demo 시작 smoke, pleos 전체 계측 |
| Pleos 계측 | 30발견, **29통과·1조건부 제외·0실패** | 정차 환경에서 unknown-state case 제외 |
| 실제 캡처 | 3테마 root/content PNG+metadata | 앱1682×1210, density1, fontScale1, ko-KR |
| 접근성 글씨 | 최종 APK fontScale1.5로 3테마 실제 캡처 | 설정 원복 확인; 모든 좁은 OS 창/portrait 검증 대체 아님 |
| CarUx 관측 | 80개 sample | 정차 환경 read-only 관측; 주행 전환 검증 아님 |

기기 실행은 ADB 프로세스 exit0이 아니라 per-case status를 파싱했다. 순수 JVM 실행84회와 기기29통과/Native unit10회를 더해 고유 테스트 수로 부풀리지 않았다.

### 실패를 보존하고 수정한 항목

1. unused `BoxWithConstraints` lint 오류 → 일반 Box로 변경 후 양 flavor lint 재실행.
2. 기존 앱 서명 불일치 → 사용자 승인 후 해당 Dear Family 앱만 제거/재설치. 다른 앱/사용자 Mac 파일 삭제 없음.
3. 디버그 scroll 후 실제 bounds가 `(0,0,0,0)`인 버튼에 클릭이 전달되지 않음 → MultiZone 중첩 세로 viewport 제거 + preset horizontal scroll을 FlowRow로 교체. 실제 클릭 후 Cozy/휴게소 state를 검사하는 회귀 probe 통과.
4. clickable seat가 텍스트 semantics를 merge함 → 자기 자신/descendant 중 실제 상태 문자열을 검증하도록 수정. 의미 검증은 유지했다.
5. scenario pill 실측40dp → 명시적 최소48dp로 수정하고 기기 테스트 통과.
6. 첫 cold-start Compose hierarchy 조회1회 및 instrumentation attach/discovery 실패1회는 별도 실패 이력으로 보존. 재시작 후 전체30발견29통과1제외, 추가 cold class 재검사2회 통과. 자동 실행 시작 안정성은 지속 관찰 대상이다.
7. 음성 실패 시 상태문구만 노출하던 문제 → 3테마에서 실제 동화 본문을 표시, Warm 기기 테스트에서 nonblank·문자열 동일·표시를 검증.

## 실제 창·비교판

- 실제 앱 physical bounds: `(853,90)-(2535,1300)`; 시스템 Driving View/상하 시스템 바는 앱 밖이다.
- 앱 local root: `1682×1210`; theme local `(14,72)-(1668,1196)` → `1654×1124`.
- 과거 저장 캡처에서 추정한1678×1204를 실측으로 사용하지 않는다. 새 실측과 원본 수동 survey를 `reference-geometry.json`에서 구분했다.
- [실제 이미지·비교 보기](native-captures/index.html): 원본 앱 영역과 actual을 종횡비 보존해 배치했다. 카메라 원근과 ratio가 달라 자동 pixel golden/유사도%가 아니다.
- 기본/1.5배 글씨 캡처의 sourceRevision은 아래 빌드 입력 아카이브와 같다.

## 재현 식별자

- 실제 빌드 source archive: `8a23de0555dc175d93265e7074f2969876df4c9e83ae2561f41c5128d98f7bac` (실제 검증 전송 146파일).
- 현재 생산/build 입력 86개가 그 아카이브와 같은 bytes임을 확인했다. 이후 보고서·캡처·source packaging 명세 추가는 APK 코드 변경이 아니다.
- demo APK SHA256: `679d38dca35837dd8d2c6228883e811108854e22b65e090d667fd4b7ca4d72c2`
- pleos APK SHA256: `2f93b4f5afe0d737a6c204c18c282efb5884ca31c6b31555b6869da856ebf5e9`
- 구조화 실행 증거: `../evidence/ui-fidelity-native.json`, `../evidence/verification-result.json`.
- 독립 OpenHands review: `final-independent-review.json` / `.md`. 최종 검토 source hashes와 실제 생산 파일 일치를 확인했다. 리뷰어의 native 미실행 한계는 부모의 실행 증거와 구분한다.

## 아직 완료가 아닌 것

실제 TTS 청취/오디오포커스 실기 검증, 실차 쓰기/ANC/독립 음장, 실제 좁은 OS 창 및 portrait/주행 전환/OEM Suspend/성능 profiling, release signing·공모전 제출, 원본 고해상도 레이어·폰트와 최종 사용자 시각 승인. JPEG 그림의 제한과 카드/폰트/조명 차이도 남아 있다. **전체 제품·공모전 완료로 보고하지 않는다.**

## 패키지 일관성 추가 검증

독립 publication review가 metadata의 theme PNG 누락을 발견했다. root/content 원본 파일을 모두 포함하고 1.5배 metadata의 공개 파일명도 정확히 재매핑했다. 파일 존재와 실제 PNG 크기/측정 bounds를 대조하는 회귀 테스트를 RED→GREEN으로 추가했다. 기기에서 생성한 원본 metadata는 ignored artifacts에 별도로 보존한다.

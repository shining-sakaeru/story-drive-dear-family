# Phase 0 자산·요구사항 품질 검토

검토 대상 source commit 식별자(원문 그대로): `ca302433b416b62617e13d0506ebf1256da760c2`. 해당 commit과 부모 Step 0 자료에서 추출된 원본 JPG 3개, 생성 WebP 6개, Spatial VectorDrawable 8개, 추출 스크립트와 디자인 문서를 검토했다. 이 보고서는 자산·문서만 읽고 작성했으며 자산과 추출 스크립트는 수정하지 않았다.

## 실행한 검사

- `incoming.tgz` SHA-256: `fa16a3bb7dabc604368a627026d9bc64e0847875539d04963a606abbf8b79f36` 일치.
- tar 멤버 126개를 절대 경로·`..` traversal·비정규 파일 없이 `./project`에 안전 추출.
- `python3 -m unittest project/scripts/test_design_assets.py -v`: 3개 테스트 모두 통과.
- Pillow로 WebP/JPEG 치수·모드·알파·알파 bbox 측정, ElementTree로 VectorDrawable XML과 viewport 검사.
- 이미지 뷰어로 3개 원본과 6개 생성 WebP의 실제 픽셀을 확인했다. 따라서 아래 시각 판정은 단순 파일명/메타데이터 추정이 아니다.

## 자산 인벤토리 및 판정

| 자산 | 크기/형식 | 원본 sourceRect (좌,상,우,하) | 판정 및 제안 |
|---|---:|---|---|
| `img_genesis_car_base.webp` | 567×387 RGBA | Option 1 `(410,193,977,580)` | 차량 비트맵 보존은 적절하다. 카드가 덮는 네 영역 `(257,168,501,285)`, `(883,167,1129,345)`, `(257,407,501,563)`, `(883,407,1129,563)`은 추출 마스크에서 모두 알파 0이었다. 시각적으로도 카드 글자/프레임은 남지 않았다. 다만 알파 bbox가 전체 캔버스이고 1px 경계에도 비투명 픽셀이 있어 외곽 halo/검은 테두리 가능성이 있다. **비차단 개선:** 실제 앱 배경 위에서 1–3px edge matte를 재검토하고, 원본 고해상도/레이어가 확보될 때만 occluded 차량을 복원한다. |
| `img_genesis_seat_glow.webp` | 256×256 RGBA, alpha bbox `(12,12,244,244)` | 없음 | 생성된 색상 오버레이이며 차량 그림을 대체하지 않는다. **비차단:** 렌더 배경에 따라 blend/opacity를 확인한다. |
| `img_warm_fox.webp` | 204×175 RGB | Option 2 `(366,238,570,413)` | 원본 해상도의 여우 crop이며 시각적으로 카드 안 그림이 보존된다. 투명 마스크가 아니므로 카드 모서리/배경색을 함께 배치해야 한다. **비차단:** 카드 artwork를 55~60% 채우는 데 필요한 고품질 resampling과 `ContentScale` 사용은 허용하되, 원본에 없는 디테일이 증가하지 않는다는 한계를 유지한다. 독립 투명 artwork가 필요하면 새 원본을 요청한다. |
| `img_warm_cabin.webp` | 198×175 RGB | Option 2 `(603,238,801,413)` | 별·나무·오두막이 crop 안에 보존된다. **비차단:** 카드 artwork를 55~60% 채우는 고품질 resampling/`ContentScale`은 허용하되, 원본에 없는 디테일은 증가하지 않는다. |
| `img_warm_seat.webp` | 149×112 RGB | Option 2 `(859,252,1008,364)` | 운전석/동승석 아이콘과 가장자리 배경이 함께 포함된다. **비차단:** 카드 artwork를 55~60% 채우는 고품질 resampling/`ContentScale`은 허용하되, 원본에 없는 디테일은 증가하지 않는다. 카드 배경과 경계가 맞지 않으면 재추출이 아니라 원본 기준 crop 정렬을 먼저 확인한다. |
| `img_spatial_album.webp` | 113×113 RGB | Option 3 `(739,217,852,330)` | 원본 앨범 아트가 보존된다. 이미지 자체에 앨범 표지의 작은 문자/그래픽이 포함되는 것은 원본 artwork이며 UI 차량 텍스트 오염으로 보지 않는다. **비차단:** 실제 UI 텍스트로 OCR/접근성 라벨을 대체하지 않는다. |

원본 JPG 3개는 각각 1376×768 RGB이며 알파가 없다. 모든 raster crop은 JPEG 원본 해상도와 조명을 상속한다. 카드 artwork를 목표 면적(예: 55~60%)에 맞추기 위한 고품질 resampling/`ContentScale`은 허용되지만, 원본에 없는 디테일을 증가시키지는 않는다. 카드에 가려진 차량 부분은 복원할 수 없다.

### VectorDrawable 8개

`ic_spatial_seat_ventilation`, `ic_spatial_seat_heating`, `ic_spatial_fan`, `ic_spatial_quick_mute`, `ic_spatial_home`, `ic_spatial_hvac`, `ic_spatial_media`, `ic_spatial_navigation`은 모두 24dp/24dp, viewport 24×24, XML parse 성공이다. 작은 제어 아이콘을 VectorDrawable로 둔 것은 “차량·여우·오두막·좌석은 비트맵, 제어 아이콘은 벡터” 기준과 일치한다.

- `fan` path는 x=25, `hvac` path는 x=25까지 사용해 viewport 오른쪽을 1 unit 초과한다. `seat_heating`도 path에 x=24가 있다. Android 렌더러에서 끝부분이 viewport 밖으로 clip될 가능성이 있다.
- 이 문제는 원본 artwork 손실이 아니라 아이콘 stroke/곡선 끝단의 **비차단 품질 이슈**다. 다음 자산 수정 시 path를 23 이하로 inset하거나 viewport를 의도적으로 확장하고, 24dp 실제 렌더 캡처로 끝단을 확인한다. 현재 요청 범위에서는 XML을 변경하지 않았다.

## 문서·요구사항 검토

- `docs/design/README.md`와 `ui-redesign-concepts.md`는 이미 **원본 시안 이미지가 시각적 Single Source of Truth**라고 선언한다. `asset-extraction-manifest.json`도 `sourceImagesAreVisualTruth: true`로 일치한다.
- `README.md`에는 기존 `ca30243` 실기 캡처가 이번 수정 결과가 아니라는 표기가 이미 있다. 이번 입력 archive에는 과거 `screen_*.png`가 포함되지 않았으므로, 이 자료 부재를 실제 저장소의 캡처 부재로 단정하지 않는다. 다만 `artifacts/design-review-r1/`의 prototype PNG는 실제 Compose 캡처가 아니며, 승인된 on-device 증거로 취급하면 안 된다.
- 디자인 문서에는 “캡처 전 시안 재현 완료나 유사도 수치를 주장하지 않는다”는 안전장치가 이미 있다. 테스트도 시각 유사도 미측정 주장을 검사해 통과했다. 별도 통합 시 해당 표의 오른쪽 열은 실제 캡처가 확보될 때까지 concept/prototype과 분리해 표기해야 한다.
- 성능·fidelity는 측정 로그 없이는 완료나 수치로 표현하면 안 된다. 이 검토에서는 CPU/RAM, 실제 창 bounds/density/fontScale, pixel similarity를 측정하지 않았다. 따라서 그런 완료 주장은 본 보고서의 근거가 아니다. `SUBMISSION_MILESTONES.md`와 `submission-progress.json`에 있는 instrumentation/레이아웃 완료 문구는 이 자산 검토 범위에서 재검증하지 않은 별도 주장으로 남긴다.
- 모든 라벨 최소 14sp, 440dp/230dp 고정 콘텐츠 높이 금지, responsive viewport 채움, consumer diagnostics opt-in, 실차 쓰기 금지는 구현/통합 단계의 계약으로 유지해야 한다. 자산 자체에는 텍스트 크기나 window 높이 정보가 없으므로 이 보고서에서 충족 판정하지 않는다.
- 최신 사용자 예외를 명시적으로 기록한다: Warm의 `Play Story`, `Cabin Warmth`, `Quiet Cabin` CTA는 원본의 공통 하단 dock가 아니라 **각 카드 내부 배치가 허용된 최신 명시 요구사항**이다. 이는 shared bottom dock 참조와의 의도적 예외다.

## blocker와 후속 제안

1. **현재 자산 blocker 없음:** 6개 WebP의 크기/알파 형식, 네 차량 카드 occlusion 마스크, 8개 Spatial XML parse는 기준을 충족한다.
2. **통합 전 blocker 후보:** 실제 앱 배경에서 Genesis crop의 edge halo를 확인하고, 차량 카드 글자/프레임이 재등장하지 않는 동일 조건 캡처를 확보해야 한다. 원본 JPG만으로 가려진 차량을 복원하는 것은 금지한다.
3. **비차단 vector 개선:** `fan`/`hvac`의 viewport 초과 path를 inset한 뒤 24dp 렌더에서 clipping을 확인한다.
4. **비차단 provenance:** 원본 JPG의 사용권·출처 확인 전 공개 배포 자산으로 단정하지 않는다.
5. **후속 통합 검토:** 실제 Compose 캡처·창 bounds·density·fontScale·locale·시연 상태가 제공될 때만 fidelity 비교를 수행한다. prototype PNG는 real Compose capture가 아니다.

이 보고서는 독립 Phase 0 자산/요구사항 검토 결과이며, 이후의 integrated-source review와 실제 기기/에뮬레이터 검증을 대체하지 않는다.

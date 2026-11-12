# Phase5 publication-repair final 독립 소스 리뷰

## 입력과 안전 추출

- 입력 archive: `phase5-publication-repair-review.zip`
- SHA-256: `207b9b05a9bff87c41d28f25ef520a9e76d1b3b7312a28d4495743d66ae22304` — 일치 확인
- ZIP 170개 엔트리를 검사했다.
- 절대 경로, `..` 상위 경로, realpath 탈출, symbolic link 엔트리는 없었고, regular file만 기존 `./project` 위에 안전하게 추출했다.
- production source와 86개 build input hash는 직전 `8a23...` snapshot과 동일하다는 evidence를 확인했다. 현재 `207b...` bundle은 publication repair 산출물만 추가된 snapshot이다.

## 최종 판정

`passed: true`

직전 publication blocker였던 capture metadata 참조 누락이 해결됐다. base/large content PNG 6개를 실제 archive에서 확인했고, large metadata의 rootFile/themeFile이 `fontscale-1_5-` prefix로 재매핑됐다. 모든 metadata 참조 PNG의 실제 존재 여부와 PNG 크기 대 bounds 일치를 직접 대조했다.

## Repair 검증

### Base metadata

`docs/design/native-captures/metadata.json`의 세 theme에서 다음 root/content 쌍을 모두 확인했다.

- Genesis: `genesis_luxury-root.png` — 1682×1210, `genesis_luxury-content.png` — 1654×1124
- Warm: `scandinavian_warm-root.png` — 1682×1210, `scandinavian_warm-content.png` — 1654×1124
- Spatial: `spatial_widget-root.png` — 1682×1210, `spatial_widget-content.png` — 1654×1124

### Large-font metadata

`docs/design/native-captures/fontscale-1_5-metadata.json`는 모든 rootFile/themeFile을 실제 large-font PNG로 참조한다.

- Genesis: `fontscale-1_5-genesis_luxury-root.png` / `fontscale-1_5-genesis_luxury-content.png` — content 1654×1062
- Warm: `fontscale-1_5-scandinavian_warm-root.png` / `fontscale-1_5-scandinavian_warm-content.png` — content 1654×1062
- Spatial: `fontscale-1_5-spatial_widget-root.png` / `fontscale-1_5-spatial_widget-content.png` — content 1654×1062

각 파일은 실제 archive에서 존재를 확인했고, Pillow로 읽은 실제 PNG `(width, height)`가 metadata의 bounds와 일치했다. 이전에 누락됐던 Genesis content부터 RED가 발생했고, 모든 metadata/image 크기 검사 GREEN으로 수정된 이력도 확인했다.

### Public capture SHA receipt

`docs/evidence/ui-fidelity-native.json:532–548`의 `publishedImagesSha256`에 base/large root/content와 comparison PNG의 공개 SHA-256이 추가됐다. 실제 현재 PNG bytes와 다음 해시를 대조했다.

- `genesis_luxury-content.png`: `3670676e8047fdbc90a42877c0b629121ae40f0c8c3483e21f0181aeb56aa1f8`
- `scandinavian_warm-content.png`: `89d35c8bd477dc161f0747c2e41adda78b2eb49ad7d51343bfe183126c730e7b`
- `spatial_widget-content.png`: `abc3ffefdcbf0c12cf722744f446dbd01180af828a231b5eb058f7c5352b9376`
- `fontscale-1_5-genesis_luxury-content.png`: `10bb44e94daf18e06b47d38d682ff360c3ccc814eb8936d9f73da74fd2cd70ee`
- `fontscale-1_5-scandinavian_warm-content.png`: `8da365f0fc9dc246ebd1d081431b2dd41908ee3a963593850ade3b47184839eb`
- `fontscale-1_5-spatial_widget-content.png`: `a4d19cbc517bf85a1fa69c7e8dff5971066feff22fa36919c25690c5362d6aa2`

### 회귀 테스트 계약

`scripts/test_design_assets.py:9–21`의 `test_native_gallery_metadata_has_all_referenced_images`는 두 metadata의 모든 `rootFile`/`themeFile`을 순회한다.

- 파일명이 metadata 디렉터리를 탈출하지 않는지 확인
- 실제 파일 존재 확인
- PNG를 열어 실제 크기와 metadata bounds 비교

부모가 보고한 최초 Genesis content 누락 RED 이후, 전체 metadata/image 정확 크기 검사 GREEN을 확인했다. 최종 Python 회귀 결과는 **55개 통과**이며, 이 결과는 부모 실행 증거로 기록하고 본인 테스트 실행으로 표현하지 않았다.

## Portable publication 검토

- `source-manifest.json`은 `docs/design/*.json`, `docs/design/native-captures/**`, `docs/evidence/ui-fidelity-native.json`을 포함한다.
- 새 content PNG와 large-font PNG가 capture glob에 포함된다.
- portable metadata에는 publication 문서와 capture 파일 해시가 포함된다.
- 개인정보, 인증, secret, `.env`, credential, private key, `id_rsa` 경로는 확인되지 않았다.
- `final-independent-review.json`과 `.md`는 portable metadata와 이번 JSON의 `reviewedFiles`에서 제외해 self-reference를 만들지 않았다.

## 기존 production과 evidence 범위

이번 단계는 packaging/docs 수정만 포함한다. 다음 production source hash와 86개 build input hash가 직전 `8a23...` source snapshot과 동일하다.

- Main/ViewModel/SDK/model/domain/audio/read-only gateway
- 세 theme UI 및 caption source
- Android/native acceptance, WarmActions, capture/probe tests
- Gradle/build/manifest 입력

APK source artifact는 `8a23...` build source에서 생성된 debug APK로 구분한다. 현재 `207b...` bundle은 publication PNG, metadata, receipt, test/doc 추가로 SHA가 달라진 bundle이며 새 production APK build라고 주장하지 않는다.

## Evidence 수치와 한계

중복 집계 없이 다음 수치를 유지한다.

- Python: 55개 통과
- 고유 JVM: 51개 / 실행 84회
- native flavor unit: 10회
- instrumentation: 30 discovered, 29 passed, 1 assumptionSkipped, 0 failed
- TTS: `initializationResult=-1`, `engine=null`, Korean voice inventory 0
- Car UX: parked emulator observation 80 samples; driving/suspend acceptance가 아님

최초 capture metadata 누락 RED, 이후 GREEN, native legacy failures, cold-start Compose hierarchy 실패와 attach/discovery 실패 이력을 성공 수치에 섞지 않았다. TTS 실패는 실제 음성 성공으로 해석하지 않았다.

owner visual approval은 false이고, viewer는 pixel golden이나 유사도 percentage를 주장하지 않는다. release signing, physical audio, 실제 차량 write/ANC/독립 음장, portrait/narrow OS window, driving transition, OEM suspend, performance profiling은 미완료다.

## 후속 제안

1. capture metadata의 모든 파일 참조와 PNG bounds 검사를 packaging preflight에 계속 유지한다.
2. public capture SHA receipt와 실제 PNG bytes를 release 전에 다시 대조한다.
3. source artifact `8a23...`와 publication bundle `207b...`를 계속 분리해 문서화한다.
4. Python 55, JVM 51/84, native unit 10, instrumentation 30/29/1/0을 중복 합산하지 않는다.
5. TTS `-1/null`, legacy failures, cold-start failures, owner approval false를 유지한다.

## 파일 무결성

최신 snapshot에서 실제로 읽은 72개 파일의 SHA-256을 `final-independent-review.json`의 `reviewedFiles`에 기록했다. 두 final report 자체는 self-reference 방지를 위해 제외했다. archive SHA, JSON schema, 모든 reviewed-file current-byte hash, 12개 metadata 참조 PNG의 존재·실제 크기를 검증했다.

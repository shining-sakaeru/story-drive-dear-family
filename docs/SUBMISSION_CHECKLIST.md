# 개인 1인 참가 제출 사전검사

이 문서는 `scripts/submission_preflight.py`가 읽는 **별도 JSON 계획 파일**의 점검 기준이다. 실제 계획 파일에는 제목·발신자 등 개인정보 원문이 들어갈 수 있으므로 저장소에 커밋하지 말고 `.gitignore` 대상인 `submission/` 또는 사용자가 관리하는 private 경로에 보관한다. CLI는 그 값을 결과에 출력하지 않으며 원본 개인정보 문서와 서명 이미지를 별도 개인정보 자료로 취급한다.

## 필수 제출 요건

- 작성한 접수양식 자체가 기획서다.
- 개인 참가자는 본인 한 명만 기재하며 팀원 정보와 팀원 CC는 없다.
- 개인 본인의 동의·서명·서명일이 필요하다. CLI의 `confirmed`와 `signature_present`는 사용자 선언 상태일 뿐 서명 진위나 동의 인증이 아니다.
- 수신 이메일은 `appdevchallenge@hyundai-partners.com`이다.
- 실제 제목 형식은 `[차량용 앱 개발 공모전] 두 개의 실제 비공백 항목_두 번째 실제 비공백 항목`이다. 안내의 `(개인은 성명)`·`대표자명`·`성명`·`미확정`은 제목에 넣는 리터럴이 아니다. 개인정보 값은 계획 파일의 상태 확인에만 사용하고 출력하지 않는다.
- 수정 제출은 `[수정제출]` 접두사를 먼저 분리한 뒤 동일한 본제목 형식을 검사하고, 최초와 같은 발신자이며, 마감 전 마지막 제출본에 전체 자료와 서명 동의서를 첨부한다.
- 공식 접수 기간은 `2026-10-21 00:00 KST`부터 `2026-11-16 00:00 KST` 직전까지다. `2026-11-15 23:59:59.999999 KST`까지 허용하며 자정은 exclusive다. 계획 JSON의 `as_of`/`deadline`은 이 판정을 우회하지 못한다.
- 공식 안내에 ‘제출 페이지에 입력’이라는 문구와 이메일 접수 주 경로가 함께 보인다. 별도의 실제 제출 페이지는 확인되지 않았으므로 허구의 폼을 만들지 않는다.

## 선택 가산점

- 실제 시연 영상.
- 소스와 실행 가능한 **release APK**를 포함한 GitHub 제공.
- 숏폼은 별도 선택이며 본상 가산점으로 취급하지 않는다.
- 선택 산출물이 없다고 최소 필수 접수를 차단하지 않는다.
- APK를 선택하면 `apk/팀명_앱이름_release.apk` 형태를 사용한다. 기본 ZIP 구조(`AndroidManifest.xml`, `classes.dex`)만 확인하며 META-INF v1 파일을 필수로 가정하지 않는다. `debug`, `unsigned`, 단순 파일명 변경은 거부한다. 별도 `apksigner` 서명 기록과 `adb` 설치 기록이 동일 APK SHA-256을 가리켜야 한다.
- 용량 초과 시 GitHub Releases를 사용할 수 있으나, GitHub의 source/APK 경로는 별도 release APK 항목과 같은 파일이어야 한다. private 저장소라면 공식 심사자의 접근 권한을 실제로 확인해야 한다. CLI는 GitHub 접근이나 APK 실행을 검증하지 않으며 JSON 증거는 기록 일관성 검사일 뿐이다.
- APK v2/v3 서명은 APK 내부 META-INF v1 파일을 요구하지 않을 수 있다. 참고: https://source.android.com/docs/security/features/apksigning/v2. META-INF 없는 synthetic ZIP fixture는 실제 v2 서명 증거가 아니다.

## CLI 사용

```sh
python3 scripts/submission_preflight.py --plan path/to/synthetic-or-user-created-plan.json
```

종료 코드 0은 계획에 선언된 경로·상태가 조건을 통과했다는 뜻이고, 1은 누락·미확정·차단 조건이 있다는 뜻이다. 이 결과는 실제 서명·동의 인증, 이메일 송부, GitHub 접근, APK 실행을 의미하지 않는다. 날짜·경로·서명·발신자 일치가 미확정이면 fail-closed로 차단한다.

출력에는 검사 식별자, 차단 사유, 한계 및 선택 APK 체크섬만 포함되며 PII 값은 포함하지 않는다.

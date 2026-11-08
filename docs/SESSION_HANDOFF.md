# 작업 인계 — OCI 서버 전용 진행

## 현재 상태

- 40개 기준선 중 **28개 종료(70%)**, 필수 접수는 **4/8(50%)**. TTS 점검 종료는 초기화 실패/자막 대체 확인이지 음성 기능 성공이 아니다.
- 공식 Pleos 기기 기준선은 15개 발견/14개 통과/조건부 1개 제외이며, native 단위 6회·4 variant lint 오류 0/경고 유지다. 기기 검증을 이번 서버 실행으로 대체하지 않는다.
- 서버에서 Temurin 17.0.21+9를 공식 릴리즈 SHA-256으로 확인해 복구했고, **고유 JVM 37개/실행 58회 및 demo/pleos 소스 컴파일**을 재검증했다. `docs/evidence/server-jvm-rerun.json`의 입력 해시가 현재 앱/빌드 입력과 일치한다. 새 APK는 생성하지 않았다.
- 28.5초 무음·한국어 자막 실제 영상과 네 단계 프레임: `media/`. Mock/실제 SDK 읽기/TTS 실패를 표시했고 전체 디코딩과 855프레임을 확인했다. 30초 종료 전환 전체나 실제 발화를 보여 주는 영상은 아니다.
- 개인 본인 정보·동의·서명·날짜는 작성하지 않았다. 서버 갱신 기획서는 ignored `submission/StoryDrive_DearFamily_application_SERVER_REVIEW.docx`, 검토 묶음은 `submission/StoryDrive_DearFamily_REVIEW_ONLY.zip`이다. 원본 DRAFT와 Mac Downloads 원본은 그대로다.
- 개인 문서/서명/서명키/계정 인증값은 Git에 넣지 않는다. 기존 미추적 역사 로그·연결 자료도 임의 삭제하거나 일괄 공개하지 않는다.

## 현재 권한과 제한

- Mac은 소유자 재개 통보 전까지 접근하지 않는다. Mac 대상 SSH/scp/adb/파일 전송·빌드·에뮬레이터를 실행하지 않는다.
- 이 저장소의 승인된 GitHub 체크포인트는 저장소 전용 deploy key로 인증하고, push 뒤 원격 SHA를 대조한다. 강제 push·이력 재작성·공개 범위 변경·심사자 초대·실제 이메일 접수는 승인 범위가 아니다.
- Hermes가 지시·통합·보고 담당이며 OpenHands만 하위 작업자로 사용한다. 동시 작업자는 기본 최대 3명, 파일 소유는 배타적으로 유지한다.
- 최대 15분과 완료/차단 시점에 보고한다. 보드의 진행 중 수와 실제 실행 프로세스 수를 구분한다.

## 준비한 서버 작업과 사용법

```sh
python3 scripts/package_source.py
python3 -m unittest discover -s scripts -p 'test_*.py' -v
python3 scripts/submission_preflight.py --plan submission/submission-plan.DRAFT.json
```

- 소스 ZIP은 ignored legacy artifact 없이 생성된다. `source-manifest.json`은 tracked 공개 입력 spec이고, ZIP의 파일 해시 사전은 `portable-source-metadata.json`이다. 구 Mac 전송 helper가 기존 manifest 형식을 가정하면 **Mac 재개 전에 새 metadata 형식을 지원하도록 소비자를 수정**한다. 기존 native 입력 ZIP은 보존한다.
- 사전검사는 계획/파일/검증 기록의 일관성을 확인하는 도구다. 서명 진위·동의 인증·이메일 송부·GitHub 접근·APK 설치를 대신 수행하지 않는다. 현재 실제 DRAFT 계획은 본인 동의·서명·날짜·발신자·제목 미확정으로 의도적으로 실패한다.
- 서버 JDK: `/opt/data/workspace/dearfamily-toolchain/jdk-17.0.21+9`, Gradle cache: `/opt/data/workspace/dearfamily-toolchain/gradle-home`. Mac 도구나 사용자 홈 설정을 대체·변경하지 않는다.

## 남은 기술 검증

1. TTS init=-1/default engine=null과 voice 미설치 문구를 분리해야 한다. Android 현재 사용자 기준 engine discovery/default/voice를 진단한다. 영구 음성 부재로 단정하거나 서버형 음성으로 임의 전환하지 않는다.
2. Console HTTP 200과 DDS 준비는 다르다. bridge_ready=false/actual gear=null 상태에서 SDK 좌석 변경 왕복을 완료로 표시하지 않는다.
3. 실제 주행 규제·AudioFocus·OEM Suspend·좁은 창/폰트/접근성·CPU/RAM/ANR 검증은 남아 있다.
4. debug/unsigned release를 최종 release로 이름만 바꾸지 않는다. 서명키 보관 방식과 실제 서명·설치 검증이 필요하다.
5. 읽기 전용 재개 순서는 `tts-diagnosis.md`, `console-bridge-diagnosis.md`, `mac-resume-checklist.md`에 있다. 이번에는 실제 Mac 명령을 실행하지 않았다.

## 사용자 확인/접수

`OWNER_DECISIONS.md`에 본인 작성·Mac 재개·서명키/심사자 접근·실제 접수 승인·기존 노출 시크릿 정리를 분리했다. 개인정보/비밀번호/개인키를 채팅으로 요구하지 않는다.

개인 1명은 팀원 CC가 없지만 본인 동의·서명·날짜는 필요하다. 필수 접수양식이 기획서이며 영상/GitHub/APK는 선택 가산점이다. 실제 발송은 최종 첨부·수신자·발신자·제목을 승인받은 뒤에만 한다. 마감은 **2026-11-15 23:59 KST**다.

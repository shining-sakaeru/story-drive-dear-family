# 제출 최종화 상태

2026-11-14 · 기능 추가를 동결하고, 기획서·승인 영상·검증 소스와 제출용 release APK 준비에 집중합니다.

## 준비 완료

- 기본 native: 41개 발견 / 39통과 / 2조건부 생략 / 실패 0. 조건부 생략은 opt-in 녹화와 실제 unknown 상태를 전제로 한 UI 시험입니다.
- 호스트 JVM 고유 71개 / 총 115회 실행, Python 55개 통과.
- 승인된 실제 앱 화면·ON_DEVICE 음성 영상: `media/StoryDrive_DearFamily_live_showcase.mp4`.
- 현재 debug APK 두 종류는 검증 소스 snapshot `0299f8be04b2c1783459a02dcafba079ca3e58b408c6388fb389ca7a2797a5b8`에서 생성했습니다. `apk/README.md`에 해시를 기록합니다.
- 별도 unsigned release 빌드는 source snapshot `29920450eb4871588e351e7f0b491c25f388140e0772f64c11aea4b1a904ab0d`에서 demo/pleos assemble·unit·lint가 성공했습니다. 두 variant unit 각 10개, lint Error/Fatal 0, debuggable=false, INTERNET 없음을 확인했습니다.

## 서명·제출 상태와 사용자 확인

- unsigned APK는 서명 검증이 실패하는 미서명 파일입니다. debug/unsigned를 제출용 release로 이름만 바꾸거나 업로드하지 않습니다.
- 제출용 release `apk/심찬영_StoryDriveDearFamily_release.apk`는 사용자 로컬 서명·v3 검증·실제 설치·동일 해시 대조·핵심 실행 검증을 완료했습니다. 상세는 `docs/design/RELEASE_VERIFICATION_20261114.md`입니다. 키·암호는 저장소나 대화로 받지 않았습니다.
- debug 설치본과 release 인증서가 다르면 업데이트가 거부될 수 있습니다. 기존 앱 삭제·데이터 초기화는 자동 수행하지 않습니다.
- 개인정보·본인 동의/서명·날짜 작성과 실제 이메일 발송은 사용자 담당입니다. 사용자가 작성 중인 신청서 원본은 덮어쓰지 않습니다.
- 기획서 프로젝트 부분은 별도 private DOCX로 제공하며 공개 저장소에 개인정보 양식이나 서명 이미지를 넣지 않습니다.

## 검증 한계

실제 주행/unknown 전환, Console→DDS→SDK callback 왕복, 다중 물리 디스플레이·OEM Suspend, 물리 청취는 미검증입니다. 글자 1.5배의 3테마 캡처는 확보했으나 보충 상호작용·장시간 시험은 GNSS system-server Watchdog으로 중단됐습니다. 위치 OFF 재시도의 부분 통과를 전체 성공으로 계산하지 않습니다.

현재 실제 연동은 Vehicle 좌석 read-only 및 앱 ON_DEVICE 음성입니다. 공조·마사지·조명·음장·내비 쓰기·생체 상태는 Mock/향후 계획과 구분합니다. 시장 지표는 현재 실적이 아니며, 신청서에 제안 목표를 기재할 경우 가정치임을 표시해야 합니다.

## 제출 운영

개발물 최종화가 지연되더라도 사용자가 작성·서명한 필수 양식과 승인 영상으로 먼저 접수하는 방안을 채택했습니다. 동일 발신자의 수정 제출은 전체 자료·서명 동의서를 다시 첨부하고, 최종 Git 커밋 번호를 제출 메일에 명시합니다.

공식 안내를 우선하고 `docs/submission-progress.json`의 과거 동일가중치 집계를 현재 제품 완성도로 해석하지 않습니다. 이번 승인 범위는 기존 저장소의 commit/push·APK 업로드이며 force push·공개 범위 변경·새 계정/비용·본인 서명·이메일 자동 발송은 포함하지 않습니다.

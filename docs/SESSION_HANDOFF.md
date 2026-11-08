# 작업 인계 — OCI 서버 전용 진행

## 현재 상태

- 40개 기준선 중 27개 종료(67.5%). 음성 점검 1건은 초기화 실패 조건 확인이지 기능 성공이 아니다.
- 공식 Pleos 이미지에서 UI/플랫폼 계측 15개 발견, 14개 통과/조건부 1개 제외. 단위 6회·Python 5개 통과.
- demo/pleos debug 및 unsigned release 빌드 완료. release 서명·설치 검증은 미완료.
- 실제 무음 투어 원본은 OCI의 ignored artifacts/에 보관되어 있다. 대본/자막 초안은 docs/demo-script.md와 docs/demo-captions.srt.
- 개인정보와 서명용 접수 문서는 ignored submission/에만 보관한다. 계정·클라이언트 인증정보·SSH 키·서명키를 저장소에 넣지 않는다.

## 현재 권한과 제한

- 사용자는 Mac을 끄고 휴식 중이다. 소유자가 다시 사용 가능하다고 알리기 전에는 SSH·Mac 파일 전송·에뮬레이터·Mac 빌드를 실행하지 않는다.
- OCI에서 가능한 소스/테스트 검토, 영상 편집, 문서 정리, 정적/JVM 검증을 진행한다.
- Hermes가 지시·통합·보고 담당이며 하위 개발 작업자는 OpenHands만, 독립 파일 소유 범위로 최대 3개 세션을 사용한다.
- 이 체크포인트를 GitHub에 commit/push하는 것은 사용자가 명시적으로 승인했다. 강제 push·이력 재작성·공개 범위 변경·심사자 초대·실제 이메일 접수는 승인하지 않았다.
- 장시간 작업은 최대 15분 간격과 완료/실패 시점에 중간 보고한다. 미완료 항목과 실제 실행 중인 프로세스를 구분한다.

## 해결할 기술 항목

1. TTS: 현재 foreground Android 사용자에서 initialization=-1/default engine=null. 앱의 '한국어 voice 미설치' 문구와 엔진 초기화 실패 원인을 구분해야 한다. 영구적인 음성 부재로 단정하지 말고 서버형 음성 SDK/네트워크 voice를 임의 도입하지 않는다.
2. SDK: Console HTTP 연결은 확인했으나 DDS bridge_ready=false, actual gear=null. 좌석 값 변경→SDK 콜백→화면 갱신은 아직 검증되지 않았다.
3. 안전/호환: 실제 주행 전환·AudioFocus·OEM Suspend, 좁은 창/portrait·접근성·CPU/RAM 추가 검증.
4. 영상: 무음 원본을 실제 프레임에 맞게 잘라 자막을 적용하고 최종 검수한다. 오디오가 없는 영상에 실제 음성 성공을 주장하지 않는다.
5. release: 미서명 파일의 이름만 release로 바꾸거나 제출용으로 표시하지 않는다. 서명 및 설치 검증 뒤 최종 제출 경로를 확정한다.

## 제출 조건

- 개인 1인 참가. 팀원 정보/팀원 CC는 해당 없음. 본인의 참가 정보·동의·서명·날짜는 사용자가 직접 작성한다.
- 접수양식이 기획서이며, 영상/GitHub/APK는 선택 가산점. 숏폼은 별도 선택 항목이다.
- GitHub 제출용 release APK는 apk/에 배치하고 README에 실제 파일 경로 또는 Release 링크를 기재한다. 현재 debug 파일은 테스트 전용이다.
- 실제 메일 송부와 개인정보 제공은 사용자 최종 승인 후 수행한다.

## 증거와 로컬 자료

- docs/submission-progress.json / docs/PROGRESS_REPORT.md: 진척과 미해결 항목
- docs/evidence/parallel-integration.json: 최신 실제 Mac 통합 결과
- docs/evidence/verification-result.json: 이전 Linux JVM/컴파일 결과이며 시점 구분 필요
- OCI 프로젝트 루트: /opt/data/home/projects/story-drive-dear-family
- 비공개 운영 기록: /opt/data/workspace/orchestration/dearfamily-mac-history/
- Mac 작업 중지 상태: /opt/data/workspace/orchestration/dearfamily-mac-availability.json

맥북의 Downloads 문서는 접근 권한 오류로 읽지 못했고 수정하지 않았다. 동일 공식 양식으로 작성한 프로젝트 소개 초안은 OCI의 submission/에 있다. Mac 재접속을 임의로 시도하지 않는다.

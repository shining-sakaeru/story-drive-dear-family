# Release APK 검증 — 2026-11-14

## 제출 파일

- 파일: `apk/심찬영_StoryDriveDearFamily_release.apk`
- Package: `com.pleos.dearfamily`, versionName `1.0`, versionCode `1`
- SHA-256: `38c5b1619ebdf3c8a2475b7aafe7a7a16646072f0af8c33ae79a85cc1c353615`
- 실제 사용자 로컬 서명, APK Signature Scheme v3 검증 성공. v1/v2=false는 이 APK의 서명 실패를 뜻하지 않습니다. API29 이상 대상이며 공식 API34 에뮬레이터에 설치했습니다.
- `debuggable=false`, INTERNET 권한 없음. UI의 `Mock / Debug`는 시뮬레이션 진단 영역의 이름이며 APK debug 빌드 판정과 다릅니다.

## 설치와 핵심 실행

프로젝트 전용 `DearFamily_Pleos_API34` / Pleos Connect 2.1.0 / API34 ARM64 / foreground user10에서 검증했습니다. 기존 debug 앱과 인증서가 달라 data-preserving install-r가 거부된 최초 시도를 보존했습니다. 사용자가 기존 프로젝트 앱 삭제를 승인한 뒤 `com.pleos.dearfamily`만 삭제·새 설치했습니다. 다른 앱, 신청서, 소스, 키는 삭제하지 않았습니다.

설치된 base.apk를 다시 받아 원본 release와 SHA-256이 일치함을 확인했습니다. Cold launch 성공 후 AAOS task reparenting에 따른 명시적 single-top 재실행으로 실제 앱 화면을 확인했습니다.

- 수면: 무음·TTS 없음 화면, 새 음성 start 없음.
- 소란: 모의 케어 상태·동화 자막·실제 ON_DEVICE start/done 콜백.
- 부모님: 모의 온열 케어·휴게소 제안 화면·실제 ON_DEVICE start/done 콜백.
- 중지·Home·명시적 복귀: 같은 앱 프로세스가 살아 있고, 복귀만으로 새 음성 start가 발생하지 않음.
- 실제 PNG 화면을 확인했습니다. 물리 청취·실차 ANC/마사지/내비 쓰기 성공을 뜻하지 않습니다.

## 시험 방법과 한계

이번 release 시험은 서명 APK 그대로의 ADB black-box 입력·실제 화면·앱 PID로 한정한 콜백 로그 기반 smoke check입니다. 기존 debug instrumentation 41개를 release에서 다시 통과했다고 주장하지 않습니다. demo release는 서명·정적 검증만 수행했으며 제출 대표 APK는 실제 설치·실행한 pleos 판입니다.

`uiautomator dump`는 애니메이션 중 idle 오류를 반환하면서 exit code 0을 줄 수 있었습니다. 해당 재시도의 오래된 XML은 상태 증거에서 제외하고 실제 screenshot과 새 콜백 증가를 사용했습니다. 최초 하네스 파싱 실패와 음성 완료 시점 경계 실패는 별도 보존하고, 35초 이내 새 start/done을 기다리는 bounded poll로 다시 확인했습니다. 제품 코드는 변경하지 않았습니다.

기존 기본 instrumentation 39통과/2조건부 생략/실패0과 별개입니다. 실제 주행/unknown·DDS 왕복·다중 물리 화면·OEM Suspend 및 확대·장시간 안정성의 미완료 한계는 유지합니다. 신청서 작성·개인정보 동의·본인 서명·이메일 발송은 사용자 담당이며 실제 이메일은 아직 발송하지 않았습니다.

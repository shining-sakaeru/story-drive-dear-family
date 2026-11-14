# 사용자용 release APK 서명키 안내

2026-11-14 · 상태: 사용자 로컬 서명 완료. Pleos release의 v3 서명·공식 에뮬레이터 설치·동일 해시·핵심 실행을 확인했다. `RELEASE_VERIFICATION_20261114.md` 참고. 아래 절차는 키 보관 및 향후 업데이트용 안내이며 다시 실행할 필요는 없다.

최신 사용자 결정으로 제출 최종화와 기존 저장소 commit/push·APK 업로드를 승인받았다. 키 생성과 암호 입력은 사용자가 Mac 로컬에서 수행한다. 별도로 전달하는 `prepare-signing.command`는 사용자 대화형 Terminal에서만 키 생성/서명을 진행하며, 에이전트가 실행하거나 암호를 수집하지 않는다. `--check`는 도구·unsigned APK checksum 검사만 수행한다.

## 무엇인가

앱 APK에 개발자의 서명을 붙이는 비밀키다. 직접 관리하는 Android 앱의 업데이트는 기존 인증서와 일치해야 하므로 계속 같은 키를 안전하게 보관해야 한다. `.jks` keystore는 이 키와 인증서를 보관하는 파일이다.

- 개인정보 동의서의 자필 서명과 별개다. SDK API key, SSH/GitHub 키와도 별개다.
- 현재 debug APK는 테스트용 서명이다. 최종 release APK에는 사용자 소유 release 키를 사용한다.
- 다른 프로젝트/다른 앱의 키를 가져오지 않는다. 이미 이 앱을 정식 배포했다면 기존 앱 서명키를 먼저 확인하며 임의 교체하지 않는다.

## 사용자가 해야 할 일 — 처음 정식 배포하는 앱인 경우

1. Mac 홈에 본인 전용 `StoryDriveSigning` 폴더를 만든다. 저장소/캐시 폴더 밖에 둔다.
2. Android Studio에서 이 프로젝트를 열고 `Build > Generate Signed Bundle/APK` → `APK` → `Next` → `Create new`로 들어간다. 버전에 따라 메뉴 이름에 `App Bundle or APK`가 포함될 수 있다.
3. 저장 위치는 예를 들어 `~/StoryDriveSigning/dear-family-release.jks`, 별칭(alias)은 `dear-family-release`, 유효기간은 25년 이상으로 지정한다.
4. 강한 비밀번호를 **사용자가 로컬 창에 직접** 입력하고 안전하게 보관한다. 공식 안내에 따라 key password와 keystore password는 동일하게 설정한다. 인증서 이름/조직 등도 사용자가 직접 결정한다. 인증서 값은 APK에 포함되는 공개 메타데이터이므로 불필요한 개인정보를 넣지 않는다.
5. 생성 완료 후 키 생성만 할 경우 후속 서명 대화상자를 Cancel해도 만들어진 키는 남는다. keystore 파일은 별도 안전한 위치에 백업하고 암호는 비밀번호 관리도구 등으로 보관한다.
6. Hermes에는 **완료 여부·파일 경로·alias만** 알린다. 키 파일·비밀번호·개인키를 채팅/로그/Git/일반 이메일에 올리지 않는다.

Android Studio가 없다면 이 과정 때문에 임의 설치/라이선스 동의를 하지 않는다. 승인된 기존 JDK의 로컬 대화형 keytool 방식으로 따로 안내할 수 있다. 채팅으로 암호를 받거나 비밀값을 명령행 인자로 노출하는 방식은 사용하지 않는다.

## 이후 검증

- 사용자 Mac 로컬에서 비밀번호를 입력하는 방식으로 최종 release APK를 서명한다. 사용자 키를 OCI로 복사하는 것을 기본값으로 하지 않는다.
- 서명 검증, 공개 인증서 fingerprint/alias 확인, APK SHA256, 설치/실행/회귀를 기록한다. 개인키와 비밀번호는 증거에 넣지 않는다.
- 기존 debug 앱과 서명이 다르면 업데이트 설치가 거부될 수 있다. 기존 앱 삭제나 데이터 초기화는 다시 허가받고 수행한다.
- 서명키가 준비될 때까지 기능 구현·debug 검증은 계속할 수 있다. release 서명만 의존한다.

## 확인한 공식 근거

2026-11-13 Android Developers `Sign your app`의 키 생성, 같은 인증서로 업데이트, 키 보호 항목을 직접 조회했다.

https://developer.android.com/studio/publish/app-signing?hl=en

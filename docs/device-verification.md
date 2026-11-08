# 공식 Pleos 대상 실행 검증 절차

이 문서는 **아직 수행하지 않은 대상 검증 절차**다. 결과·스크린샷을 미리 채우지 않는다. 기준 정책과 원문은 `pleos-development-guidelines.md`를 따른다.

## 준비

1. 소유자가 승인한 Android SDK/공식 Pleos 이미지, JDK 17 및 Android Studio를 준비한다. 플랫폼 API 34의 공식 Automotive AVD를 Cold boot / RAM 4 GB로 실행한다.
2. 계정 생성·SDK 신청·약관 동의·비용 발생·서버 원격 접속은 별도 허가 없이 실행하지 않는다. 이 프로젝트의 빌드/검증 외 다른 프로젝트나 기기를 조작하지 않는다.
3. 실제 Pleos의 Vehicle 서비스 버전과 읽기 권한을 확인한다. 권한 미승인을 adb 강제 grant나 시스템 권한 위장으로 우회하지 않는다.
4. `./gradlew assembleDemoDebug assemblePleosDebug` 결과와 APK SHA-256을 기록한다. 기존 APK를 새 소스의 빌드 결과로 재사용하지 않는다.

## 설치와 기본 화면

```sh
adb devices
adb install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk
adb shell am start -n com.pleos.dearfamily.demo/com.pleos.dearfamily.MainActivity
```

일반 Android의 demo에서는 요약 화면에 네 zone이 보이고, 명시적으로 정차 모의 시연을 선택하기 전에는 게임·독·음성을 실행할 수 없어야 한다. Automotive에서는 플랫폼 UX 상태가 미확인/제한이면 모의 진입 버튼으로 우회할 수 없어야 한다.

## 실제 SDK / Virtual Console

```sh
adb install -r app/build/outputs/apk/pleos/debug/app-pleos-debug.apk
adb shell am start -n com.pleos.dearfamily/com.pleos.dearfamily.MainActivity
```

- `CAR_SEATS` 미승인, 서비스 없음, Capability NONE/WRITE, 지원 영역 없음, 응답 실패가 각각 성공 상태로 표시되지 않는지 확인한다.
- 읽기 가능한 실제 영역만 나오는지 확인한다. 후석 영역이 없는 기기에서 2열/3열 지원을 생성하지 않는다.
- Virtual Console의 Seat Occupancy를 변경해 읽기/구독 결과가 `VEHICLE READ-ONLY` 영역에 반영되는지 확인한다.
- 모의 소음·피로·열선·음장 값은 별도로 MOCK 표시가 유지되어야 한다. 실제 seat occupancy 읽기 성공만으로 공간 제어까지 성공했다고 기록하지 않는다.
- 앱을 백그라운드로 전환한 뒤 listener 해제/Vehicle.release 및 stale callback 무시를 확인하고 다시 foreground에서 재연결한다.

## 규제·오디오·화면

- Virtual Console 및 공식 플랫폼 테스트 수단으로 정차/주행 규제 상태를 전환한다. 기어 변경이 항상 같은 UX 상태를 만든다고 단정하지 말고 실제 CarUxRestrictions 콜백/flags를 확인한다.
- 규제 시 상세 I-P-O, 게임, 조작 독과 애니메이션이 없어지고 음성/예약 투어가 멈추어야 한다. 시스템이 DO 미선언 앱을 차단하는 경우도 별도로 기록한다. 심사 전 DO를 임의로 선언하지 않는다.
- 한국어 offline voice가 있는 경우와 없는 경우, 네트워크가 없는 경우를 각각 확인한다. 파형은 합성 시작이 아니라 실제 MediaPlayer 재생 상태에 대응해야 한다.
- 재생 앞단 PCM이 175ms 묵음인지, 원래 첫 음절이 보존되는지 확인한다. 단순 지연만 측정한 결과로 출력 데이터를 검증했다고 하지 않는다.
- 내비/통화/긴급 상황의 공식 테스트 수단으로 포커스 거절·상실·duck/원복을 검증한다. 로그 태그 `DearFamilyAudio`에는 포커스 요청/결과/변경/반납이 기록된다. 앱 내부 시나리오 버튼이 실제 시스템 포커스 상황을 만든다고 보지 않는다.
- 수면 버튼, 빠른 시나리오 교체, 게임 열기 중에도 stale 합성/prepare callback이 음성을 재개하지 않는지 확인한다.
- 화면 OFF 및 background 정지는 실제 OEM Suspend와 별개로 기록한다. 실제 Suspend 진입 전 출력/포커스 반납, Resume 자동 재생 금지 여부는 해당 기기에서 따로 확인한다.
- Landscape 넓은 앱 영역, portrait 단일 영역, 좁은 window 전환을 확인한다. Driving view와 window bar를 가리지 않아야 한다. 프로세스를 재생성했을 때 UI 선호 설정만 복원되고 시연 동의/투어/음성이 재개되지 않아야 한다.

## 증거와 성능

성공한 각 실행의 기기/이미지/서비스 버전, 소스/빌드 식별자, APK SHA-256, 재현 단계와 실제 결과를 함께 기록한다.

```sh
adb exec-out screencap -p > docs/evidence/device-screen.png
adb shell dumpsys meminfo com.pleos.dearfamily
adb logcat -d -s DearFamilyAudio
```

CPU/RAM은 대표 시나리오·안전 제한·백그라운드에서 측정한다. 한 장의 화면이나 단일 순간 스냅샷으로 지속적인 성능 기준 충족을 주장하지 않는다. 결과가 없는 항목은 '미검증'으로 남긴다.

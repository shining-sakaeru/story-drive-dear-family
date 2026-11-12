package com.pleos.dearfamily.model

enum class SeatZone(val label: String, val accent: Long) {
    DRIVER_1L("운전석", 0xFF00E5B8), PASSENGER_1R("조수석", 0xFFFFB347),
    KIDS_2ND_ROW("2열 아이", 0xFF9B72FF), PARENTS_3RD_ROW("3열 부모님", 0xFFFF8A65)
}

enum class DriveModeState(val label: String, val badge: String) {
    DRIVING("주행 중", "GLANCEABLE DRIVING"),
    PARKED_LOUNGE("정차 / 휴식", "FAMILY LOUNGE")
}

enum class CareTab(val label: String) {
    HOME("홈"), CLIMATE("공조"), MEDIA("미디어"), NAVIGATION("내비")
}

enum class FamilyPersona(val title: String, val ageGroup: String, val description: String, val badge: String) {
    TODDLER_KIDS("유아 / 어린이", "4~7세", "상상력 자극 귓속말 미션 & 수면 오르골", "KIDS ADVENTURE"),
    TEEN_STUDENT("청소년 자녀", "13~18세", "차분한 Lo-Fi 힐링 & 집중 에어케어", "TEEN FOCUS & CALM"),
    ADULT_PARTNER("성인 파트너 / 부부", "30~40대", "따뜻한 Cozy 대화 & 감성 무드등", "COZY TALK"),
    ELDERLY_PARENTS("고령 부모님 / 조부모", "60~70대", "온열 럼버 케어 & 7080 로드 스토리", "SILVER CAREGIVER")
}

data class DriveModeThemeConfig(
    val drivingThemeName: String = "GENESIS_LUXURY",
    val parkedThemeName: String = "GENESIS_LUXURY"
)

data class SensorReading(val sdkName: String, val sensorKey: String, val currentValue: String, val isTriggered: Boolean)

data class IpoFlowState(
    val activeScenarioTitle: String = "가족 여정 대기 중",
    val targetZone: SeatZone = SeatZone.DRIVER_1L,
    val inputSensors: List<SensorReading> = emptyList(),
    val processingRule: String = "AI 컴패니언이 차내 환경을 관찰 중입니다",
    val confidenceScore: Int = 0,
    val vehicleControlOutputs: List<String> = emptyList(),
    val audioRoutingOutput: String = "공유 데모 출력 · 음소거 가능",
    val gleoTtsScript: String = "",
    val driverPeaceShieldStatus: String = "정상 · SIMULATION"
)

data class SeatStatus(
    val zone: SeatZone,
    val active: Boolean = false,
    val temperature: Float = 22.0f,
    val noiseDb: Int = 35,
    val ambientOn: Boolean = true,
    val heatingLevel: Int = 0,
    val lumbarOn: Boolean = false,
    val fanLevel: Int = 2,
    val freshAir: Boolean = false,
    val windowLocked: Boolean = true,
    val status: String = "대기"
)

data class AudioZoneState(val zone: SeatZone, val volume: Int, val status: String)

data class FamilyCareUiState(
    val seats: List<SeatStatus> = SeatZone.entries.map { SeatStatus(it) },
    val audio: List<AudioZoneState> = listOf(
        AudioZoneState(SeatZone.DRIVER_1L, 70, "내비게이션 · SIMULATION"),
        AudioZoneState(SeatZone.PASSENGER_1R, 70, "LoFi · SIMULATION"),
        AudioZoneState(SeatZone.KIDS_2ND_ROW, 70, "키즈 오디오북 · SIMULATION"),
        AudioZoneState(SeatZone.PARENTS_3RD_ROW, 70, "7080 라디오 · SIMULATION")
    ),
    val flow: IpoFlowState = IpoFlowState(),
    val noiseDb: Int = 35,
    val elapsedMin: Int = 0,
    val cozyMode: Boolean = true,
    val snackChoice: String? = null,
    val restStopAccepted: Boolean? = null,
    val restStopVisible: Boolean = false,
    val silentMessage: String? = null,
    val tourRunning: Boolean = false,
    val tourElapsedSeconds: Int = 0,
    val tourReport: Boolean = false,
    val eventId: Long = 0,
    val sleepPulseId: Long = 0,
    val driveMode: DriveModeState = DriveModeState.PARKED_LOUNGE,
    val demoDrawerOpen: Boolean = false,
    val companionMessage: String = "오늘도 온 가족이 편안한 여정이 되도록 AI가 함께합니다.",
    val persona: FamilyPersona = FamilyPersona.TODDLER_KIDS,
    val themeConfig: DriveModeThemeConfig = DriveModeThemeConfig(),
    val showSettingsDialog: Boolean = false,
    // Local care intents, not proof of audible playback or physical vehicle writes.
    val storyPlaying: Boolean = false,
    val cabinWarmth: Boolean = false,
    val quietCabin: Boolean = false,
    val selectedTab: CareTab = CareTab.HOME,
    val selectedScenario: String = "Night Journey"
)

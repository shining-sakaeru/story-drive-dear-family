package com.pleos.dearfamily.model

enum class SeatZone(val label: String, val accent: Long) {
    DRIVER_1L("운전석", 0xFF00E5B8), PASSENGER_1R("조수석", 0xFFFFB347),
    KIDS_2ND_ROW("2열 아이", 0xFF9B72FF), PARENTS_3RD_ROW("3열 부모님", 0xFFFF8A65)
}
data class SensorReading(val sdkName: String, val sensorKey: String, val currentValue: String, val isTriggered: Boolean)
data class IpoFlowState(
    val activeScenarioTitle: String = "초기 가족 케어 대기",
    val targetZone: SeatZone = SeatZone.DRIVER_1L,
    val inputSensors: List<SensorReading> = emptyList(),
    val processingRule: String = "센서 입력을 기다리는 중",
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
    val sleepPulseId: Long = 0
)

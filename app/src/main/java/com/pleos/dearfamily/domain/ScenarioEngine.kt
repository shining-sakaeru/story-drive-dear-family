package com.pleos.dearfamily.domain

import com.pleos.dearfamily.model.FamilyPersona
import com.pleos.dearfamily.model.IpoFlowState
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.model.SensorReading

enum class Scenario { KIDS_NOISE, KIDS_SLEEP, PARENTS, PASSENGER, TEEN_FOCUS }

data class Context(
    val noiseDb: Int = 35, val elapsedMin: Int = 0, val tunnelMeters: Int = 300,
    val quietSeconds: Int = 0, val eyesClosed: Boolean = false,
    val breathingStable: Boolean = false, val pressureShift: Boolean = false,
    val oldRoadDistanceKm: Double = 99.0, val rain: Boolean = false,
    val lowLight: Boolean = false, val conversation: Boolean = false,
    val lateralG: Double = 0.28 // Informational sensor, never a mandatory whisper trigger.
)

object ScenarioEngine {
    const val KIDS_SCRIPT = "쉿! 다들 내 목소리 들리니? 지금 우리 차가 잠든 거인이 사는 비밀 터널에 들어왔어. 거인이 깨지 않게 가장 작은 귓속말로 구름 사탕이라고 속삭여 볼까?"
    const val TEEN_SCRIPT = "오늘 시험 준비하느라 피곤했지? 2열 에어컨을 21.5도로 쾌적하게 맞추고, 편안한 Lo-Fi 플레이리스트를 띄워둘게. 도착할 때까지 편하게 눈 붙여도 돼."
    const val PARENTS_SCRIPT = "어머님, 아버님, 오래 앉아 계시느라 허리 뻐근하시죠? 등받이를 따뜻하게 데워 드렸어요. 지금 지나가는 대관령 길은 1970년대에 처음 고속도로가 뚫리던 추억의 명소랍니다."
    const val PASSENGER_SCRIPT = "빗소리와 어울리는 따뜻한 조명을 켰어요. 두 분이 이야기 나누시는 동안 음악 소리는 살짝 줄여둘게요."

    fun evaluate(c: Context): Scenario? = when {
        c.noiseDb > 75 && c.tunnelMeters <= 300 -> Scenario.KIDS_NOISE
        c.noiseDb <= 35 && c.quietSeconds >= 180 && c.eyesClosed && c.breathingStable -> Scenario.KIDS_SLEEP
        c.elapsedMin > 50 && c.pressureShift && c.oldRoadDistanceKm <= 3.0 -> Scenario.PARENTS
        c.rain && c.lowLight && c.conversation -> Scenario.PASSENGER
        else -> null
    }

    fun flow(
        s: Scenario,
        noiseDb: Int = 35,
        elapsedMin: Int = 0,
        tunnelMeters: Int = 300,
        persona: FamilyPersona = FamilyPersona.TODDLER_KIDS
    ): IpoFlowState = when (s) {
        Scenario.KIDS_NOISE -> {
            val isTeen = persona == FamilyPersona.TEEN_STUDENT
            IpoFlowState(
                activeScenarioTitle = if (isTeen) "2열 청소년 소란 완화 · 힐링 케어" else "2열 아이 소란 케어",
                targetZone = SeatZone.KIDS_2ND_ROW,
                inputSensors = listOf(
                    SensorReading("Vehicle SDK", "cabin_mic", "${noiseDb}dB", noiseDb > 75),
                    SensorReading("NaviHelper SDK", "tunnel", "${tunnelMeters}m", tunnelMeters <= 300),
                    SensorReading("Vehicle SDK", "lateral_g", "0.28G", true)
                ),
                processingRule = if (isTeen) "noise > 75 & teen profile -> Lo-Fi & Refresh" else "noise > 75 AND tunnel ≤ 300m",
                confidenceScore = 96,
                vehicleControlOutputs = listOf(
                    "후석 음량 점진 하향 · Multi-Zone Adapter LOCAL MOCK",
                    "신선한 공기 · Multi-Zone Adapter LOCAL MOCK",
                    "보라색 라이트 · Multi-Zone Adapter LOCAL MOCK"
                ),
                audioRoutingOutput = "2열 방향성 오디오 · LOCAL MOCK",
                gleoTtsScript = if (isTeen) TEEN_SCRIPT else KIDS_SCRIPT,
                driverPeaceShieldStatus = "완벽 차단 · 내비 100% 전방 집중"
            )
        }
        Scenario.TEEN_FOCUS -> IpoFlowState(
            activeScenarioTitle = "2열 청소년 힐링·몰입 케어",
            targetZone = SeatZone.KIDS_2ND_ROW,
            inputSensors = listOf(
                SensorReading("Context Engine LOCAL MOCK", "user_profile", "Teenager (15세)", true),
                SensorReading("Vehicle SDK", "cabin_mic", "${noiseDb}dB", true),
                SensorReading("Vehicle SDK", "hvac_temp", "21.5°C", true)
            ),
            processingRule = "teen student mode: Lo-Fi beats & cool airflow",
            confidenceScore = 95,
            vehicleControlOutputs = listOf("2열 21.5°C 에어케어", "인디고 앰비언트", "소음 흡음 모드"),
            audioRoutingOutput = "2열 지향성 Lo-Fi 플레이리스트 · LOCAL MOCK",
            gleoTtsScript = TEEN_SCRIPT,
            driverPeaceShieldStatus = "평온 유지 · 무간섭 사운드 존"
        )
        Scenario.KIDS_SLEEP -> IpoFlowState(
            activeScenarioTitle = "2열 수면 케어",
            targetZone = SeatZone.KIDS_2ND_ROW,
            inputSensors = listOf(
                SensorReading("Vehicle SDK", "cabin_mic", "${noiseDb}dB / 180초", noiseDb <= 35),
                SensorReading("Vehicle SDK", "eyes", "closed", true),
                SensorReading("Context Engine LOCAL MOCK", "breathing", "stable", true)
            ),
            processingRule = "noise ≤ 35dB for 180s AND eyes closed AND breathing stable",
            confidenceScore = 99,
            vehicleControlOutputs = listOf("독서등 OFF", "22.5°C / fan 1", "창문 잠금"),
            audioRoutingOutput = "음소거 · TTS 없음",
            gleoTtsScript = "",
            driverPeaceShieldStatus = "수면 모드 진입 · 운전석 방해 0dB"
        )
        Scenario.PARENTS -> IpoFlowState(
            activeScenarioTitle = "3열 부모님 피로 케어",
            targetZone = SeatZone.PARENTS_3RD_ROW,
            inputSensors = listOf(
                SensorReading("Vehicle SDK", "elapsed", "${elapsedMin}분", elapsedMin > 50),
                SensorReading("Context Engine LOCAL MOCK", "pressure", "shift", true),
                SensorReading("NaviHelper SDK", "road", "old road ≤3km", true)
            ),
            processingRule = "elapsed > 50 AND pressure shift AND location ≤ 3km",
            confidenceScore = 94,
            vehicleControlOutputs = listOf(
                "등받이 heat 1 · LOCAL MOCK",
                "lumbar ON · LOCAL MOCK",
                "roof airflow · LOCAL MOCK",
                "7080 acoustic radio · LOCAL MOCK"
            ),
            audioRoutingOutput = "3열 공유 데모 출력 · LOCAL MOCK",
            gleoTtsScript = PARENTS_SCRIPT,
            driverPeaceShieldStatus = "운전자 개입 불필요 · 자동 안락 케어"
        )
        Scenario.PASSENGER -> IpoFlowState(
            activeScenarioTitle = "조수석 Cozy 대화 케어",
            targetZone = SeatZone.PASSENGER_1R,
            inputSensors = listOf(
                SensorReading("Vehicle SDK", "rain", "true", true),
                SensorReading("Vehicle SDK", "light", "low", true),
                SensorReading("Context Engine LOCAL MOCK", "conversation", "active", true)
            ),
            processingRule = "rain AND low light AND conversation",
            confidenceScore = 91,
            vehicleControlOutputs = listOf(
                "amber 조명 · LOCAL MOCK",
                "LoFi 70% → 25% 점진 하향 · LOCAL MOCK"
            ),
            audioRoutingOutput = "조수석 공유 데모 출력 · LOCAL MOCK",
            gleoTtsScript = PASSENGER_SCRIPT,
            driverPeaceShieldStatus = "대화 존 분리 · 내비 안내 음성 우선"
        )
    }
}

package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.domain.ScenarioEngine
import com.pleos.dearfamily.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MockPleosSdkManager(private val scope: CoroutineScope) {
    private val _state = MutableStateFlow(FamilyCareUiState())
    val state: StateFlow<FamilyCareUiState> = _state.asStateFlow()
    private var tourJob: Job? = null
    private val volumeJobs = mutableMapOf<SeatZone, Job>()
    private var noiseArmed = true
    private var elapsedArmed = true

    fun trigger(scenario: Scenario) {
        cancelTourOnly()
        val seeded = when (scenario) {
            Scenario.KIDS_NOISE -> _state.value.copy(noiseDb = 82)
            Scenario.KIDS_SLEEP -> _state.value.copy(noiseDb = 35)
            Scenario.PARENTS -> _state.value.copy(elapsedMin = 52)
            Scenario.PASSENGER -> _state.value
            Scenario.TEEN_FOCUS -> _state.value.copy(noiseDb = 50)
        }
        _state.value = seeded
        if (scenario == Scenario.TEEN_FOCUS) {
            applyScenario(Scenario.TEEN_FOCUS, forceEvent = true)
        } else {
            val context = when (scenario) {
                Scenario.KIDS_NOISE -> com.pleos.dearfamily.domain.Context(noiseDb = seeded.noiseDb, tunnelMeters = 300)
                Scenario.KIDS_SLEEP -> com.pleos.dearfamily.domain.Context(noiseDb = 35, quietSeconds = 180, eyesClosed = true, breathingStable = true)
                Scenario.PARENTS -> com.pleos.dearfamily.domain.Context(elapsedMin = seeded.elapsedMin, pressureShift = true, oldRoadDistanceKm = 3.0)
                Scenario.PASSENGER -> com.pleos.dearfamily.domain.Context(rain = true, lowLight = true, conversation = true)
                else -> com.pleos.dearfamily.domain.Context()
            }
            ScenarioEngine.evaluate(context)?.let { applyScenario(it, forceEvent = true) }
        }
    }
    fun setNoise(value: Int) {
        cancelTourOnly()
        val safe = value.coerceIn(30, 100)
        val old = _state.value.noiseDb
        _state.update { it.copy(noiseDb = safe) }
        if (_state.value.flow.targetZone == SeatZone.KIDS_2ND_ROW && _state.value.flow.activeScenarioTitle.contains("소란")) refreshScenario(Scenario.KIDS_NOISE)
        if (safe <= 75) noiseArmed = true
        if (noiseArmed && old <= 75 && safe > 75) { noiseArmed = false; applyScenario(Scenario.KIDS_NOISE, forceEvent = true) }
    }
    fun setElapsed(value: Int) {
        cancelTourOnly()
        val safe = value.coerceIn(0, 120)
        val old = _state.value.elapsedMin
        _state.update { it.copy(elapsedMin = safe) }
        if (_state.value.flow.targetZone == SeatZone.PARENTS_3RD_ROW) refreshScenario(Scenario.PARENTS)
        if (safe <= 50) elapsedArmed = true
        if (elapsedArmed && old <= 50 && safe > 50) { elapsedArmed = false; val context = com.pleos.dearfamily.domain.Context(elapsedMin = safe, pressureShift = true, oldRoadDistanceKm = 3.0); ScenarioEngine.evaluate(context)?.let { applyScenario(it, forceEvent = true) } }
    }
    fun toggleMode() { cancelTourOnly(); _state.update { it.copy(cozyMode = !it.cozyMode) } }
    fun chooseSnack(choice: String) { cancelTourOnly(); _state.update { it.copy(snackChoice = choice) } }
    fun answerRestStop(accepted: Boolean) { cancelTourOnly(); _state.update { it.copy(restStopAccepted = accepted, restStopVisible = true) } }
    fun endConversation() { cancelTourOnly(); volumeJobs[SeatZone.PASSENGER_1R]?.cancel(); volumeJobs[SeatZone.PASSENGER_1R] = scope.launch { restoreVolume(SeatZone.PASSENGER_1R) } }
    fun startTour() {
        tourJob?.cancel()
        volumeJobs.values.forEach(Job::cancel)
        volumeJobs.clear()
        tourJob = scope.launch {
            _state.update { it.copy(noiseDb = 82, tourRunning = true, tourElapsedSeconds = 0, tourReport = false) }
            applyScenario(Scenario.KIDS_NOISE, cancel = false, forceEvent = true)
            repeat(30) { second ->
                delay(1_000)
                val elapsed = second + 1
                when (elapsed) {
                    7 -> {
                        _state.update { it.copy(tourElapsedSeconds = elapsed, elapsedMin = 52) }
                        applyScenario(Scenario.PARENTS, cancel = false, forceEvent = true)
                    }
                    14 -> {
                        _state.update { it.copy(tourElapsedSeconds = elapsed) }
                        applyScenario(Scenario.PASSENGER, cancel = false, forceEvent = true)
                    }
                    21 -> {
                        val report = _state.value.flow.copy(
                            activeScenarioTitle = "가족 케어 데모 리포트",
                            targetZone = SeatZone.DRIVER_1L,
                            inputSensors = listOf(SensorReading("Context Engine LOCAL MOCK", "tour_report", "21초 리포트", true)),
                            processingRule = "AUTO_TOUR completed care summary",
                            vehicleControlOutputs = listOf("운전자 평온 리포트 · LOCAL MOCK"),
                            audioRoutingOutput = "운전자 리포트 · TTS 없음",
                            gleoTtsScript = ""
                        )
                        _state.update { it.copy(tourElapsedSeconds = elapsed, tourReport = true, eventId = it.eventId + 1, flow = report) }
                    }
                    30 -> _state.update { it.copy(tourElapsedSeconds = elapsed, tourRunning = false, tourReport = true) }
                    else -> _state.update { it.copy(tourElapsedSeconds = elapsed) }
                }
            }
        }
    }
    fun stopTour() { tourJob?.cancel(); tourJob = null; _state.update { it.copy(tourRunning = false) } }
    fun reset() { cancelManualJobs(); noiseArmed = true; elapsedArmed = true; _state.value = FamilyCareUiState() }
    fun shutdown() { cancelManualJobs() }

    fun setPersona(persona: FamilyPersona) {
        cancelTourOnly()
        _state.update { it.copy(persona = persona) }
        if (_state.value.flow.targetZone == SeatZone.KIDS_2ND_ROW) {
            refreshScenario(if (persona == FamilyPersona.TEEN_STUDENT) Scenario.TEEN_FOCUS else Scenario.KIDS_NOISE)
        }
    }

    fun updateThemeConfig(driving: String, parked: String) {
        _state.update { it.copy(themeConfig = DriveModeThemeConfig(driving, parked)) }
    }

    fun toggleDriveMode() {
        _state.update {
            val next = if (it.driveMode == DriveModeState.DRIVING) DriveModeState.PARKED_LOUNGE else DriveModeState.DRIVING
            it.copy(driveMode = next)
        }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _state.update { it.copy(showSettingsDialog = visible) }
    }

    private fun cancelManualJobs() { tourJob?.cancel(); tourJob = null; volumeJobs.values.forEach(Job::cancel); volumeJobs.clear(); _state.update { it.copy(tourRunning = false) } }
    private fun cancelTourOnly() { tourJob?.cancel(); tourJob = null; _state.update { it.copy(tourRunning = false) } }
    private fun refreshScenario(scenario: Scenario) {
        val old = _state.value
        val flow = ScenarioEngine.flow(scenario, old.noiseDb, old.elapsedMin, persona = old.persona)
        _state.update { it.copy(flow = flow, seats = old.seats.map { seat -> seatFor(seat, scenario, old.noiseDb) }) }
    }
    private fun applyScenario(scenario: Scenario, cancel: Boolean = true, forceEvent: Boolean = false) {
        if (cancel) { tourJob?.cancel(); tourJob = null }
        if (scenario == Scenario.KIDS_SLEEP) {
            volumeJobs[SeatZone.KIDS_2ND_ROW]?.cancel()
            volumeJobs.remove(SeatZone.KIDS_2ND_ROW)
        }
        val old = _state.value
        val flow = ScenarioEngine.flow(scenario, old.noiseDb, old.elapsedMin, persona = old.persona)
        val newEvent = forceEvent || old.flow.activeScenarioTitle != flow.activeScenarioTitle
        val companionMsg = when (scenario) {
            Scenario.KIDS_NOISE -> if (old.persona == FamilyPersona.TEEN_STUDENT) "2열 청소년을 위한 힐링 사운드와 온도를 맞추었습니다." else "아이들을 위해 조용한 비밀 터널 미션을 속삭입니다."
            Scenario.TEEN_FOCUS -> "2열 학업 피로를 덜어주는 Lo-Fi 비트와 쾌적한 쿨링을 작동합니다."
            Scenario.KIDS_SLEEP -> "2열 아이들이 잠들었습니다. 조명을 낮추고 수면 케어를 시작합니다."
            Scenario.PARENTS -> "3열 부모님의 허리 피로를 위해 온열 럼버 케어를 시작합니다."
            Scenario.PASSENGER -> "조수석 파트너와 편안하게 대화하실 수 있도록 음악을 은은하게 조절했습니다."
        }
        _state.update { it.copy(flow = flow, companionMessage = companionMsg, seats = old.seats.map { seat -> seatFor(seat, scenario, old.noiseDb) }, audio = old.audio.map { audio -> audioFor(audio, scenario) }, silentMessage = if (scenario == Scenario.KIDS_SLEEP) "2열 아이들이 잠들었습니다. 후석 수면 케어를 시작합니다." else null, restStopVisible = scenario == Scenario.PARENTS || (scenario == Scenario.PASSENGER && old.restStopVisible), tourReport = false, eventId = if (newEvent) old.eventId + 1 else old.eventId, sleepPulseId = if (scenario == Scenario.KIDS_SLEEP && newEvent) old.sleepPulseId + 1 else old.sleepPulseId) }
        if (scenario == Scenario.KIDS_NOISE || scenario == Scenario.PASSENGER || scenario == Scenario.TEEN_FOCUS) duckVolume(flow.targetZone, if (scenario == Scenario.PASSENGER) 25 else if (scenario == Scenario.TEEN_FOCUS) 45 else 40)
    }
    private fun seatFor(old: SeatStatus, scenario: Scenario, noise: Int): SeatStatus = when (scenario) {
        Scenario.KIDS_NOISE -> if (old.zone == SeatZone.KIDS_2ND_ROW) old.copy(active = true, noiseDb = noise, ambientOn = true, fanLevel = 2, freshAir = true, status = "소란 케어") else old
        Scenario.TEEN_FOCUS -> if (old.zone == SeatZone.KIDS_2ND_ROW) old.copy(active = true, temperature = 21.5f, fanLevel = 2, status = "힐링·몰입") else old
        Scenario.KIDS_SLEEP -> if (old.zone == SeatZone.KIDS_2ND_ROW) old.copy(active = true, noiseDb = 35, temperature = 22.5f, ambientOn = false, fanLevel = 1, windowLocked = true, status = "수면 케어") else old
        Scenario.PARENTS -> if (old.zone == SeatZone.PARENTS_3RD_ROW) old.copy(active = true, heatingLevel = 1, lumbarOn = true, status = "피로 케어") else old
        Scenario.PASSENGER -> if (old.zone == SeatZone.PASSENGER_1R) old.copy(active = true, status = "Cozy 대화") else old
    }
    private fun audioFor(old: AudioZoneState, scenario: Scenario): AudioZoneState {
        val target = ScenarioEngine.flow(scenario).targetZone
        if (old.zone != target) return old
        return when (scenario) {
            Scenario.KIDS_SLEEP -> old.copy(volume = 0, status = "수면 음소거 · SIMULATION")
            Scenario.KIDS_NOISE -> old.copy(status = "키즈 오디오북 · directional SIMULATION")
            Scenario.TEEN_FOCUS -> old.copy(volume = 45, status = "Lo-Fi 스터디 · SIMULATION")
            Scenario.PARENTS -> old.copy(status = "7080 라디오 · SIMULATION")
            Scenario.PASSENGER -> old.copy(status = "LoFi duck · SIMULATION")
        }
    }
    private fun duckVolume(zone: SeatZone, target: Int) { volumeJobs[zone]?.cancel(); volumeJobs[zone] = scope.launch { val start = _state.value.audio.first { it.zone == zone }.volume; for (step in 1..5) { delay(100); val value = start + (target - start) * step / 5; _state.update { it.copy(audio = it.audio.map { a -> if (a.zone == zone) a.copy(volume = value) else a }) } } } }
    private suspend fun restoreVolume(zone: SeatZone) { val start = _state.value.audio.first { it.zone == zone }.volume; for (step in 1..5) { delay(100); val value = start + (70 - start) * step / 5; _state.update { it.copy(audio = it.audio.map { a -> if (a.zone == zone) a.copy(volume = value, status = "대화 종료 · 복원") else a }) } } }
}

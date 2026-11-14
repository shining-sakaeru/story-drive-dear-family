package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.domain.ScenarioEngine
import com.pleos.dearfamily.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MockPleosSdkManager(
    private val scope: CoroutineScope,
    private val monotonicMillis: () -> Long = { System.nanoTime() / 1_000_000 }
) {
    private val _state = MutableStateFlow(FamilyCareUiState())
    val state: StateFlow<FamilyCareUiState> = _state.asStateFlow()
    private var tourJob: Job? = null
    private val volumeJobs = mutableMapOf<SeatZone, Job>()
    private var noiseArmed = true
    private var elapsedArmed = true
    private var tourGeneration = 0L
    private var narratedFeedbackEventId: Long? = null
    private var narratedFeedbackOutcome: NarrationOutcome? = null
    private var narratedFeedbackSignal: CompletableDeferred<NarrationOutcome>? = null
    private var narratedSceneDeadline: Long? = null

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
    fun toggleStoryPlayback() {
        cancelTourOnly()
        _state.update { old ->
            if (old.quietCabin || old.silentMessage != null) old
            else {
                val play = !old.storyPlaying
                old.copy(storyPlaying = play, eventId = old.eventId + 1,
                    flow = old.flow.copy(gleoTtsScript = if (play)
                        "별빛 여정이 시작됩니다. 작은 여우는 따뜻한 담요를 덮고 별들이 들려주는 조용한 이야기에 귀를 기울였어요." else ""))
            }
        }
    }
    fun toggleCabinWarmth() {
        cancelTourOnly()
        _state.update { it.copy(cabinWarmth = !it.cabinWarmth) }
    }
    fun toggleQuietCabin() {
        cancelTourOnly()
        _state.update { old -> old.copy(quietCabin = !old.quietCabin,
            storyPlaying = false, eventId = old.eventId + 1,
            flow = old.flow.copy(gleoTtsScript = "")) }
    }
    fun selectTab(tab: CareTab) { _state.update { it.copy(selectedTab = tab) } }
    fun selectScenario(scenario: Scenario) { trigger(scenario) }
    fun setSeatVentilation(level: Int) {
        cancelTourOnly()
        val safe = level.coerceIn(0, 3)
        _state.update { it.copy(seats = it.seats.map { seat ->
            if (seat.zone == SeatZone.PASSENGER_1R) seat.copy(fanLevel = safe) else seat
        }) }
    }
    fun toggleSeatHeating() {
        cancelTourOnly()
        _state.update { it.copy(seats = it.seats.map { seat ->
            if (seat.zone == SeatZone.PARENTS_3RD_ROW)
                seat.copy(heatingLevel = if (seat.heatingLevel > 0) 0 else 1) else seat
        }) }
    }
    fun chooseSnack(choice: String) { cancelTourOnly(); _state.update { it.copy(snackChoice = choice) } }
    fun answerRestStop(accepted: Boolean) { cancelTourOnly(); _state.update { it.copy(restStopAccepted = accepted, restStopVisible = true) } }
    fun endConversation() { cancelTourOnly(); volumeJobs[SeatZone.PASSENGER_1R]?.cancel(); volumeJobs[SeatZone.PASSENGER_1R] = scope.launch { restoreVolume(SeatZone.PASSENGER_1R) } }
    fun onNarrationFeedback(eventId: Long, outcome: NarrationOutcome) {
        if (!_state.value.tourRunning || narratedFeedbackEventId != eventId || narratedFeedbackOutcome != null) return
        if (narratedSceneDeadline?.let { monotonicMillis() >= it } == true) {
            stopTour()
            return
        }
        if (outcome == NarrationOutcome.CANCELLED) {
            stopTour()
            return
        }
        narratedFeedbackOutcome = outcome
        narratedFeedbackSignal?.complete(outcome)
    }
    fun startTour(awaitNarration: Boolean = false) {
        tourJob?.cancel()
        volumeJobs.values.forEach(Job::cancel)
        volumeJobs.clear()
        val generation = ++tourGeneration
        narratedFeedbackEventId = null
        narratedFeedbackOutcome = null
        narratedFeedbackSignal = null
        narratedSceneDeadline = null
        if (awaitNarration) {
            startNarratedTour(generation)
            return
        }
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
                    21 -> publishTourReport(elapsed)
                    30 -> _state.update { it.copy(tourElapsedSeconds = elapsed, tourRunning = false, tourReport = true) }
                    else -> _state.update { it.copy(tourElapsedSeconds = elapsed) }
                }
            }
        }
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun startNarratedTour(generation: Long) {
        _state.update { it.copy(noiseDb = 82, tourRunning = true, tourElapsedSeconds = 0, tourReport = false) }
        applyScenario(Scenario.KIDS_NOISE, cancel = false, forceEvent = true)
        narratedFeedbackEventId = _state.value.eventId
        narratedFeedbackOutcome = null
        narratedFeedbackSignal = CompletableDeferred()
        narratedSceneDeadline = monotonicMillis() + 30_000
        tourJob = scope.launch {
            var elapsed = 0
            for (scene in listOf(Scenario.KIDS_NOISE, Scenario.PARENTS, Scenario.PASSENGER)) {
                if (scene != Scenario.KIDS_NOISE) {
                    _state.update { it.copy(elapsedMin = if (scene == Scenario.PARENTS) 52 else it.elapsedMin) }
                    applyScenario(scene, cancel = false, forceEvent = true)
                    narratedFeedbackEventId = _state.value.eventId
                    narratedFeedbackOutcome = null
                    narratedFeedbackSignal = CompletableDeferred()
                    narratedSceneDeadline = monotonicMillis() + 30_000
                }
                val startedAt = elapsed
                val signal = narratedFeedbackSignal ?: return@launch
                while (narratedFeedbackOutcome == null) {
                    select<Unit> {
                        onTimeout(1_000) {
                            elapsed++
                            _state.update { it.copy(tourElapsedSeconds = elapsed) }
                        }
                        signal.onAwait { outcome -> narratedFeedbackOutcome = outcome }
                    }
                    if (elapsed - startedAt >= 30) { stopTour(); return@launch }
                }
                while (elapsed - startedAt < 7) {
                    delay(1_000)
                    elapsed++
                    _state.update { it.copy(tourElapsedSeconds = elapsed) }
                }
                if (generation != tourGeneration || !_state.value.tourRunning) return@launch
            }
            publishTourReport(elapsed)
            repeat(9) {
                delay(1_000)
                elapsed++
                _state.update { it.copy(tourElapsedSeconds = elapsed) }
            }
            _state.update { it.copy(tourRunning = false, tourReport = true) }
        }
    }
    private fun publishTourReport(elapsed: Int) {
        val report = _state.value.flow.copy(
            activeScenarioTitle = "가족 케어 데모 리포트", targetZone = SeatZone.DRIVER_1L,
            inputSensors = listOf(SensorReading("Context Engine LOCAL MOCK", "tour_report", "${elapsed}초 리포트", true)),
            processingRule = "AUTO_TOUR completed care summary",
            vehicleControlOutputs = listOf("운전자 평온 리포트 · LOCAL MOCK"),
            audioRoutingOutput = "운전자 리포트 · TTS 없음", gleoTtsScript = "")
        _state.update { it.copy(tourElapsedSeconds = elapsed, tourReport = true, eventId = it.eventId + 1, flow = report) }
    }
    fun stopTour() {
        ++tourGeneration
        narratedFeedbackEventId = null
        narratedFeedbackOutcome = null
        narratedFeedbackSignal = null
        narratedSceneDeadline = null
        val wasActive = tourJob != null || _state.value.tourRunning || _state.value.storyPlaying || _state.value.flow.gleoTtsScript.isNotEmpty()
        tourJob?.cancel()
        tourJob = null
        volumeJobs.values.forEach(Job::cancel)
        volumeJobs.clear()
        _state.update { old ->
            if (!wasActive) old.copy(tourRunning = false)
            else old.copy(tourRunning = false, storyPlaying = false, eventId = old.eventId + 1, flow = old.flow.copy(gleoTtsScript = ""))
        }
    }
    fun reset() { cancelManualJobs(); noiseArmed = true; elapsedArmed = true; _state.value = FamilyCareUiState(eventId = _state.value.eventId + 1) }
    fun shutdown() {
        cancelManualJobs()
        _state.update { it.copy(storyPlaying = false, flow = it.flow.copy(gleoTtsScript = "")) }
    }

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

    private fun cancelManualJobs() { cancelTourOnly(); volumeJobs.values.forEach(Job::cancel); volumeJobs.clear() }
    private fun cancelTourOnly() { ++tourGeneration; tourJob?.cancel(); tourJob = null; narratedFeedbackEventId = null; narratedFeedbackOutcome = null; narratedFeedbackSignal = null; narratedSceneDeadline = null; _state.update { it.copy(tourRunning = false) } }
    private fun refreshScenario(scenario: Scenario) {
        val old = _state.value
        val flow = ScenarioEngine.flow(scenario, old.noiseDb, old.elapsedMin, persona = old.persona)
        _state.update { it.copy(flow = flow, selectedScenario = scenario.name, seats = old.seats.map { seat -> seatFor(seat, scenario, old.noiseDb) }) }
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
        _state.update { it.copy(flow = flow, selectedScenario = scenario.name, storyPlaying = false, companionMessage = companionMsg, seats = old.seats.map { seat -> seatFor(seat, scenario, old.noiseDb) }, audio = old.audio.map { audio -> audioFor(audio, scenario) }, silentMessage = if (scenario == Scenario.KIDS_SLEEP) "2열 아이들이 잠들었습니다. 후석 수면 케어를 시작합니다." else null, restStopVisible = scenario == Scenario.PARENTS || (scenario == Scenario.PASSENGER && old.restStopVisible), tourReport = false, eventId = if (newEvent) old.eventId + 1 else old.eventId, sleepPulseId = if (scenario == Scenario.KIDS_SLEEP && newEvent) old.sleepPulseId + 1 else old.sleepPulseId) }
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

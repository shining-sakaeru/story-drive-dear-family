package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.model.FamilyCareUiState
import com.pleos.dearfamily.domain.Context
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.domain.ScenarioEngine
import com.pleos.dearfamily.sdk.MockPleosSdkManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyAcceptanceTest {
    @Test fun independentZonesCompleteDuckingWithoutCancellingEachOther() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.trigger(Scenario.KIDS_NOISE); advanceTimeBy(100); runCurrent()
            sdk.trigger(Scenario.PASSENGER); advanceUntilIdle()
            assertEquals(40, sdk.state.value.audio.single { it.zone == SeatZone.KIDS_2ND_ROW }.volume)
            assertEquals(25, sdk.state.value.audio.single { it.zone == SeatZone.PASSENGER_1R }.volume)
        } finally { sdk.shutdown() }
    }

    @Test fun openingGameDoesNotCancelConversationDucking() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.trigger(Scenario.PASSENGER); runCurrent()
            sdk.toggleMode(); advanceUntilIdle()
            assertEquals(25, sdk.state.value.audio.single { it.zone == SeatZone.PASSENGER_1R }.volume)
        } finally { sdk.shutdown() }
    }

    @Test fun tourProgressAdvancesEverySecondAndManualClearsOldReport() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.startTour(); runCurrent()
            advanceTimeBy(3000); runCurrent()
            assertEquals(3, sdk.state.value.tourElapsedSeconds)
            advanceTimeBy(27000); runCurrent()
            assertTrue(sdk.state.value.tourReport)
            sdk.trigger(Scenario.KIDS_NOISE)
            assertFalse("a manual scene is not the previous tour report", sdk.state.value.tourReport)
        } finally { sdk.shutdown() }
    }

    @Test fun elapsedThresholdRearmsEvenWhenParentsSceneWasAlreadyActive() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.setElapsed(51)
            val first = sdk.state.value.eventId
            sdk.setElapsed(65)
            assertEquals(first, sdk.state.value.eventId)
            assertTrue(sdk.state.value.flow.inputSensors.any { it.currentValue == "65분" })
            sdk.setElapsed(40); sdk.setElapsed(52)
            assertTrue("second rising edge needs a new event", sdk.state.value.eventId > first)
        } finally { sdk.shutdown() }
    }

    @Test fun sensorTriggerBadgeReflectsTheCurrentReadingNotOldEvent() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.setNoise(82); sdk.setNoise(60)
            assertFalse(sdk.state.value.flow.inputSensors.first { it.sensorKey == "cabin_mic" }.isTriggered)
        } finally { sdk.shutdown() }
    }

    @Test fun initialStateHasFourIndependentZonesAndNavigationRoute() = runTest {
        val sdk = MockPleosSdkManager(this)
        assertEquals(SeatZone.entries.toSet(), sdk.state.value.seats.map { it.zone }.toSet())
        assertEquals(4, sdk.state.value.audio.size)
        assertTrue(sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L }.status.contains("내비"))
        sdk.shutdown()
    }

    @Test fun movingNoiseWithinTriggeredBandUpdatesReadingsWithoutNewNarration() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.setNoise(76)
            val firstEvent = sdk.state.value.eventId
            sdk.setNoise(89)
            val current = sdk.state.value
            assertEquals("same high-band reading must not replay TTS", firstEvent, current.eventId)
            assertEquals(89, current.seats.single { it.zone == SeatZone.KIDS_2ND_ROW }.noiseDb)
            assertTrue("IPO must show the actual live 89dB rather than the first trigger value",
                current.flow.inputSensors.any { it.currentValue.contains("89") })
        } finally { sdk.shutdown() }
    }

    @Test fun sleepCancelsWhisperAndAppliesTypedControls() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            val driver = sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L }
            sdk.trigger(Scenario.KIDS_NOISE)
            advanceTimeBy(200); runCurrent()
            sdk.trigger(Scenario.KIDS_SLEEP)
            advanceUntilIdle()
            val current = sdk.state.value
            val kids = current.seats.single { it.zone == SeatZone.KIDS_2ND_ROW }
            assertFalse(kids.ambientOn)
            assertEquals(22.5f, kids.temperature, 0.0f)
            assertEquals(1, kids.fanLevel)
            assertTrue(kids.windowLocked)
            assertEquals(0, current.audio.single { it.zone == SeatZone.KIDS_2ND_ROW }.volume)
            assertEquals("", current.flow.gleoTtsScript)
            assertNotNull(current.silentMessage)
            assertEquals(driver, current.audio.single { it.zone == SeatZone.DRIVER_1L })
        } finally { sdk.shutdown() }
    }

    @Test fun replayingSamePresetCreatesNewNarrationEvent() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.trigger(Scenario.KIDS_NOISE)
            assertEquals(82, sdk.state.value.noiseDb)
            val event = sdk.state.value.eventId
            sdk.trigger(Scenario.KIDS_NOISE)
            assertTrue(sdk.state.value.eventId > event)
        } finally { sdk.shutdown() }
    }

    @Test fun parentCarePersistsDuringPassengerConversation() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.trigger(Scenario.PARENTS)
            sdk.trigger(Scenario.PASSENGER)
            val parents = sdk.state.value.seats.single { it.zone == SeatZone.PARENTS_3RD_ROW }
            assertTrue(parents.active)
            assertEquals(1, parents.heatingLevel)
            assertTrue(parents.lumbarOn)
            assertTrue(sdk.state.value.restStopVisible)
            sdk.answerRestStop(true)
            assertEquals(true, sdk.state.value.restStopAccepted)
            assertTrue("accepted route remains visible as confirmation", sdk.state.value.restStopVisible)
        } finally { sdk.shutdown() }
    }

    @Test fun passengerDuckingRestoresWithoutChangingDriver() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            val driver = sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L }
            sdk.trigger(Scenario.PASSENGER)
            advanceUntilIdle()
            assertEquals(25, sdk.state.value.audio.single { it.zone == SeatZone.PASSENGER_1R }.volume)
            sdk.endConversation(); advanceUntilIdle()
            assertEquals(70, sdk.state.value.audio.single { it.zone == SeatZone.PASSENGER_1R }.volume)
            assertEquals(driver, sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L })
            sdk.toggleMode(); sdk.chooseSnack("호두과자")
            assertFalse(sdk.state.value.cozyMode)
            assertEquals("호두과자", sdk.state.value.snackChoice)
        } finally { sdk.shutdown() }
    }

    @Test fun tourUsesExactTimelineAndReportInterruptsPriorVoice() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.startTour(); runCurrent()
            assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
            assertEquals(82, sdk.state.value.noiseDb)
            advanceTimeBy(6999); runCurrent()
            assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
            advanceTimeBy(1); runCurrent()
            assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
            advanceTimeBy(7000); runCurrent()
            assertEquals(SeatZone.PASSENGER_1R, sdk.state.value.flow.targetZone)
            val priorEvent = sdk.state.value.eventId
            advanceTimeBy(7000); runCurrent()
            assertTrue(sdk.state.value.tourReport)
            assertEquals(SeatZone.DRIVER_1L, sdk.state.value.flow.targetZone)
            assertEquals("", sdk.state.value.flow.gleoTtsScript)
            assertTrue("new event tells TTS boundary to stop", sdk.state.value.eventId > priorEvent)
            advanceTimeBy(9000); runCurrent()
            assertFalse(sdk.state.value.tourRunning)
            assertEquals(30, sdk.state.value.tourElapsedSeconds)
        } finally { sdk.shutdown() }
    }

    @Test fun manualSliderCancelsPendingTourSteps() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.startTour(); runCurrent()
            advanceTimeBy(6000); runCurrent()
            sdk.setNoise(40)
            val event = sdk.state.value.eventId
            advanceTimeBy(31000); runCurrent()
            assertFalse(sdk.state.value.tourRunning)
            assertFalse(sdk.state.value.tourReport)
            assertEquals(event, sdk.state.value.eventId)
            assertEquals(0, sdk.state.value.seats.single { it.zone == SeatZone.PARENTS_3RD_ROW }.heatingLevel)
        } finally { sdk.shutdown() }
    }

    @Test fun resetCancelsPendingVolumeAndTourAndRestoresSnapshot() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.startTour(); runCurrent(); advanceTimeBy(100)
            val priorEvent = sdk.state.value.eventId
            sdk.reset(); advanceUntilIdle()
            assertTrue(sdk.state.value.eventId > priorEvent)
            assertEquals(FamilyCareUiState(eventId = sdk.state.value.eventId), sdk.state.value)
        } finally { sdk.shutdown() }
    }

    @Test fun thresholdsRequireAllCompanionConditions() {
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 75)))
        assertEquals(Scenario.KIDS_NOISE, ScenarioEngine.evaluate(Context(noiseDb = 76)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 82, tunnelMeters = 301)))
        assertNull(ScenarioEngine.evaluate(Context(elapsedMin = 50, pressureShift = true, oldRoadDistanceKm = 3.0)))
        assertNull(ScenarioEngine.evaluate(Context(elapsedMin = 51, oldRoadDistanceKm = 3.0)))
        assertEquals(Scenario.PARENTS, ScenarioEngine.evaluate(Context(elapsedMin = 51, pressureShift = true, oldRoadDistanceKm = 3.0)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 36, quietSeconds = 180, eyesClosed = true, breathingStable = true)))
        assertNull(ScenarioEngine.evaluate(Context(quietSeconds = 179, eyesClosed = true, breathingStable = true)))
        assertNull(ScenarioEngine.evaluate(Context(quietSeconds = 180, breathingStable = true)))
        assertEquals(Scenario.KIDS_SLEEP, ScenarioEngine.evaluate(Context(quietSeconds = 180, eyesClosed = true, breathingStable = true)))
        assertNull(ScenarioEngine.evaluate(Context(rain = true, lowLight = true)))
        assertEquals(Scenario.PASSENGER, ScenarioEngine.evaluate(Context(rain = true, lowLight = true, conversation = true)))
    }

    @Test fun slidersClampAndRearmWithoutRepeatedHighBandEvents() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.setNoise(101); assertEquals(100, sdk.state.value.noiseDb)
            val first = sdk.state.value.eventId
            sdk.setNoise(99); assertEquals(first, sdk.state.value.eventId)
            sdk.setNoise(0); assertEquals(30, sdk.state.value.noiseDb)
            sdk.setNoise(76); assertTrue(sdk.state.value.eventId > first)
            sdk.setElapsed(130); assertEquals(120, sdk.state.value.elapsedMin)
            sdk.setElapsed(-10); assertEquals(0, sdk.state.value.elapsedMin)
        } finally { sdk.shutdown() }
    }
}

@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.SeatZone
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockPleosSdkManagerTest {
    @Test fun sleepAndParentsPopulateTypedOutputs() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.trigger(Scenario.KIDS_SLEEP)
        val kids = sdk.state.value.seats.first { it.zone == SeatZone.KIDS_2ND_ROW }
        assertEquals(22.5f, kids.temperature); assertFalse(kids.ambientOn); assertEquals(1, kids.fanLevel); assertEquals(1L, sdk.state.value.sleepPulseId)
        sdk.setElapsed(51)
        assertTrue(sdk.state.value.restStopVisible)
        val parents = sdk.state.value.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }
        assertEquals(1, parents.heatingLevel); assertTrue(parents.lumbarOn)
        assertEquals("51분", sdk.state.value.flow.inputSensors.first { it.sensorKey == "elapsed" }.currentValue)
    }

    @Test fun noiseRisingEdgeRearmsAndDucks() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.setNoise(76); advanceUntilIdle()
        assertEquals(40, sdk.state.value.audio.first { it.zone == SeatZone.KIDS_2ND_ROW }.volume)
        val first = sdk.state.value.eventId
        sdk.setNoise(80); assertEquals(first, sdk.state.value.eventId)
        sdk.setNoise(70); sdk.setNoise(81); advanceUntilIdle()
        assertTrue(sdk.state.value.eventId > first)
    }

    @Test fun sliderUpdatesRetainedScenarioWithoutNewEvent() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.setNoise(76)
        val event = sdk.state.value.eventId
        sdk.setNoise(89)
        assertEquals(event, sdk.state.value.eventId)
        assertEquals(89, sdk.state.value.seats.single { it.zone == SeatZone.KIDS_2ND_ROW }.noiseDb)
        assertEquals("89dB", sdk.state.value.flow.inputSensors.single { it.sensorKey == "cabin_mic" }.currentValue)
        sdk.setElapsed(51)
        assertEquals("51분", sdk.state.value.flow.inputSensors.single { it.sensorKey == "elapsed" }.currentValue)
    }


    @Test fun passengerRestoresVolumeAndSnackState() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.trigger(Scenario.PASSENGER); advanceUntilIdle(); assertEquals(25, sdk.state.value.audio.first { it.zone == SeatZone.PASSENGER_1R }.volume)
        sdk.chooseSnack("호두과자"); sdk.endConversation(); advanceUntilIdle()
        assertEquals(70, sdk.state.value.audio.first { it.zone == SeatZone.PASSENGER_1R }.volume); assertEquals("호두과자", sdk.state.value.snackChoice)
    }

    @Test fun tourHasRequiredTimelineAndNoSleep() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(); runCurrent(); assertEquals(Scenario.KIDS_NOISE, sdk.state.value.flowTarget())
        advanceTimeBy(7_000); runCurrent(); assertEquals(Scenario.PARENTS, sdk.state.value.flowTarget())
        advanceTimeBy(7_000); runCurrent(); assertEquals(Scenario.PASSENGER, sdk.state.value.flowTarget())
        advanceTimeBy(7_000); runCurrent(); assertTrue(sdk.state.value.tourReport); assertEquals(21, sdk.state.value.tourElapsedSeconds)
        advanceTimeBy(9_000); runCurrent(); assertFalse(sdk.state.value.tourRunning); assertEquals(30, sdk.state.value.tourElapsedSeconds)
    }

    @Test fun stoppingTourCancelsNarrationAndPendingVolumeWorkWithoutChangingCurrentScenario() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour()
        advanceTimeBy(200)
        runCurrent()
        val beforeStop = sdk.state.value

        sdk.stopTour()
        runCurrent()
        val stopped = sdk.state.value
        assertFalse(stopped.tourRunning)
        assertFalse(stopped.storyPlaying)
        assertEquals("", stopped.flow.gleoTtsScript)
        assertEquals(beforeStop.eventId + 1, stopped.eventId)
        assertEquals(beforeStop.selectedScenario, stopped.selectedScenario)
        assertEquals(beforeStop.flow.activeScenarioTitle, stopped.flow.activeScenarioTitle)
        assertEquals(beforeStop.seats, stopped.seats)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(stopped.audio, sdk.state.value.audio)
        val eventAfterFirstStop = sdk.state.value.eventId
        sdk.stopTour()
        assertEquals(eventAfterFirstStop, sdk.state.value.eventId)
    }

    private fun com.pleos.dearfamily.model.FamilyCareUiState.flowTarget(): Scenario? = when (flow.targetZone) { SeatZone.PARENTS_3RD_ROW -> Scenario.PARENTS; SeatZone.PASSENGER_1R -> Scenario.PASSENGER; SeatZone.KIDS_2ND_ROW -> Scenario.KIDS_NOISE; else -> null }
}

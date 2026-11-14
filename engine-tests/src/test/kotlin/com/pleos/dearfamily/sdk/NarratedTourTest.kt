@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.model.NarrationOutcome
import com.pleos.dearfamily.model.SeatZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class NarratedTourTest {
    @Test
    fun narratedTourKeepsSceneForSevenSecondsAndWaitsForCompletion() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val noiseEvent = sdk.state.value.eventId

        advanceTimeBy(7_000); runCurrent()
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
        assertEquals(noiseEvent, sdk.state.value.eventId)

        advanceTimeBy(5_000); runCurrent()
        assertEquals(12, sdk.state.value.tourElapsedSeconds)
        sdk.onNarrationFeedback(noiseEvent, NarrationOutcome.COMPLETED)
        runCurrent()
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun earlyCompletionStillKeepsMinimumSevenSeconds() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val event = sdk.state.value.eventId
        sdk.onNarrationFeedback(event, NarrationOutcome.COMPLETED)
        runCurrent()
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
        advanceTimeBy(6_999); runCurrent()
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
        advanceTimeBy(1); runCurrent()
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun failedOrUnavailableFeedbackAdvancesAfterMinimum() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val event = sdk.state.value.eventId
        advanceTimeBy(7_000); runCurrent()
        sdk.onNarrationFeedback(event, NarrationOutcome.FAILED)
        runCurrent()
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun cancelledFeedbackStopsTourAndIgnoresLaterCallbacks() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val event = sdk.state.value.eventId
        sdk.onNarrationFeedback(event, NarrationOutcome.CANCELLED)
        runCurrent()
        assertFalse(sdk.state.value.tourRunning)
        sdk.onNarrationFeedback(event, NarrationOutcome.COMPLETED)
        advanceTimeBy(40_000); runCurrent()
        assertFalse(sdk.state.value.tourRunning)
    }

    @Test
    fun staleAndDuplicateFeedbackDoNotAdvanceNewScene() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val first = sdk.state.value.eventId
        advanceTimeBy(7_000); runCurrent()
        sdk.onNarrationFeedback(first, NarrationOutcome.COMPLETED)
        runCurrent()
        val second = sdk.state.value.eventId
        sdk.onNarrationFeedback(first, NarrationOutcome.COMPLETED)
        sdk.onNarrationFeedback(second + 100, NarrationOutcome.COMPLETED)
        runCurrent()
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun manualStopAndRestartInvalidateOldCallbacks() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val oldEvent = sdk.state.value.eventId
        sdk.stopTour()
        sdk.startTour(awaitNarration = true)
        val newEvent = sdk.state.value.eventId
        sdk.onNarrationFeedback(oldEvent, NarrationOutcome.COMPLETED)
        runCurrent()
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
        assertNotEquals(oldEvent, newEvent)
    }

    @Test fun resetNeverReusesNarrationIdsForDelayedCompletion() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val oldEvent = sdk.state.value.eventId
        sdk.reset()
        sdk.startTour(awaitNarration = true)
        val newEvent = sdk.state.value.eventId
        advanceTimeBy(7_000); runCurrent()
        sdk.onNarrationFeedback(oldEvent, NarrationOutcome.COMPLETED)
        runCurrent()
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
        assertNotEquals(oldEvent, newEvent)
        sdk.stopTour()
    }

    @Test fun feedbackAtDeadlineStopsInsteadOfWinningTheTimerRace() = runTest {
        val sdk = MockPleosSdkManager(this, monotonicMillis = { testScheduler.currentTime })
        sdk.startTour(awaitNarration = true)
        val event = sdk.state.value.eventId
        advanceTimeBy(30_000)
        sdk.onNarrationFeedback(event, NarrationOutcome.COMPLETED)
        runCurrent()
        assertFalse(sdk.state.value.tourRunning)
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
    }

    @Test fun unavailableFeedbackAdvancesAfterMinimum() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        val event = sdk.state.value.eventId
        advanceTimeBy(7_000); runCurrent()
        sdk.onNarrationFeedback(event, NarrationOutcome.UNAVAILABLE)
        runCurrent()
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun narrationTimeoutStopsTourInsteadOfForcingNextScene() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.startTour(awaitNarration = true)
        advanceTimeBy(30_000); runCurrent()
        assertFalse(sdk.state.value.tourRunning)
        assertEquals(SeatZone.KIDS_2ND_ROW, sdk.state.value.flow.targetZone)
    }
}

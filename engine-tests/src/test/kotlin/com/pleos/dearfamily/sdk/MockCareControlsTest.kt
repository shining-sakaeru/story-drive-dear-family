@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.domain.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockCareControlsTest {
    @Test
    fun storyPlaybackIsIndependentAndReversibleWithoutChangingCozyMode() = runTest {
        val sdk = MockPleosSdkManager(this)
        val initialCozy = sdk.state.value.cozyMode

        sdk.toggleStoryPlayback()
        assertTrue(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.cozyMode == initialCozy)

        sdk.toggleStoryPlayback()
        assertFalse(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.cozyMode == initialCozy)
    }

    @Test
    fun quietActivationStopsStoryAndQuietOffDoesNotResumeIt() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.toggleStoryPlayback()
        assertTrue(sdk.state.value.storyPlaying)

        sdk.toggleQuietCabin()
        assertTrue(sdk.state.value.quietCabin)
        assertFalse(sdk.state.value.storyPlaying)

        sdk.toggleQuietCabin()
        assertFalse(sdk.state.value.quietCabin)
        assertFalse(sdk.state.value.storyPlaying)
    }

    @Test
    fun storyCannotStartDuringQuietCabinOrKidsSleep() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.toggleQuietCabin()
        sdk.toggleStoryPlayback()
        assertFalse(sdk.state.value.storyPlaying)

        sdk.toggleQuietCabin()
        sdk.trigger(Scenario.KIDS_SLEEP)
        sdk.toggleStoryPlayback()
        assertFalse(sdk.state.value.storyPlaying)
    }

    @Test
    fun shutdownStopsStoryPlayback() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.toggleStoryPlayback()
        assertTrue(sdk.state.value.storyPlaying)
        sdk.shutdown()
        assertFalse(sdk.state.value.storyPlaying)
    }

    @Test
    fun resetClearsCareFlags() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.toggleStoryPlayback()
        sdk.toggleCabinWarmth()
        sdk.toggleQuietCabin()
        sdk.reset()

        assertFalse(sdk.state.value.storyPlaying)
        assertFalse(sdk.state.value.cabinWarmth)
        assertFalse(sdk.state.value.quietCabin)
    }

    @Test
    fun warmthIsIndependentOfCozyAndDoesNotStartStory() = runTest {
        val sdk = MockPleosSdkManager(this)
        val initialCozy = sdk.state.value.cozyMode
        val initialEvent = sdk.state.value.eventId

        sdk.toggleCabinWarmth()
        advanceUntilIdle()

        assertTrue(sdk.state.value.cabinWarmth)
        assertTrue(sdk.state.value.cozyMode == initialCozy)
        assertFalse(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.eventId == initialEvent)
    }

    @Test
    fun careTogglesCancelAnActiveTour() = runTest {
        val toggles = listOf<(MockPleosSdkManager) -> Unit>(
            { it.toggleStoryPlayback() },
            { it.toggleCabinWarmth() },
            { it.toggleQuietCabin() }
        )
        toggles.forEach { toggle ->
            val sdk = MockPleosSdkManager(this)
            sdk.startTour()
            runCurrent()
            assertTrue(sdk.state.value.tourRunning)
            toggle(sdk)
            assertFalse(sdk.state.value.tourRunning)
        }
    }
}

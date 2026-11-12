package com.pleos.dearfamily.sdk

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** UI story intent must not reuse the passenger Cozy/Fun action. */
class StoryActionTest {
    @Test fun storyStartStopLeavesPassengerModeAlone() = runTest {
        val sdk = MockPleosSdkManager(this)
        assertFalse(sdk.state.value.storyPlaying)
        sdk.toggleStoryPlayback()
        assertTrue(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.cozyMode)
        sdk.toggleStoryPlayback()
        assertFalse(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.cozyMode)
    }
}

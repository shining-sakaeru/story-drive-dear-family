package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.model.CareTab
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SpatialActionTest {
    @Test fun selectingDockChangesOnlyUiSelectionNotPlayback() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.selectTab(CareTab.MEDIA)
        assertEquals(CareTab.MEDIA, sdk.state.value.selectedTab)
        assertFalse(sdk.state.value.storyPlaying)
        assertEquals(0L, sdk.state.value.eventId)
    }
}

package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.policy.SafetyState
import org.junit.Assert.*
import org.junit.Test

class SafetyPolicyTest {
    @Test fun ordinaryDemoNeedsExplicitParkedOptIn() {
        val state = SafetyState(foreground = true)
        assertFalse(state.canInteract)
        assertTrue(state.copy(demoOptIn = true).canInteract)
    }
    @Test fun realFlavorCannotUseTabletDemoBypass() {
        assertFalse(SafetyState(demoBuild = false, foreground = true, demoOptIn = true).canInteract)
    }
    @Test fun knownParkedStateStillRequiresForegroundAndNoSuspend() {
        val parked = SafetyState(automotive = true, demoBuild = false, foreground = true,
            platformKnown = true, requiresOptimization = false)
        assertTrue(parked.canInteract)
        assertFalse(parked.copy(foreground = false).canPlayAudio)
        assertFalse(parked.copy(suspended = true).canInteract)
        assertFalse(parked.copy(requiresOptimization = true).canInteract)
        assertFalse(parked.copy(activeRestrictions = 1).canInteract)
    }
    @Test fun unknownAutomotiveStateCannotBeUnlockedByDemoOptIn() {
        val unknown = SafetyState(automotive = true, demoBuild = true, foreground = true,
            demoOptIn = true)
        assertFalse(unknown.canInteract)
        assertFalse(unknown.canPlayAudio)
    }
}

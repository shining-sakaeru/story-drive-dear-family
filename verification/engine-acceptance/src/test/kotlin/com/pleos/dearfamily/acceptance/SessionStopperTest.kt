package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.policy.SessionStopper
import org.junit.Assert.*
import org.junit.Test

class SessionStopperTest {
    @Test fun everySubscriptionIsReleasedEvenWhenAnotherCleanupFails() {
        val calls = mutableListOf<String>()
        val stopper = SessionStopper(listOf(
            { calls += "mock"; throw IllegalStateException("cleanup failure") },
            { calls += "ux" },
            { calls += "vehicle" }
        ))
        assertFalse(stopper.stop())
        assertEquals(listOf("mock", "ux", "vehicle"), calls)
    }
}

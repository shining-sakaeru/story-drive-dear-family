package com.pleos.dearfamily.policy.audio

import com.pleos.dearfamily.model.NarrationFeedback
import com.pleos.dearfamily.model.NarrationOutcome
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class NarrationFeedbackTrackerTest {
    @Test
    fun completionIsEmittedAtMostOnceForApplicationEvent() {
        val tracker = NarrationFeedbackTracker()
        assertNull(tracker.finish(7L, NarrationOutcome.COMPLETED))
        tracker.begin(7L)
        assertEquals(NarrationFeedback(7L, NarrationOutcome.COMPLETED), tracker.finish(7L, NarrationOutcome.COMPLETED))
        assertNull(tracker.finish(7L, NarrationOutcome.FAILED))
    }

    @Test
    fun replacingAnActiveRequestCancelsOnlyThePreviousEvent() {
        val tracker = NarrationFeedbackTracker()
        tracker.begin(11L)
        assertEquals(listOf(NarrationFeedback(11L, NarrationOutcome.CANCELLED)), tracker.begin(12L))
        assertEquals(NarrationFeedback(12L, NarrationOutcome.CANCELLED), tracker.cancel())
        assertNull(tracker.cancel())
    }

    @Test
    fun staleVendorTokenCannotFinishTheCurrentApplicationEvent() {
        val tracker = NarrationFeedbackTracker()
        tracker.begin(21L)
        tracker.bind(4L)
        tracker.begin(22L)
        tracker.bind(5L)
        assertNull(tracker.finishToken(4L, NarrationOutcome.COMPLETED))
        assertEquals(NarrationFeedback(22L, NarrationOutcome.FAILED), tracker.finishToken(5L, NarrationOutcome.FAILED))
    }

    @Test
    fun resumingTheSamePendingEventDoesNotCancelOrBeginItAgain() {
        val tracker = NarrationFeedbackTracker()
        assertEquals(emptyList<NarrationFeedback>(), tracker.begin(31L))
        assertEquals(NarrationFeedback(31L, NarrationOutcome.COMPLETED), tracker.finishCurrent(NarrationOutcome.COMPLETED))
        assertNull(tracker.finishCurrent(NarrationOutcome.FAILED))
    }

    @Test
    fun everyTerminalOutcomeIsEmittedOnce() {
        for (outcome in NarrationOutcome.entries) {
            val tracker = NarrationFeedbackTracker()
            tracker.begin(outcome.ordinal.toLong())
            assertEquals(NarrationFeedback(outcome.ordinal.toLong(), outcome), tracker.finishCurrent(outcome))
            assertNull(tracker.finishCurrent(NarrationOutcome.CANCELLED))
        }
    }
}

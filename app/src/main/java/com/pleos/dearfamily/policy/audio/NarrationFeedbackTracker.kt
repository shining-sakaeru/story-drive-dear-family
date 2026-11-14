package com.pleos.dearfamily.policy.audio

import com.pleos.dearfamily.model.NarrationFeedback
import com.pleos.dearfamily.model.NarrationOutcome

/** Keeps application event identity separate from vendor/session callback tokens. */
class NarrationFeedbackTracker {
    private var activeEventId: Long? = null
    private var activeToken: Long? = null

    fun begin(eventId: Long): List<NarrationFeedback> {
        val cancelled = activeEventId?.let { NarrationFeedback(it, NarrationOutcome.CANCELLED) }
        activeEventId = eventId
        activeToken = null
        return cancelled?.let(::listOf) ?: emptyList()
    }

    fun bind(token: Long) { activeToken = token }

    fun finishToken(token: Long, outcome: NarrationOutcome): NarrationFeedback? {
        if (activeToken != token) return null
        activeToken = null
        return finishCurrent(outcome)
    }

    fun finish(eventId: Long, outcome: NarrationOutcome): NarrationFeedback? {
        if (activeEventId != eventId) return null
        activeEventId = null
        activeToken = null
        return NarrationFeedback(eventId, outcome)
    }

    fun finishCurrent(outcome: NarrationOutcome): NarrationFeedback? = activeEventId?.let {
        activeEventId = null
        activeToken = null
        NarrationFeedback(it, outcome)
    }

    fun cancel(): NarrationFeedback? = activeEventId?.let {
        activeEventId = null
        activeToken = null
        NarrationFeedback(it, NarrationOutcome.CANCELLED)
    }
}

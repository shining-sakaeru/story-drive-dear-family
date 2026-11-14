package com.pleos.dearfamily.model

/** App event identity, not a vendor callback token or proof of physical listening. */
enum class NarrationOutcome { COMPLETED, FAILED, UNAVAILABLE, CANCELLED }

data class NarrationFeedback(val eventId: Long, val outcome: NarrationOutcome)

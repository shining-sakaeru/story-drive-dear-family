package com.pleos.dearfamily.policy

/** No speed label, seat noise or local scenario can grant driving permission. */
data class SafetyState(
    val automotive: Boolean = false,
    val demoBuild: Boolean = true,
    val foreground: Boolean = false,
    val suspended: Boolean = false,
    val platformKnown: Boolean = false,
    val requiresOptimization: Boolean = true,
    val activeRestrictions: Int = 0,
    val demoOptIn: Boolean = false,
    val sourceMessage: String = "차량 UX 제한 상태 확인 전 · 안전 제한"
) {
    val canInteract: Boolean get() = foreground && !suspended &&
        if (automotive || !demoBuild) platformKnown && !requiresOptimization && activeRestrictions == 0
        else demoOptIn
    // Conservative project policy: family-demo speech is never auto-started in restricted UX.
    val canPlayAudio: Boolean get() = canInteract
}

package com.pleos.dearfamily.policy

/** One shutdown contract shared by background and display-suspend paths. */
class SessionStopper(private val actions: List<() -> Unit>) {
    fun stop(): Boolean {
        var complete = true
        actions.forEach { action ->
            try { action() } catch (_: Exception) { complete = false }
        }
        return complete
    }
}

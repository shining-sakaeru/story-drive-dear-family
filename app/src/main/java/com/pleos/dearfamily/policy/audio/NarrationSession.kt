package com.pleos.dearfamily.policy.audio

interface NarrationBackend {
    fun synthesize(token: Long, text: String)
    fun requestFocus(): Boolean
    fun play(token: Long)
    fun cancel()
    fun abandonFocus()
    fun setVolume(volume: Float)
}
enum class PlaybackPhase { IDLE, SYNTHESIZING, PREPARING, PLAYING, BLOCKED, DENIED, ERROR }
enum class FocusEvent { GAIN, LOSS, TRANSIENT_LOSS, DUCK }

/** Serialized by the Android main loop. Every callback carries a generation token. */
class NarrationSession(private val output: NarrationBackend, private val changed: (PlaybackPhase) -> Unit = {}) {
    var phase = PlaybackPhase.IDLE
        private set
    var token = 0L
        private set
    private var allowed = false
    private var muted = false
    private var focusHeld = false

    fun setAllowed(value: Boolean) {
        allowed = value
        if (!value) stop(PlaybackPhase.BLOCKED)
    }
    fun setMuted(value: Boolean) {
        muted = value
        if (value) stop()
    }
    fun speak(text: String): Long {
        stop()
        if (!allowed || muted || text.isBlank()) {
            transition(if (!allowed) PlaybackPhase.BLOCKED else PlaybackPhase.IDLE)
            return token
        }
        transition(PlaybackPhase.SYNTHESIZING)
        try { output.synthesize(token, text) } catch (_: Exception) { stop(PlaybackPhase.ERROR) }
        return token
    }
    fun onSynthesized(generation: Long) {
        if (generation != token || !allowed || muted || phase != PlaybackPhase.SYNTHESIZING) return
        try {
            if (!output.requestFocus()) { stop(PlaybackPhase.DENIED); return }
            focusHeld = true
            transition(PlaybackPhase.PREPARING)
            output.play(token)
        } catch (_: Exception) { stop(PlaybackPhase.ERROR) }
    }
    fun onPlaying(generation: Long) {
        if (generation == token && allowed && focusHeld && phase == PlaybackPhase.PREPARING)
            transition(PlaybackPhase.PLAYING)
    }
    fun onFinished(generation: Long, error: Boolean = false) {
        if (generation == token) stop(if (error) PlaybackPhase.ERROR else PlaybackPhase.IDLE)
    }
    fun onFocusChange(event: FocusEvent) {
        when (event) {
            FocusEvent.LOSS, FocusEvent.TRANSIENT_LOSS -> stop()
            FocusEvent.DUCK -> if (focusHeld) output.setVolume(.2f)
            FocusEvent.GAIN -> if (allowed && focusHeld) output.setVolume(1f)
        }
    }
    fun stop(finalPhase: PlaybackPhase = PlaybackPhase.IDLE) {
        token++ // Invalidate before cancel: synchronous stop callbacks must be stale.
        try { output.cancel() } finally {
            if (focusHeld) {
                focusHeld = false
                output.abandonFocus()
            }
            transition(finalPhase)
        }
    }
    private fun transition(value: PlaybackPhase) { phase = value; changed(value) }
}

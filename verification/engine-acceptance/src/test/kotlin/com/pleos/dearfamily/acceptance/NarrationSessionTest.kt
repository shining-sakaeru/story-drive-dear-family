package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.policy.audio.NarrationBackend
import com.pleos.dearfamily.policy.audio.NarrationSession
import com.pleos.dearfamily.policy.audio.PlaybackPhase
import com.pleos.dearfamily.policy.audio.FocusEvent
import org.junit.Assert.*
import org.junit.Test

class NarrationSessionTest {
    @Test fun restrictedStateAndUnmuteDoNotStartHiddenPlayback() {
        val output = Output()
        val session = NarrationSession(output)
        session.speak("재생 금지")
        assertFalse(output.calls.any { it.startsWith("synthesize:") })
        session.setAllowed(true); session.setMuted(true); session.speak("음소거")
        session.setMuted(false)
        assertFalse(output.calls.any { it.startsWith("synthesize:") })
    }
    @Test fun staleSynthesisCannotAcquireFocusOrReplayAfterReplacement() {
        val output = Output()
        val session = NarrationSession(output)
        session.setAllowed(true)
        val old = session.speak("지난 이벤트")
        val current = session.speak("현재 이벤트")
        session.onSynthesized(old)
        assertFalse(output.calls.contains("focus"))
        session.onSynthesized(current)
        assertTrue(output.calls.contains("play:$current"))
        session.onPlaying(current)
        assertEquals(PlaybackPhase.PLAYING, session.phase)
        session.onFinished(old)
        assertEquals(PlaybackPhase.PLAYING, session.phase)
    }
    @Test fun focusDuckRestoresAndLossStopsWithoutAutomaticResume() {
        val output = Output()
        val session = NarrationSession(output)
        session.setAllowed(true)
        val token = session.speak("이야기")
        session.onSynthesized(token); session.onPlaying(token)
        session.onFocusChange(FocusEvent.DUCK)
        assertTrue(output.calls.contains("volume:0.2"))
        session.onFocusChange(FocusEvent.GAIN)
        assertTrue(output.calls.contains("volume:1.0"))
        session.onFocusChange(FocusEvent.TRANSIENT_LOSS)
        assertEquals(PlaybackPhase.IDLE, session.phase)
        assertTrue(output.calls.contains("abandon"))
        val plays = output.calls.count { it.startsWith("play:") }
        session.onFocusChange(FocusEvent.GAIN)
        session.onSynthesized(token)
        assertEquals(plays, output.calls.count { it.startsWith("play:") })
    }
    @Test fun suspendInvalidatesAllCallbacksAndReleasesFocus() {
        val output = Output()
        val session = NarrationSession(output)
        session.setAllowed(true)
        val token = session.speak("가족 케어")
        session.onSynthesized(token); session.onPlaying(token)
        session.setAllowed(false)
        assertTrue(output.calls.contains("abandon"))
        assertEquals(PlaybackPhase.BLOCKED, session.phase)
        session.setAllowed(true)
        session.onPlaying(token)
        assertNotEquals(PlaybackPhase.PLAYING, session.phase)
    }
    private class Output(var grant: Boolean = true) : NarrationBackend {
        val calls = mutableListOf<String>()
        override fun synthesize(token: Long, text: String) { calls += "synthesize:$token" }
        override fun requestFocus(): Boolean { calls += "focus"; return grant }
        override fun play(token: Long) { calls += "play:$token" }
        override fun cancel() { calls += "cancel" }
        override fun abandonFocus() { calls += "abandon" }
        override fun setVolume(volume: Float) { calls += "volume:$volume" }
    }

    @Test fun rejectedFocusNeverStartsPlayback() {
        val output = Output(grant = false)
        val session = NarrationSession(output)
        session.setAllowed(true)
        val token = session.speak("이야기")
        session.onSynthesized(token)
        assertFalse(output.calls.any { it.startsWith("play:") })
        assertEquals(PlaybackPhase.DENIED, session.phase)
    }
}

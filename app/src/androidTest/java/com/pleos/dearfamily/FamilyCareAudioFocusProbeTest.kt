package com.pleos.dearfamily

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.SystemClock
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.pleos.dearfamily.platform.AudioNarrator
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Real platform focus clients in this app; not a navigation-app or acoustic test. */
class FamilyCareAudioFocusProbeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @Test fun platformFocusLossAndAllowanceRevocationStopWithoutAutomaticReplay() {
        val context = compose.activity.applicationContext
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val evidence = JSONObject().put("physicalListeningVerified", false)
            .put("competingClientScope", "Second real AudioManager focus client in same app")
        val competing = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setOnAudioFocusChangeListener { }.build()
        var narrator: AudioNarrator? = null
        fun onMain(action: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(action)
        val script = "안전한 음성 중지와 재생을 확인하는 안내입니다. 잠깐 편안히 기다려 주세요. ".repeat(4)
        try {
            val permitted = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java].safety.value.canPlayAudio
            evidence.put("safetyCanPlayAudio", permitted)
            assumeTrue("Actual parked foreground safety must permit audio", permitted)
            onMain { narrator = AudioNarrator(context); narrator!!.setAllowed(true) }
            compose.waitUntil(20_000) { narrator!!.status.value.ready }
            onMain { narrator!!.speak(1L, script) }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            var result = AudioManager.AUDIOFOCUS_REQUEST_FAILED
            onMain { result = audio.requestAudioFocus(competing) }
            evidence.put("competingFocusResult", result)
            assertEquals("Competing real focus client must be granted", AudioManager.AUDIOFOCUS_REQUEST_GRANTED, result)
            compose.waitUntil(3_000) { !narrator!!.status.value.speaking }
            evidence.put("focusLossStopsPlayback", true)
            onMain { audio.abandonAudioFocusRequest(competing) }
            SystemClock.sleep(900)
            assertFalse("Focus recovery must not automatically replay", narrator!!.status.value.speaking)
            evidence.put("focusRecoveryDoesNotReplay", true)

            onMain { narrator!!.speak(2L, script) }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            onMain { narrator!!.setAllowed(false) }
            assertFalse("Revoked safety allowance stops playback", narrator!!.status.value.speaking)
            onMain { narrator!!.setAllowed(true) }
            SystemClock.sleep(900)
            assertFalse("Restoring allowance must not automatically replay", narrator!!.status.value.speaking)
            evidence.put("allowanceRevocationStopsPlayback", true).put("allowanceRecoveryDoesNotReplay", true)

            onMain { narrator!!.speak(3L, script) }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            onMain { narrator!!.release() }
            SystemClock.sleep(900)
            assertFalse("Released narrator ignores late playback callbacks", narrator!!.status.value.speaking)

            evidence.put("releaseStopsPlayback", true).put("state", "verified_software_callbacks")
        } catch (error: Throwable) {
            evidence.put("error", error.toString())
            throw error
        } finally {
            onMain { audio.abandonAudioFocusRequest(competing); narrator?.release() }
            File(context.filesDir, "voice-focus-probe.json").writeText(evidence.toString(2), Charsets.UTF_8)
        }
    }
}

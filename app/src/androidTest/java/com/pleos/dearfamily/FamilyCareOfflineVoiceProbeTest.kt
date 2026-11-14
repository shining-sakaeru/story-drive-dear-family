package com.pleos.dearfamily


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.pleos.dearfamily.platform.AudioNarrator
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Real SDK callbacks are software evidence, not a physical-listening claim. */
class FamilyCareOfflineVoiceProbeTest {
    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun probeOfflineVoiceFromRealSafetyState() {
        val output = File(compose.activity.filesDir, "voice-showcase-probe.json")
        val evidence = JSONObject().put("physicalListeningVerified", false)
        var narrator: AudioNarrator? = null
        fun onMain(action: () -> Unit) =
            InstrumentationRegistry.getInstrumentation().runOnMainSync(action)
        try {
            val permissions = compose.activity.packageManager.getPackageInfo(
                compose.activity.packageName, android.content.pm.PackageManager.GET_PERMISSIONS
            ).requestedPermissions.orEmpty()
            evidence.put("appInternetPermissionDeclared", "android.permission.INTERNET" in permissions)
            assertFalse("Offline app must not inherit INTERNET from a multimode SDK",
                "android.permission.INTERNET" in permissions)
            var permitted = false
            onMain {
                permitted = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]
                    .safety.value.canPlayAudio
            }
            evidence.put("safetyCanPlayAudio", permitted).put("state", "blocked")
            assumeTrue("Actual platform must permit parked foreground audio", permitted)
            onMain {
                narrator = AudioNarrator(compose.activity.applicationContext)
                narrator!!.setAllowed(permitted)
            }
            compose.waitUntil(20_000) { narrator!!.status.value.ready }
            val readyStatus = narrator!!.status.value
            val pleosAdapterAvailable = runCatching {
                Class.forName("com.pleos.dearfamily.platform.PleosOfflineSpeechAdapter")
            }.isSuccess
            val backend = if (pleosAdapterAvailable) "Pleos ON_DEVICE" else "AndroidTTS"
            evidence.put("packagedPleosAdapter", pleosAdapterAvailable)
                .put("expectedBackend", backend)
                .put("initializationReadyObserved", true)
                .put("readyStatusMessage", readyStatus.message)
            if (pleosAdapterAvailable) {
                assertTrue(
                    "Pleos ready status must identify the ON_DEVICE backend: ${readyStatus.message}",
                    readyStatus.message.contains("Pleos ON_DEVICE")
                )
            }
            onMain { narrator!!.speak(1L, "같은 차 안에서, 우리 가족 함께 쉬어 가요.") }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            evidence.put("playbackStartObserved", true)
            compose.waitUntil(15_000) { !narrator!!.status.value.speaking }
            assertTrue("Voice must remain usable after playback", narrator!!.status.value.ready)
            assertFalse("Playback must not end as an error", narrator!!.status.value.message.contains("오류"))
            evidence.put("playbackFinishedObserved", true)

            onMain { narrator!!.speak(2L, "이번 안내는 중지 동작을 확인하는 문장입니다.") }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            onMain { narrator!!.stop() }
            compose.waitUntil(3_000) { !narrator!!.status.value.speaking }
            evidence.put("explicitStopObserved", true)

            onMain { narrator!!.toggleMute(); narrator!!.speak(3L, "음소거 중에는 들리지 않아야 합니다.") }
            android.os.SystemClock.sleep(600)
            assertTrue(narrator!!.status.value.muted)
            assertFalse("Mute must block a new request and late callbacks", narrator!!.status.value.speaking)
            evidence.put("muteBlocksReplayObserved", true).put("state", "verified_software_path")
        } catch (error: Throwable) {
            evidence.put("error", error.toString())
            throw error
        } finally {
            narrator?.status?.value?.let {
                evidence.put("ready", it.ready).put("speaking", it.speaking)
                    .put("muted", it.muted).put("message", it.message)
            }
            onMain { narrator?.release() }
            output.writeText(evidence.toString(2), Charsets.UTF_8)
        }
    }
}

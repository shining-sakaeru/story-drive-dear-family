package com.pleos.dearfamily

import android.graphics.Bitmap

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Opt-in real-time footage; host emulator recorder captures actual mixed audio. */
class FamilyCareShowcaseRecordingTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val frames = JSONArray()

    private fun node(tag: String): SemanticsNodeInteraction {
        compose.waitUntil(8_000) {
            runCatching { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }.getOrDefault(false)
        }
        return compose.onNodeWithTag(tag).also {
            runCatching { it.performScrollTo() }
            it.assertIsDisplayed()
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        // PixelCopy can stall while the emulator encoder owns the surface.
        // UiAutomation captures real display pixels without pausing live speech.
        val image = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val directory = File(compose.activity.filesDir, "showcase-media").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        val model = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]
        frames.put(JSONObject().put("file", "$name.png").put("width", image.width)
            .put("height", image.height).put("wallTimeMillis", System.currentTimeMillis())
            .put("scenario", model.state.value.selectedScenario)
            .put("script", model.state.value.flow.gleoTtsScript))
        image.recycle()
    }

    @Test fun recordThreeScenesWithActualAppSpeechAndSilence() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue("Recording is opt-in, not a functional pass", args.getString("recordShowcase") == "true")
        awaitDearFamilyWindow(compose)
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) node("enterDemo").performClick()
        node("openFamilyJourney").performClick()
        Thread.sleep(1_000)
        compose.waitUntil(20_000) {
            runCatching {
                compose.onNodeWithTag("journeyVoice").fetchSemanticsNode()
                    .config[SemanticsProperties.Text].none { it.text.contains("오류/준비 전") }
            }.getOrDefault(false)
        }
        val readyStatusMessage = compose.onNodeWithTag("journeyVoice").fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString { it.text }
        val expectedBackend = if (runCatching {
            Class.forName("com.pleos.dearfamily.platform.PleosOfflineSpeechAdapter")
        }.isSuccess) "Pleos ON_DEVICE" else "AndroidTTS"
        capture("journey-ready")

        node("journeyNoise").performClick()
        node("journeyCaption")
        compose.waitUntil(10_000) {
            runCatching {
                compose.onNodeWithTag("journeyVoice").fetchSemanticsNode()
                    .config[SemanticsProperties.Text].any { it.text.contains("재생 중") }
            }.getOrDefault(false)
        }
        capture("journey-noise")
        Thread.sleep(2_000)

        node("journeySleep").performClick()
        node("journeyVoice").assertTextContains("무음", substring = true)
        capture("journey-sleep")
        Thread.sleep(4_000)

        node("journeyParents").performClick()
        node("journeyCaption")
        capture("journey-parents")
        Thread.sleep(10_000)
        node("journeyRestAccept").performClick()
        val model = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]
        assertEquals(true, model.state.value.restStopAccepted)
        capture("journey-rest-accepted")
        Thread.sleep(2_000)

        node("journeyDetails").performClick()
        node("journeyDetailsClose")
        capture("journey-details")
        Thread.sleep(3_000)
        node("journeyDetailsClose").performClick()
        node("journeyStop").performClick()
        Thread.sleep(2_000)
        val config = compose.activity.resources.configuration
        File(compose.activity.filesDir, "showcase-media/metadata.json").writeText(
            JSONObject().put("sourceRevision", args.getString("sourceRevision"))
                .put("density", compose.activity.resources.displayMetrics.density)
                .put("fontScale", config.fontScale).put("frames", frames)
                .put("imageScope", "Actual full native display pixels, including OS-owned chrome")
                .put("inputsAndVehicleControlsMock", true)
                .put("audioSource", "Live app narration; no editing voiceover")
                .put("expectedBackend", expectedBackend)
                .put("readyStatusMessage", readyStatusMessage)
                .put("physicalListeningVerified", false).toString(2), Charsets.UTF_8)
    }
}

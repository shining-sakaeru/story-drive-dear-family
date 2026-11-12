package com.pleos.dearfamily

import android.speech.tts.TextToSpeech
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Device probes for platform contracts; no fake car provider or network voice is used. */
class FamilyCarePlatformAcceptanceTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun model(): DearFamilyViewModel =
        ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun openDebugBadgeAfterDemo() {
        if (compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isEmpty()) {
            compose.onNodeWithTag("debugBadge").performClick()
        }
    }

    private fun enterDemoIfAvailable() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        openDebugBadgeAfterDemo()
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("tour").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun probeNativeTtsCapabilitiesToFilesDir() {
        val context = compose.activity.applicationContext
        val initialized = CountDownLatch(1)
        var result = Int.MIN_VALUE
        var engineName: String? = null
        var koreanVoices: List<Map<String, Any?>> = emptyList()
        val tts = TextToSpeech(context) { status ->
            result = status
            initialized.countDown()
        }

        assertTrue("TextToSpeech initialization callback timed out", initialized.await(5, TimeUnit.SECONDS))
        engineName = tts.defaultEngine
        if (result == TextToSpeech.SUCCESS) {
            koreanVoices = tts.voices.orEmpty()
                .filter { it.locale.language == Locale.KOREAN.language }
                .sortedBy { it.name }
                .map { voice ->
                    mapOf(
                        "name" to voice.name,
                        "locale" to voice.locale.toLanguageTag(),
                        "isNetworkConnectionRequired" to voice.isNetworkConnectionRequired,
                        "features" to voice.features.orEmpty().sorted()
                    )
                }
        }
        val offlineKoreanVoices = koreanVoices.filter {
            it["isNetworkConnectionRequired"] == false &&
                !(it["features"] as List<*>).contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
        }
        val output = File(context.filesDir, "native-tts-capabilities.json")
        output.writeText(
            "{" +
                "\"initializationResult\":$result," +
                "\"initializationSucceeded\":${result == TextToSpeech.SUCCESS}," +
                "\"engine\":${jsonString(engineName)}," +
                "\"koreanVoiceCount\":${koreanVoices.size}," +
                "\"offlineInstalledKoreanVoiceCount\":${offlineKoreanVoices.size}," +
                "\"voices\":[${koreanVoices.joinToString(",") { voiceJson(it) }}]" +
                "}"
        )
        tts.shutdown()
        assertTrue("TTS capability evidence was not written", output.isFile)
    }

    @Test
    fun tourStopsAcrossActivityRecreateWithoutAutomaticReplay() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        val vm = model()
        compose.runOnIdle { vm.startTour() }
        compose.waitUntil(3_000) { vm.state.value.tourRunning }
        val elapsedBeforeRecreate = vm.state.value.tourElapsedSeconds
        assertTrue(elapsedBeforeRecreate >= 0)

        compose.activityRule.scenario.recreate()
        compose.waitUntil(3_000) { !vm.state.value.tourRunning }
        val stateAfterRecreate = vm.state.value
        assertFalse("onStop must stop the active tour", stateAfterRecreate.tourRunning)
        Thread.sleep(1_200)
        assertFalse("recreate must not automatically replay the tour", vm.state.value.tourRunning)
    }

    @Test
    fun recordCarUxRestrictionsStateForTwentySeconds() {
        val context = compose.activity.applicationContext
        val events = mutableListOf<String>()
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < 20_000) {
            val safety = model().safety.value
            events += "{" +
                "\"timestampEpochMs\":${System.currentTimeMillis()}," +
                "\"automotive\":${safety.automotive}," +
                "\"platformKnown\":${safety.platformKnown}," +
                "\"requiresOptimization\":${safety.requiresOptimization}," +
                "\"activeRestrictions\":${safety.activeRestrictions}," +
                "\"foreground\":${safety.foreground}," +
                "\"sourceMessage\":${jsonString(safety.sourceMessage)}" +
                "}"
            Thread.sleep(250)
        }
        val output = File(context.filesDir, "native-carux-events.json")
        output.writeText(
            "{" +
                "\"probe\":\"public DearFamilyViewModel.safety StateFlow snapshot\",\"durationMs\":20000," +
                "\"events\":[${events.joinToString(",")}]" +
                "}"
        )
        assertTrue("Car UX evidence was not written", output.isFile)
        assertTrue("Car UX probe must contain samples", events.isNotEmpty())
    }

    private fun jsonString(value: String?): String =
        value?.let { "\"${it.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\"" } ?: "null"

    private fun voiceJson(voice: Map<String, Any?>): String =
        "{" +
            "\"name\":${jsonString(voice["name"] as String? ?: "")}," +
            "\"locale\":${jsonString(voice["locale"] as String? ?: "")}," +
            "\"isNetworkConnectionRequired\":${voice["isNetworkConnectionRequired"]}," +
            "\"features\":[${(voice["features"] as List<*>).joinToString(",") { jsonString(it as String) }}]" +
            "}"
}

package com.pleos.dearfamily

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.pleos.dearfamily.model.NarrationFeedback
import com.pleos.dearfamily.model.NarrationOutcome
import com.pleos.dearfamily.platform.AudioNarrator
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertTrue
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Real platform callback evidence only; physical listening is deliberately unverified. */
class FamilyCareNarrationFeedbackProbeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @Test fun readySpeakCompletionAndStopCancellationUseAppEventIdsExactlyOnce() {
        val evidence = JSONObject().put("physicalListeningVerified", false)
            .put("softwareCallbackEvidence", true)
        val feedback = CopyOnWriteArrayList<NarrationFeedback>()
        var narrator: AudioNarrator? = null
        var subscription: kotlinx.coroutines.Job? = null
        fun onMain(action: () -> Unit) =
            InstrumentationRegistry.getInstrumentation().runOnMainSync(action)
        try {
            val vm = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]
            compose.waitUntil(8_000) { vm.safety.value.canPlayAudio }
            assertTrue(vm.safety.value.canPlayAudio)
            onMain {
                narrator = AudioNarrator(compose.activity.applicationContext)
                narrator!!.setAllowed(true)
            }
            compose.waitUntil(20_000) { narrator!!.status.value.ready }
            evidence.put("initializationReadyObserved", true)

            onMain {
                subscription = kotlinx.coroutines.MainScope().launch {
                    narrator!!.feedback.collect { feedback += it }
                }
                narrator!!.speak(101L, "피드백 완료 콜백을 확인하는 실제 플랫폼 안내입니다.")
            }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            compose.waitUntil(20_000) { feedback.any { it.eventId == 101L } }
            val completed = feedback.filter { it.eventId == 101L }
            assertEquals(listOf(NarrationOutcome.COMPLETED), completed.map { it.outcome }.distinct())
            assertEquals(1, completed.size)
            evidence.put("completedEventId", 101L).put("completedCount", completed.size)

            onMain { narrator!!.speak(202L, "중지 시 이전 vendor callback이 무시되는지 확인하는 긴 안내입니다. ".repeat(4)) }
            compose.waitUntil(8_000) { narrator!!.status.value.speaking }
            onMain { narrator!!.stop() }
            compose.waitUntil(5_000) { feedback.any { it.eventId == 202L } }
            val cancelled = feedback.filter { it.eventId == 202L }
            assertEquals(listOf(NarrationOutcome.CANCELLED), cancelled.map { it.outcome }.distinct())
            assertEquals(1, cancelled.size)
            assertFalse(narrator!!.status.value.speaking)
            evidence.put("cancelledEventId", 202L).put("cancelledCount", cancelled.size)
            kotlinx.coroutines.runBlocking { kotlinx.coroutines.delay(1_000) }
            assertEquals(1, feedback.count { it.eventId == 202L })
            evidence.put("lateCallbackDidNotDuplicate", true)
        } catch (error: Throwable) {
            evidence.put("errorType", error::class.java.name)
            throw error
        } finally {
            File(compose.activity.filesDir, "narration-feedback-probe.json")
                .writeText(evidence.toString(2), Charsets.UTF_8)
            onMain { subscription?.cancel(); narrator?.release() }
        }
    }
}

package com.pleos.dearfamily

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider

import androidx.test.platform.app.InstrumentationRegistry
import com.pleos.dearfamily.model.NarrationFeedback
import com.pleos.dearfamily.model.NarrationOutcome
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Owned emulator OS/window and real speech callbacks; not physical/OEM certification. */
class FamilyCareFollowupNativeProbeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    private fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)
    ).bufferedReader().use { it.readText() }

    private fun model() = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun console() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithTag("enterDemo").performClick()
        compose.waitUntil(8_000) { model().safety.value.canPlayAudio }
        if (compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isEmpty())
            compose.onNodeWithTag("debugBadge").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("tour").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun waitWithoutCompose(timeoutMs: Long, predicate: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (!predicate() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(50)
        assertTrue("condition not observed within ${timeoutMs}ms", predicate())
    }

    @Test fun actualTourAdvancesOnlyAfterThreeRealSpeechCompletions() {
        console()
        val activity = compose.activity
        val vm = model()
        compose.waitUntil(20_000) { activity.narrationStatus.value.ready }
        val feedback = CopyOnWriteArrayList<NarrationFeedback>()
        var subscription: kotlinx.coroutines.Job? = null
        instrumentation.runOnMainSync {
            subscription = MainScope().launch(start = CoroutineStart.UNDISPATCHED) {
                activity.narrationFeedback.collect { feedback += it }
            }
        }
        val evidence = JSONObject().put("physicalListeningVerified", false)
        val scenes = JSONArray()
        try {
            compose.onNodeWithTag("tour").performScrollTo().performClick()
            val expected = listOf(SeatZone.KIDS_2ND_ROW, SeatZone.PARENTS_3RD_ROW, SeatZone.PASSENGER_1R)
            expected.forEach { zone ->
                compose.waitUntil(35_000) { vm.state.value.flow.targetZone == zone && !vm.state.value.tourReport }
                val event = vm.state.value.eventId
                val started = SystemClock.elapsedRealtime()
                compose.waitUntil(35_000) { feedback.any { it.eventId == event } }
                val result = feedback.filter { it.eventId == event }
                assertEquals(listOf(NarrationOutcome.COMPLETED), result.map { it.outcome })
                compose.waitUntil(10_000) { vm.state.value.eventId != event }
                scenes.put(JSONObject().put("zone", zone.name).put("eventId", event)
                    .put("observedSceneMs", SystemClock.elapsedRealtime() - started)
                    .put("terminal", result.single().outcome.name))
            }
            assertTrue(vm.state.value.tourReport)
            compose.waitUntil(12_000) { !vm.state.value.tourRunning }
            assertFalse(activity.narrationStatus.value.speaking)
            evidence.put("reportReached", true).put("tourEnded", true)
        } finally {
            evidence.put("scenes", scenes)
            File(activity.filesDir, "narrated-tour-probe.json").writeText(evidence.toString(2))
            instrumentation.runOnMainSync { subscription?.cancel(); vm.stopTour() }
        }
    }

    @Test fun ownedTaskResizesToNarrowPortraitAndRestoresItsOriginalBounds() {
        val activity = compose.activity
        val original = activity.windowManager.currentWindowMetrics.bounds
        val task = activity.taskId
        val evidence = JSONObject().put("originalBounds", original.toShortString())
            .put("physicalDisplayChanged", false)
        try {
            evidence.put("resizeOutput", shell("am task resize $task 1700 90 2535 1300"))
            awaitDearFamilyWindow(compose)
            compose.waitUntil(8_000) { compose.activity.resources.configuration.screenWidthDp <= 900 }
            val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
            evidence.put("screenWidthDp", compose.activity.resources.configuration.screenWidthDp)
                .put("rootWidthPx", root.width.toDouble()).put("rootHeightPx", root.height.toDouble())
            assertTrue("actual Compose root must be narrow", root.width in 1f..900f)
            compose.onNodeWithTag("simulationNotice").assertIsDisplayed()
            listOf("DRIVER_1L", "PASSENGER_1R", "KIDS_2ND_ROW", "PARENTS_3RD_ROW").forEach {
                compose.onNodeWithTag("seat_$it").assertExists()
            }
            val parents = compose.onNodeWithTag("seat_PARENTS_3RD_ROW")
            parents.performScrollTo().assertIsDisplayed()
            assertTrue("last seat must have a usable visible area",
                parents.fetchSemanticsNode().boundsInRoot.height >= 48f * compose.activity.resources.displayMetrics.density)
            evidence.put("lastSeatScrollVerified", true)
            val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(activity.filesDir, "narrow-window.png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
            evidence.put("narrowRootVerified", true)
        } finally {
            evidence.put("restoreOutput", shell("am task resize $task ${original.left} ${original.top} ${original.right} ${original.bottom}"))
            awaitDearFamilyWindow(compose)
            evidence.put("restoredBounds", compose.activity.windowManager.currentWindowMetrics.bounds.toShortString())
            File(activity.filesDir, "narrow-window-probe.json").writeText(evidence.toString(2))
        }
    }

    @Test fun realOsSleepStopsSpeechAndWakeDoesNotReplayIt() {
        console()
        val activity = compose.activity
        val vm = model()
        val evidence = JSONObject().put("oemSuspendVerified", false).put("physicalListeningVerified", false)
        try {
            compose.onNodeWithTag("kidsNoise").performScrollTo().performClick()
            compose.waitUntil(10_000) { activity.narrationStatus.value.speaking }
            evidence.put("speakingBeforeSleep", true)
                .put("sleepCommandOutput", shell("input keyevent ${KeyEvent.KEYCODE_SLEEP}"))
            waitWithoutCompose(5_000) { vm.safety.value.suspended }
            waitWithoutCompose(5_000) { !activity.narrationStatus.value.speaking }
            assertFalse(vm.safety.value.canPlayAudio)
            evidence.put("screenOffObserved", true).put("speechStopped", true)
        } finally {
            File(activity.filesDir, "os-sleep-wake-probe.json").writeText(evidence.toString(2))
            evidence.put("wakeCommandOutput", shell("input keyevent ${KeyEvent.KEYCODE_WAKEUP}"))
            try {
                shell("am start --user 10 --activity-single-top -n com.pleos.dearfamily/.MainActivity")
                evidence.put("foregroundRestoration", "explicit app reopen with activity-single-top")
                awaitDearFamilyWindow(compose)
                SystemClock.sleep(1_200)
                assertFalse("wake must not automatically replay", compose.activity.narrationStatus.value.speaking)
                evidence.put("wakeDidNotReplay", true)
            } finally {
                File(activity.filesDir, "os-sleep-wake-probe.json").writeText(evidence.toString(2))
            }
        }
    }
}

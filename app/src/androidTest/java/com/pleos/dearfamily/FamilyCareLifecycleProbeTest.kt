package com.pleos.dearfamily

import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.ui.DearFamilyViewModel
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Real UI lifecycle probe; screen-off remains an OS/privileged procedure, not simulated here. */
class FamilyCareLifecycleProbeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    private fun enterDemoAndOpenConsole() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        if (compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isEmpty()) {
            compose.onNodeWithTag("debugBadge").performClick()
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForNarrationStopped(activity: MainActivity, timeoutMs: Long) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (activity.narrationStatus.value.speaking && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(50)
        }
        assertFalse("narration did not stop within ${timeoutMs}ms", activity.narrationStatus.value.speaking)
    }

    private fun waitForActivityState(activity: MainActivity, state: Lifecycle.State, timeoutMs: Long) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (activity.lifecycle.currentState != state && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(50)
        }
        assertTrue("activity did not reach $state within ${timeoutMs}ms", activity.lifecycle.currentState == state)
    }

    @Test
    fun realNoiseSelectionStopsNarrationWhenStoppedAndNeverReplaysAfterResumeOrRecreate() {
        val activityBeforeStop = compose.activity
        val evidenceFile = File(activityBeforeStop.filesDir, "lifecycle-probe.json")
        val evidence = JSONObject()
            .put("physicalListeningVerified", false)
            .put("screenOffExecuted", false)
            .put("screenOffReason", "requires OS power action; no privileged workaround")
        try {
            enterDemoAndOpenConsole()
            val vm = ViewModelProvider(activityBeforeStop)[DearFamilyViewModel::class.java]
            compose.waitUntil(5_000) { vm.safety.value.canPlayAudio }
            assertTrue("parked foreground audio must be available for this native probe", vm.safety.value.canPlayAudio)

            compose.onNodeWithTag("kidsNoise").performScrollTo().performClick()
            compose.waitUntil(10_000) { activityBeforeStop.narrationStatus.value.speaking }
            evidence.put("noiseSelectionObserved", true)
                .put("speakingBeforeStop", true)

            compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
            waitForNarrationStopped(activityBeforeStop, 5_000)
            evidence.put("createdStoppedNarration", true)

            compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            waitForActivityState(activityBeforeStop, Lifecycle.State.RESUMED, 5_000)
            SystemClock.sleep(1_200)
            assertFalse("RESUMED must not automatically replay narration", activityBeforeStop.narrationStatus.value.speaking)
            evidence.put("resumedDidNotReplay", true)

            compose.activityRule.scenario.recreate()
            val recreatedActivity = compose.activity
            waitForActivityState(recreatedActivity, Lifecycle.State.RESUMED, 5_000)
            SystemClock.sleep(1_200)
            assertFalse("recreate must not automatically replay narration", recreatedActivity.narrationStatus.value.speaking)
            evidence.put("recreatedDidNotReplay", true)
        } finally {
            evidenceFile.writeText(evidence.toString(2), Charsets.UTF_8)
        }
    }
}

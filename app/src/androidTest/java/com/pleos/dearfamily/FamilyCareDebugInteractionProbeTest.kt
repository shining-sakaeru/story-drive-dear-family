package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Records nested debug-panel interaction evidence without replacing UI clicks with SDK calls. */
class FamilyCareDebugInteractionProbeTest {
    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun node(tag: String): SemanticsNodeInteraction = compose.onNodeWithTag(tag)

    @Test
    fun probeNestedCarePanelInteractions() {
        val log = StringBuilder()
        val output = File(compose.activity.filesDir, "debug-interaction-probe.txt")
        try {
            val model = ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]
            val hasEnterDemo = compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()
            log.appendLine("enterDemoPresent=$hasEnterDemo")
            if (hasEnterDemo) node("enterDemo").performClick()
            val entered = runCatching {
                compose.waitUntil(5_000) {
                    compose.onAllNodesWithTag("debugBadge").fetchSemanticsNodes().isNotEmpty()
                }
                true
            }.getOrDefault(false)
            assumeTrue("debug entry unavailable; probe skipped", entered)
            if (compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isEmpty()) {
                node("debugBadge").performClick()
            }
            compose.waitUntil(5_000) {
                compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isNotEmpty()
            }

            log.appendLine("funToggle.beforeState.cozyMode=${model.state.value.cozyMode}")
            logNode(log, "funToggle")
            clickAfterScroll(log, "funToggle")
            compose.waitUntil(3_000) { !model.state.value.cozyMode }
            log.appendLine("funToggle.afterState.cozyMode=${model.state.value.cozyMode}")
            assertFalse("funToggle UI click must leave Cozy mode", model.state.value.cozyMode)

            clickAfterScroll(log, "parents")
            log.appendLine("restAccept.beforeState.restStopAccepted=${model.state.value.restStopAccepted}")
            logNode(log, "restAccept")
            clickAfterScroll(log, "restAccept")
            compose.waitUntil(3_000) { model.state.value.restStopAccepted == true }
            log.appendLine("restAccept.afterState.restStopAccepted=${model.state.value.restStopAccepted}")
            assertTrue("restAccept UI click must accept the rest stop", model.state.value.restStopAccepted == true)
            logNode(log, "restAnswer")
            node("restAnswer").assertIsDisplayed()
        } finally {
            output.parentFile?.mkdirs()
            output.writeText(log.toString())
        }
    }

    private fun logNode(log: StringBuilder, tag: String) {
        val interaction = node(tag)
        log.appendLine("$tag.printToString=${runCatching { interaction.printToString() }.getOrElse { "ERROR:${it.message}" }}")
        log.appendLine("$tag.bounds=${runCatching { interaction.fetchSemanticsNode().boundsInRoot.toString() }.getOrElse { "ERROR:${it.message}" }}")
    }

    private fun clickAfterScroll(log: StringBuilder, tag: String) {
        val interaction = node(tag)
        logNode(log, tag)
        runCatching { interaction.assertIsDisplayed() }
            .onFailure { log.appendLine("$tag.displayCheck=notDisplayed:${it.message}") }
        runCatching { interaction.performScrollTo() }
            .onFailure { log.appendLine("$tag.scroll=failed:${it.message}") }
        logNode(log, tag)
        interaction.performClick()
        log.appendLine("$tag.click=performed")
    }
}

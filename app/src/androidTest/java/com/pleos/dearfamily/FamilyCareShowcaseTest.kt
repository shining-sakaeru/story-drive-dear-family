package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Exercises the compact family journey entry point through MainActivity semantics. */
class FamilyCareShowcaseTest {
    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun model(): DearFamilyViewModel =
        ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun action(tag: String) = compose.onNodeWithTag(tag).also {
        compose.waitUntil(5_000) {
            runCatching { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }.getOrDefault(false)
        }
        runCatching { it.performScrollTo() }
        it.assertIsDisplayed()
        it.assertHeightIsAtLeast(48.dp)
        it.assertWidthIsAtLeast(48.dp)
    }

    private fun visibleText(tag: String) = compose.onNodeWithTag(tag).also {
        compose.waitUntil(5_000) {
            runCatching { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }.getOrDefault(false)
        }
        runCatching { it.performScrollTo() }
        it.assertIsDisplayed()
    }

    private fun openJourney() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            action("enterDemo").performClick()
        }
        compose.waitUntil(3_000) { compose.onAllNodesWithTag("themePicker").fetchSemanticsNodes().isNotEmpty() }
        action("themePicker").performClick()
        action("theme_SCANDINAVIAN_WARM").performClick()
        compose.waitUntil(3_000) { compose.onAllNodesWithTag("openFamilyJourney").fetchSemanticsNodes().isNotEmpty() }
        action("openFamilyJourney").performClick()
        val panel = compose.onNodeWithTag("journeyPanel").assertIsDisplayed()
        panel.assertWidthIsAtLeast(48.dp)
        panel.assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun threeScenesUseRealScenarioCallbacksAndShowBeforeAfter() {
        openJourney()
        val vm = model()
        action("journeyNoise").performClick()
        compose.waitUntil(2_000) { vm.state.value.selectedScenario == "KIDS_NOISE" }
        visibleText("journeyBeforeAfter")
        visibleText("journeyCaption").assertTextEquals(vm.state.value.flow.gleoTtsScript)

        action("journeySleep").performClick()
        compose.waitUntil(2_000) { vm.state.value.selectedScenario == "KIDS_SLEEP" }
        visibleText("journeyVoice").assertTextContains("무음", substring = true)
        compose.onNodeWithTag("journeyCaption").assertDoesNotExist()

        action("journeyParents").performClick()
        compose.waitUntil(2_000) { vm.state.value.selectedScenario == "PARENTS" }
        action("journeyRestAccept").performClick()
        assertEquals(true, vm.state.value.restStopAccepted)
    }

    @Test
    fun closeAndStopWorkAndDetailsStayWithinCompactPanel() {
        openJourney()
        val vm = model()
        action("journeyNoise").performClick()
        compose.waitUntil(2_000) { vm.state.value.flow.gleoTtsScript.isNotBlank() }
        action("journeyStop").performClick()
        compose.waitUntil(2_000) { vm.state.value.flow.gleoTtsScript.isBlank() }
        compose.onNodeWithTag("journeyCaption").assertDoesNotExist()
        action("journeyDetails").performClick()
        action("journeyDetailsClose").performClick()
        action("journeyStop").performClick()
        action("journeyClose").performClick()
        compose.onNodeWithTag("journeyPanel").assertDoesNotExist()
    }

    @Test
    fun journeyAcceptsTouchScrollingWhenItsContentOverflows() {
        openJourney()
        action("journeyParents").performClick()
        val panel = compose.onNodeWithTag("journeyPanel")
        val range = panel.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange]
        if (range.maxValue() > 0f) {
            val before = range.value()
            panel.performTouchInput { swipeUp() }
            compose.waitUntil(3_000) { range.value() > before }
        } else {
            compose.onNodeWithTag("journeyRestAccept").assertIsDisplayed()
        }
        action("journeyStop").performClick()
        assertTrue(model().state.value.flow.gleoTtsScript.isBlank())
    }
}

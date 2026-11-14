package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.unit.dp
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Exercises the public care controls through the Warm theme UI. */
class FamilyCareWarmActionsTest {
    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun model(): DearFamilyViewModel =
        ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun enterWarmTheme() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("themePicker").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("themePicker").performClick()
        compose.onNodeWithTag("theme_SCANDINAVIAN_WARM").performClick()
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("playStory").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun action(tag: String): SemanticsNodeInteraction {
        val action = compose.onNodeWithTag(tag)
        action.assertExists()
        action.assertHeightIsAtLeast(48.dp)
        if (!runCatching { action.assertIsDisplayed() }.isSuccess) {
            action.performScrollTo()
        }
        return action
    }

    @Test
    fun storyPlaybackIsReversibleAndDoesNotChangeCozyMode() {
        enterWarmTheme()
        val vm = model()
        val initialCozy = vm.state.value.cozyMode

        action("playStory").performClick()
        compose.waitUntil(3_000) { vm.state.value.storyPlaying }
        assertTrue(vm.state.value.storyPlaying)
        assertEquals(initialCozy, vm.state.value.cozyMode)
        assertTrue(vm.state.value.flow.gleoTtsScript.isNotBlank())
        compose.onNodeWithTag("warmStoryCaption").performScrollTo()
            .assertIsDisplayed().assertTextEquals(vm.state.value.flow.gleoTtsScript)

        action("playStory").performClick()
        compose.waitUntil(3_000) { !vm.state.value.storyPlaying }
        assertFalse(vm.state.value.storyPlaying)
        assertEquals(initialCozy, vm.state.value.cozyMode)
    }

    @Test
    fun quietCabinStopsStoryAndPlayIsDisabledUntilQuietIsOff() {
        enterWarmTheme()
        val vm = model()
        action("playStory").performClick()
        compose.waitUntil(3_000) { vm.state.value.storyPlaying }

        action("quietCabin").performClick()
        compose.waitUntil(3_000) { vm.state.value.quietCabin && !vm.state.value.storyPlaying }
        assertTrue(vm.state.value.quietCabin)
        assertFalse(vm.state.value.storyPlaying)
        action("playStory").assertIsNotEnabled()

        action("quietCabin").performClick()
        compose.waitUntil(3_000) { !vm.state.value.quietCabin }
        assertFalse(vm.state.value.quietCabin)
        assertFalse(vm.state.value.storyPlaying)
    }

    @Test
    fun cabinWarmthIsIndependentOfCozyModeAndStoryPlayback() {
        enterWarmTheme()
        val vm = model()
        val initialCozy = vm.state.value.cozyMode

        action("cabinWarmth").performClick()
        compose.waitUntil(3_000) { vm.state.value.cabinWarmth }
        assertTrue(vm.state.value.cabinWarmth)
        assertEquals(initialCozy, vm.state.value.cozyMode)
        assertFalse(vm.state.value.storyPlaying)
    }

    @Test
    fun storyIsDisabledDuringKidsSleep() {
        enterWarmTheme()
        val vm = model()
        compose.runOnIdle { vm.manual(Scenario.KIDS_SLEEP) }
        compose.waitUntil(3_000) { vm.state.value.silentMessage != null }

        action("playStory").assertIsNotEnabled()
        assertFalse(vm.state.value.storyPlaying)
    }
}

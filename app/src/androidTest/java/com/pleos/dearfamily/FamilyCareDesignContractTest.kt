package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test

/** Covers opt-in diagnostics and reachable parked-mode design controls. */
class FamilyCareDesignContractTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    private fun enterDemoIfAvailable() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("debugBadge").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun diagnosticsAreHiddenUntilDebugBadgeIsOpened() {
        val hasEnterDemo = compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()
        if (hasEnterDemo) {
            compose.onNodeWithTag("restrictedStatus").assertExists()
            compose.onNodeWithTag("debugBadge").assertDoesNotExist()
        } else {
            compose.onNodeWithTag("debugBadge").assertExists()
        }
        compose.onNodeWithTag("debugDock").assertDoesNotExist()
        compose.onNodeWithTag("debugVehicle").assertDoesNotExist()

        enterDemoIfAvailable()
        compose.onNodeWithTag("debugDock").assertDoesNotExist()
        compose.onNodeWithTag("debugVehicle").assertDoesNotExist()

        compose.onNodeWithTag("debugBadge").performClick()
        compose.waitUntil(5_000) {
            runCatching {
                compose.onNodeWithTag("debugDock").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        compose.onNodeWithTag("debugDock").assertIsDisplayed()
        compose.onNodeWithTag("debugVehicle").assertExists()
        compose.onNodeWithTag("debugVehicle").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun themePickerSelectionControlsRemainReachable() {
        enterDemoIfAvailable()
        compose.onNodeWithTag("themePicker").performClick()
        compose.onNodeWithTag("theme_SCANDINAVIAN_WARM").performClick()
        compose.onNodeWithTag("themePicker").assertIsDisplayed()
        compose.onNodeWithTag("settingsButton").performClick()
        compose.onNodeWithTag("settingsDialog").assertIsDisplayed()
        compose.onNodeWithTag("settingDrivingTheme_GENESIS_LUXURY").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settingParkedTheme_SCANDINAVIAN_WARM").performScrollTo().assertIsDisplayed()
    }
}

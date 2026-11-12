package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Requires a landscape Android 10+ device. Not a replacement for JVM domain tests. */
class FamilyCareUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun enterDemo() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithTag("enterDemo").performClick()
    }
    private fun openDebugBadgeAfterDemo() {
        compose.onNodeWithTag("debugBadge").performClick()
    }

    private fun mediaPanel() {
        if (compose.onAllNodesWithTag("panelTab2").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithTag("panelTab2").performScrollTo().performClick()
    }
    @Test fun unknownStateDoesNotExposeScenarioControls() {
        org.junit.Assume.assumeTrue(compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithTag("restrictedStatus").assertExists()
        compose.onNodeWithTag("tour").assertDoesNotExist()
        compose.onNodeWithTag("funToggle").assertDoesNotExist()
    }

    @Test fun initialCabinShowsFourZonesAndMockBoundary() {
        listOf("seat_DRIVER_1L", "seat_PASSENGER_1R", "seat_KIDS_2ND_ROW", "seat_PARENTS_3RD_ROW")
            .forEach { compose.onNodeWithTag(it).assertExists() }
        compose.onNodeWithTag("simulationNotice").assertIsDisplayed()
    }

    @Test fun sleepIsSilentAndResetRestoresBaseline() {
        enterDemo()
        openDebugBadgeAfterDemo()
        compose.onNodeWithTag("kidsNoise").performScrollTo().performClick()
        compose.onNodeWithTag("kidsSleep").performScrollTo().performClick()
        compose.onNodeWithTag("silentSleepMessage").assertExists()
        mediaPanel()
        compose.onNodeWithTag("ttsStatus").assertTextContains("수면", substring = true)
        compose.onNodeWithTag("reset").performScrollTo().performClick()
        compose.onNodeWithTag("silentSleepMessage").assertDoesNotExist()
    }

    @Test fun passengerGameAndConversationRestoreAreInteractive() {
        enterDemo()
        openDebugBadgeAfterDemo()
        compose.onNodeWithTag("passenger").performScrollTo().performClick()
        mediaPanel()
        compose.onNodeWithTag("funToggle").performScrollTo().performClick()
        compose.onNodeWithTag("snackWalnut").performScrollTo().performClick()
        compose.onNodeWithTag("snackChoice").assertTextContains("호두과자", substring = true)
        compose.onNodeWithTag("conversationEnd").performScrollTo().performClick()
        compose.waitUntil(5_000) {
            runCatching {
                compose.onNodeWithTag("audio_PASSENGER_1R").assertTextContains("70%", substring = true)
            }.isSuccess
        }
    }

    @Test fun manualScenarioStopsTourAndParentsOfferRest() {
        enterDemo()
        openDebugBadgeAfterDemo()
        compose.onNodeWithTag("tour").performScrollTo().performClick()
        compose.onNodeWithTag("parents").performScrollTo().performClick()
        compose.onNodeWithTag("tourStop").assertDoesNotExist()
        mediaPanel()
        compose.onNodeWithTag("restAccept").performScrollTo().performClick()
        compose.onNodeWithTag("restAnswer").assertTextContains("모의 경유", substring = true)
    }
}

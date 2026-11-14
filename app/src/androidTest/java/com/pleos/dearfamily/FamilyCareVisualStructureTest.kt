package com.pleos.dearfamily

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Verifies the Phase 2 Genesis visual structure through real Compose semantics. */
class FamilyCareVisualStructureTest {
    @Before
    fun waitForPlatformWindow() { awaitDearFamilyWindow(compose) }

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun enterDemoIfAvailable() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("themePicker").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun genesisArtworkAndSeatStructureFillTheRenderedLayout() {
        enterDemoIfAvailable()
        compose.onNodeWithTag("themePicker").performClick()
        compose.onNodeWithTag("theme_GENESIS_LUXURY").performClick()

        compose.onNodeWithTag("genesisContent").assertIsDisplayed()
        compose.onNodeWithTag("genesisVehicleArtwork").assertIsDisplayed()

        val seatTags = listOf(
            "seat_DRIVER_1L",
            "seat_PASSENGER_1R",
            "seat_KIDS_2ND_ROW",
            "seat_PARENTS_3RD_ROW"
        )
        seatTags.forEach {
            compose.onAllNodesWithTag(it).assertCountEquals(1)
            compose.onNodeWithTag(it).assertIsDisplayed()
        }

        val contentBounds = compose.onNodeWithTag("genesisContent").fetchSemanticsNode().boundsInRoot
        val artworkBounds = compose.onNodeWithTag("genesisVehicleArtwork").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Genesis vehicle artwork must occupy a significant portion of Genesis content",
            artworkBounds.width >= contentBounds.width * 0.4f
        )
    }

    @Test
    fun diagnosticsRemainHiddenUntilDebugBadgeIsClicked() {
        enterDemoIfAvailable()
        compose.onNodeWithTag("debugDock").assertDoesNotExist()
        compose.onNodeWithTag("debugVehicle").assertDoesNotExist()

        compose.onNodeWithTag("debugBadge").performClick()
        compose.onNodeWithTag("debugDock").assertIsDisplayed()
        compose.onNodeWithTag("debugVehicle").assertExists()
        compose.onNodeWithTag("debugVehicle").performScrollTo().assertIsDisplayed()
    }
}

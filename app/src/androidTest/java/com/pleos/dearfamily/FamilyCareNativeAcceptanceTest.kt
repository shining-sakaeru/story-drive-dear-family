package com.pleos.dearfamily

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Acceptance coverage for actual Compose semantics and the public ViewModel state contract. */
class FamilyCareNativeAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun node(tag: String) = compose.onNodeWithTag(tag)
    private fun model(): DearFamilyViewModel =
        ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun enterDemoIfAvailable() {
        val enteredFromRestrictedState = compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()
        if (enteredFromRestrictedState) {
            node("enterDemo").performClick()
        }
        openDebugBadgeAfterDemo()
        if (!enteredFromRestrictedState) {
            node("tour").assertExists()
            node("kidsNoise").assertExists()
            node("parents").assertExists()
            node("passenger").assertExists()
        }
        compose.waitUntil(3_000) {
            compose.onAllNodesWithTag("tour").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openDebugBadgeAfterDemo() {
        if (compose.onAllNodesWithTag("debugDock").fetchSemanticsNodes().isEmpty()) {
            node("debugBadge").performClick()
        }
    }

    private fun media() {
        if (compose.onAllNodesWithTag("panelTab2").fetchSemanticsNodes().isNotEmpty()) {
            node("panelTab2").performScrollTo().performClick()
        }
    }

    private fun ipo() {
        if (compose.onAllNodesWithTag("panelTab1").fetchSemanticsNodes().isNotEmpty()) {
            node("panelTab1").performClick()
        }
    }

    private fun setProgress(tag: String, value: Float) {
        node(tag).performSemanticsAction(SemanticsActions.SetProgress) { action -> action(value) }
    }

    private fun assertSlider(tag: String, value: Float, range: ClosedFloatingPointRange<Float>) {
        node(tag).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(value, range)
            )
        )
    }

    private fun assertMockIpoOutput() {
        ipo()
        compose.onNodeWithText("Multi-Zone Adapter · MOCK", substring = true).assertExists()
    }

    @Test fun initialStateExposesExactlyFourSeatZonesAndMockBoundary() {
        listOf("DRIVER_1L", "PASSENGER_1R", "KIDS_2ND_ROW", "PARENTS_3RD_ROW")
            .forEach { node("seat_$it").assertExists() }
        node("simulationNotice").assertExists()
    }

    @Test fun noiseSliderRearmsOnlyAfterDroppingBelowThreshold() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        val vm = model()
        setProgress("noiseSlider", 82f)
        compose.waitUntil(3_000) { vm.state.value.noiseDb == 82 && vm.state.value.flow.activeScenarioTitle.contains("소란") }
        val firstEvent = vm.state.value.eventId
        assertSlider("noiseSlider", 82f, 30f..100f)
        // The same sensor value is intentionally visible in the cabin and dock.
        compose.onAllNodesWithText("82 dB", substring = false).onFirst().assertIsDisplayed()
        assertMockIpoOutput()
        media()
        node("audio_KIDS_2ND_ROW").assertTextContains("SIMULATION", substring = true)

        setProgress("noiseSlider", 89f)
        compose.waitUntil(3_000) { vm.state.value.noiseDb == 89 }
        assertSlider("noiseSlider", 89f, 30f..100f)
        assertEquals("same-band noise must not create another event", firstEvent, vm.state.value.eventId)

        setProgress("noiseSlider", 60f)
        compose.waitUntil(3_000) { vm.state.value.noiseDb == 60 }
        assertEquals(firstEvent, vm.state.value.eventId)
        setProgress("noiseSlider", 82f)
        compose.waitUntil(3_000) { vm.state.value.noiseDb == 82 && vm.state.value.eventId > firstEvent }
        assertTrue(vm.state.value.flow.activeScenarioTitle.contains("소란"))
    }

    @Test fun elapsedSliderRearmsOnlyAfterDroppingBelowThreshold() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        val vm = model()
        setProgress("elapsedSlider", 52f)
        compose.waitUntil(3_000) { vm.state.value.elapsedMin == 52 && vm.state.value.flow.activeScenarioTitle.contains("부모님") }
        val firstEvent = vm.state.value.eventId
        assertSlider("elapsedSlider", 52f, 0f..120f)
        // I-P-O and the slider label both show elapsed time; the tagged range is unique.
        compose.onAllNodesWithText("52분", substring = false).onFirst().assertIsDisplayed()
        assertMockIpoOutput()

        setProgress("elapsedSlider", 60f)
        compose.waitUntil(3_000) { vm.state.value.elapsedMin == 60 }
        assertSlider("elapsedSlider", 60f, 0f..120f)
        assertEquals(firstEvent, vm.state.value.eventId)
        setProgress("elapsedSlider", 40f)
        compose.waitUntil(3_000) { vm.state.value.elapsedMin == 40 }
        assertEquals(firstEvent, vm.state.value.eventId)
        setProgress("elapsedSlider", 52f)
        compose.waitUntil(3_000) { vm.state.value.elapsedMin == 52 && vm.state.value.eventId > firstEvent }
        assertTrue(vm.state.value.flow.activeScenarioTitle.contains("부모님"))
    }

    @Test fun fourManualPresetsTargetTheirZonesAndExposeMockOutput() {
        enterDemoIfAvailable()
        val cases = listOf(
            Triple("kidsNoise", "seat_KIDS_2ND_ROW", "소란 케어"),
            Triple("kidsSleep", "seat_KIDS_2ND_ROW", "수면 케어"),
            Triple("parents", "seat_PARENTS_3RD_ROW", "피로 케어"),
            Triple("passenger", "seat_PASSENGER_1R", "Cozy 대화")
        )
        cases.forEach { (button, seat, status) ->
            node(button).performScrollTo().performClick()
            val renderedStatus = when (button) {
                "kidsNoise" -> "외기 순환"
                "kidsSleep" -> "조명 OFF"
                "parents" -> "온열 1"
                else -> "Cozy 대화"
            }
            // Clickable seat cards merge their text; other themes expose descendants.
            node(seat).assert(hasText(renderedStatus, substring = true).or(
                hasAnyDescendant(hasText(renderedStatus, substring = true))))
            assertEquals(seat.removePrefix("seat_"), model().state.value.flow.targetZone.name)
            assertEquals(status, model().state.value.seats.first { it.zone.name == seat.removePrefix("seat_") }.status)
            assertMockIpoOutput()
            node("activeScenario").assertTextContains(status.substringBefore(" 케어").substringBefore(" 대화"), substring = true)
        }
    }

    @Test fun passengerFunConversationAndRestStopRestoreState() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        node("passenger").performScrollTo().performClick()
        media()
        node("funToggle").performScrollTo().performClick()
        node("snackWalnut").performScrollTo().performClick()
        node("snackChoice").assertTextContains("호두과자", substring = true)
        node("conversationEnd").performScrollTo().performClick()
        compose.waitUntil(3_000) {
            runCatching { node("audio_PASSENGER_1R").assertTextContains("70%", substring = true) }.isSuccess
        }
        node("parents").performScrollTo().performClick()
        node("restAccept").performScrollTo().performClick()
        node("restAnswer").assertTextContains("모의 경유", substring = true)
    }

    @Test fun tourCanBeCancelledWithoutLeavingRunningState() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        node("tour").performScrollTo().performClick()
        node("tourStop").performScrollTo().performClick()
        node("tourStop").assertDoesNotExist()
        assertFalse(model().state.value.tourRunning)
    }

    @Test fun tourRunsThirtyRealSecondsAndShowsDriverSilentReport() {
        enterDemoIfAvailable()
        openDebugBadgeAfterDemo()
        val vm = model()
        node("tour").performScrollTo().performClick()
        compose.waitUntil(45_000) { vm.state.value.tourReport && !vm.state.value.tourRunning }
        node("tourReport").assertTextContains("운전석 내비 라우팅 유지", substring = true)
        assertEquals("DRIVER_1L", vm.state.value.flow.targetZone.name)
        assertEquals("운전자 리포트 · TTS 없음", vm.state.value.flow.audioRoutingOutput)
        assertMockIpoOutput()
    }
}

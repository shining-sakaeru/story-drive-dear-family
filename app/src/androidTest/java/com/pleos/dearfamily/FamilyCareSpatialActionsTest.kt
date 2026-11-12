package com.pleos.dearfamily

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.CareTab
import com.pleos.dearfamily.ui.DearFamilyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Exercises the Spatial theme controls through real Compose semantics. */
class FamilyCareSpatialActionsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun model(): DearFamilyViewModel =
        ViewModelProvider(compose.activity)[DearFamilyViewModel::class.java]

    private fun enterSpatialTheme() {
        if (compose.onAllNodesWithTag("enterDemo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("enterDemo").performClick()
        }
        compose.waitUntil(3_000) { compose.onAllNodesWithTag("themePicker").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("themePicker").performClick()
        compose.onNodeWithTag("theme_SPATIAL_WIDGET").performClick()
        compose.waitUntil(3_000) { compose.onAllNodesWithTag("dock_HOME").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun fixedControl(tag: String): SemanticsNodeInteraction {
        val node = compose.onNodeWithTag(tag)
        node.assertExists()
        node.assertHeightIsAtLeast(48.dp)
        if (!runCatching { node.assertIsDisplayed() }.isSuccess) node.performScrollTo()
        return node
    }

    @Test
    fun dockTabsUpdateRealViewModelSelection() {
        enterSpatialTheme()
        val vm = model()
        listOf(CareTab.HOME, CareTab.CLIMATE, CareTab.MEDIA, CareTab.NAVIGATION).forEach { tab ->
            fixedControl("dock_${tab.name}").performClick()
            assertEquals(tab, vm.state.value.selectedTab)
        }
    }

    @Test
    fun scenarioPillChangesScenarioAndTargetZone() {
        enterSpatialTheme()
        val vm = model()
        fixedControl("scenarioPill").performClick()
        compose.onNodeWithTag("scenario_PARENTS").performClick()
        assertEquals("PARENTS", vm.state.value.selectedScenario)
        assertEquals("PARENTS_3RD_ROW", vm.state.value.flow.targetZone.name)
    }

    @Test
    fun passengerVentilationIsSetThroughProgressSemantics() {
        enterSpatialTheme()
        val vm = model()
        compose.onNodeWithTag("seatVentilation").performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
        assertEquals(3, vm.state.value.seats.first { it.zone.name == "PASSENGER_1R" }.fanLevel)
    }

    @Test
    fun parentSeatHeatingTogglesAndMuteUsesSelectedSemantics() {
        enterSpatialTheme()
        val vm = model()
        fixedControl("seatHeating").performClick()
        assertEquals(1, vm.state.value.seats.first { it.zone.name == "PARENTS_3RD_ROW" }.heatingLevel)
        fixedControl("seatHeating").performClick()
        assertEquals(0, vm.state.value.seats.first { it.zone.name == "PARENTS_3RD_ROW" }.heatingLevel)

        val mute = fixedControl("quickMute")
        mute.performClick()
        mute.assertIsSelected()
        mute.performClick()
        mute.assertIsNotSelected()
    }

    @Test
    fun spatialStoryControlTogglesRealState() {
        enterSpatialTheme()
        val vm = model()
        fixedControl("spatialPlay").performClick()
        assertTrue(vm.state.value.storyPlaying)
        fixedControl("spatialPlay").performClick()
        assertFalse(vm.state.value.storyPlaying)
    }
}

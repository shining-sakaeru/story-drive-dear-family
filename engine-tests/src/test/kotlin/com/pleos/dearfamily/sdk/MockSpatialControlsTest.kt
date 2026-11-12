@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.pleos.dearfamily.sdk

import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.model.CareTab
import com.pleos.dearfamily.model.SeatZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockSpatialControlsTest {
    @Test
    fun tabAndScenarioSelectionsPersistUntilReset() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.selectTab(CareTab.MEDIA)
        sdk.selectScenario(Scenario.PARENTS)

        assertEquals(CareTab.MEDIA, sdk.state.value.selectedTab)
        assertEquals(Scenario.PARENTS.name, sdk.state.value.selectedScenario)
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)

        sdk.reset()
        assertEquals(CareTab.HOME, sdk.state.value.selectedTab)
        assertEquals("Night Journey", sdk.state.value.selectedScenario)
    }

    @Test
    fun scenarioSelectionReflectsNameAndAppliesCorrectZone() = runTest {
        val sdk = MockPleosSdkManager(this)
        sdk.selectScenario(Scenario.PARENTS)

        assertEquals("PARENTS", sdk.state.value.selectedScenario)
        assertEquals(SeatZone.PARENTS_3RD_ROW, sdk.state.value.flow.targetZone)
    }

    @Test
    fun boundedVentilationOnlyChangesPassengerFanLevel() = runTest {
        val sdk = MockPleosSdkManager(this)
        val before = sdk.state.value.seats.associateBy { it.zone }
        sdk.setSeatVentilation(99)

        assertEquals(3, sdk.state.value.seats.first { it.zone == SeatZone.PASSENGER_1R }.fanLevel)
        sdk.state.value.seats.filter { it.zone != SeatZone.PASSENGER_1R }.forEach { seat ->
            assertEquals(before.getValue(seat.zone), seat)
        }
        sdk.setSeatVentilation(-4)
        assertEquals(0, sdk.state.value.seats.first { it.zone == SeatZone.PASSENGER_1R }.fanLevel)
    }

    @Test
    fun seatHeatingTogglesParentsBetweenOffAndLevelOne() = runTest {
        val sdk = MockPleosSdkManager(this)
        assertEquals(0, sdk.state.value.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }.heatingLevel)
        sdk.toggleSeatHeating()
        assertEquals(1, sdk.state.value.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }.heatingLevel)
        sdk.toggleSeatHeating()
        assertEquals(0, sdk.state.value.seats.first { it.zone == SeatZone.PARENTS_3RD_ROW }.heatingLevel)
    }

    @Test
    fun spatialControlsDoNotStartStoryOrToggleCozyMode() = runTest {
        val sdk = MockPleosSdkManager(this)
        val cozy = sdk.state.value.cozyMode
        sdk.selectTab(CareTab.CLIMATE)
        sdk.setSeatVentilation(3)
        sdk.toggleSeatHeating()

        assertFalse(sdk.state.value.storyPlaying)
        assertTrue(sdk.state.value.cozyMode == cozy)
    }
}

package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.domain.Context
import com.pleos.dearfamily.domain.Scenario
import com.pleos.dearfamily.domain.ScenarioEngine
import com.pleos.dearfamily.model.SeatZone
import com.pleos.dearfamily.sdk.MockPleosSdkManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShowcaseJourneyAcceptanceTest {
    @Test
    fun representativeScenesUseShortKoreanScriptsAndKeepOtherZones() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            val driver = sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L }

            sdk.trigger(Scenario.KIDS_NOISE)
            assertTrue(sdk.state.value.flow.gleoTtsScript.contains("동화"))
            assertTrue(sdk.state.value.flow.gleoTtsScript.contains("쉬어요"))
            assertEquals(driver, sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L })

            sdk.trigger(Scenario.KIDS_SLEEP)
            assertEquals("", sdk.state.value.flow.gleoTtsScript)
            assertEquals(0, sdk.state.value.audio.single { it.zone == SeatZone.KIDS_2ND_ROW }.volume)
            assertEquals(driver, sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L })

            sdk.trigger(Scenario.PARENTS)
            assertTrue(sdk.state.value.flow.gleoTtsScript.contains("쉬어"))
            assertFalse(sdk.state.value.flow.gleoTtsScript.contains("데워"))
            assertTrue(sdk.state.value.seats.single { it.zone == SeatZone.PARENTS_3RD_ROW }.active)
        } finally {
            sdk.shutdown()
        }
    }

    @Test
    fun thresholdsKeepStrictBoundariesAndRequireCompanionSignals() {
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 75, tunnelMeters = 300)))
        assertEquals(Scenario.KIDS_NOISE, ScenarioEngine.evaluate(Context(noiseDb = 76, tunnelMeters = 300)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 76, tunnelMeters = 301)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 35, quietSeconds = 180, eyesClosed = true)))
        assertEquals(Scenario.KIDS_SLEEP, ScenarioEngine.evaluate(Context(noiseDb = 35, quietSeconds = 180, eyesClosed = true, breathingStable = true)))
        assertNull(ScenarioEngine.evaluate(Context(elapsedMin = 50, pressureShift = true, oldRoadDistanceKm = 3.0)))
        assertEquals(Scenario.PARENTS, ScenarioEngine.evaluate(Context(elapsedMin = 51, pressureShift = true, oldRoadDistanceKm = 3.0)))
    }

    @Test
    fun replayCreatesAnEventAndStopClearsNarrationWithoutChangingDriver() = runTest {
        val sdk = MockPleosSdkManager(this)
        try {
            sdk.trigger(Scenario.KIDS_NOISE)
            val firstEvent = sdk.state.value.eventId
            sdk.trigger(Scenario.KIDS_NOISE)
            assertTrue(sdk.state.value.eventId > firstEvent)
            val driver = sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L }

            sdk.stopTour()
            advanceUntilIdle()
            assertEquals("", sdk.state.value.flow.gleoTtsScript)
            assertFalse(sdk.state.value.tourRunning)
            assertEquals(driver, sdk.state.value.audio.single { it.zone == SeatZone.DRIVER_1L })
        } finally {
            sdk.shutdown()
        }
    }

    @Test
    fun passengerScriptDoesNotClaimRealAudioOrVehicleControl() {
        val flow = ScenarioEngine.flow(Scenario.PASSENGER)
        assertNotNull(flow.gleoTtsScript)
        assertTrue(flow.gleoTtsScript.contains("쉬어"))
        assertFalse(flow.gleoTtsScript.contains("조명을 켰어요"))
        assertTrue(flow.vehicleControlOutputs.all { it.contains("LOCAL MOCK") })
        assertTrue(flow.audioRoutingOutput.contains("LOCAL MOCK"))
    }
}

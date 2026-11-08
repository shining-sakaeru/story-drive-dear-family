package com.pleos.dearfamily.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioEngineTest {
    @Test fun noiseThresholdUsesNoiseAndTunnelOnly() {
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 75, tunnelMeters = 300)))
        assertEquals(Scenario.KIDS_NOISE, ScenarioEngine.evaluate(Context(noiseDb = 76, tunnelMeters = 300, lateralG = 0.0)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 82, tunnelMeters = 301)))
    }

    @Test fun sleepRequiresQuietEyesAndBreathing() {
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 36, quietSeconds = 180, eyesClosed = true, breathingStable = true)))
        assertNull(ScenarioEngine.evaluate(Context(noiseDb = 30, quietSeconds = 179, eyesClosed = true, breathingStable = true)))
        assertEquals(Scenario.KIDS_SLEEP, ScenarioEngine.evaluate(Context(noiseDb = 30, quietSeconds = 180, eyesClosed = true, breathingStable = true)))
    }

    @Test fun flowReflectsSliderInputAndExactScript() {
        val flow = ScenarioEngine.flow(Scenario.KIDS_NOISE, noiseDb = 88, tunnelMeters = 120)
        assertEquals("88dB", flow.inputSensors.first { it.sensorKey == "cabin_mic" }.currentValue)
        assertEquals("120m", flow.inputSensors.first { it.sensorKey == "tunnel" }.currentValue)
        assertTrue(flow.gleoTtsScript.isNotBlank())
    }
}

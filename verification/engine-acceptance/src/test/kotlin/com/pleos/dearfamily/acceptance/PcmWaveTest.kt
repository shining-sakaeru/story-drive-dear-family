package com.pleos.dearfamily.acceptance

import com.pleos.dearfamily.policy.audio.PcmWave
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.*
import org.junit.Test

class PcmWaveTest {
    @Test fun malformedWavAndOutOfPolicyDelayAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { PcmWave.prependSilence(ByteArray(44)) }
        assertThrows(IllegalArgumentException::class.java) { PcmWave.prependSilence(wave(), 149) }
        assertThrows(IllegalArgumentException::class.java) { PcmWave.prependSilence(wave(), 201) }
        val truncated = wave().copyOf(47)
        assertThrows(IllegalArgumentException::class.java) { PcmWave.prependSilence(truncated) }
    }
    @Test fun unsignedEightBitPcmUsesNeutralMidpointNotZero() {
        val input = wave()
        ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(28, 16000); putShort(32, 1); putShort(34, 8)
        }
        val output = PcmWave.prependSilence(input)
        assertTrue(output.copyOfRange(44, output.size - 4).all { it == 0x80.toByte() })
        assertArrayEquals(input.takeLast(4).toByteArray(), output.takeLast(4).toByteArray())
    }
    private fun wave(): ByteArray = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray()); putInt(40); put("WAVEfmt ".toByteArray()); putInt(16)
        putShort(1); putShort(1); putInt(16000); putInt(32000); putShort(2); putShort(16)
        put("data".toByteArray()); putInt(4); put(byteArrayOf(1, 2, 3, 4))
    }.array()

    @Test fun actualPcmSilencePrecedesUnchangedSpeechData() {
        val original = wave()
        val padded = PcmWave.prependSilence(original, 175)
        val buffer = ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(5604, buffer.getInt(40))
        assertEquals(padded.size - 8, buffer.getInt(4))
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), padded.takeLast(4).toByteArray())
        assertTrue(padded.copyOfRange(44, padded.size - 4).all { it == 0.toByte() })
        assertEquals(48, original.size)
    }
}

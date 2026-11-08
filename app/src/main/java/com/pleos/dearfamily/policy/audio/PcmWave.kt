package com.pleos.dearfamily.policy.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Inserts real silent PCM frames, not a scheduling delay. Unsupported audio fails closed. */
object PcmWave {
    const val MAX_BYTES = 16 * 1024 * 1024
    fun prependSilence(input: ByteArray, milliseconds: Int = 175): ByteArray {
        require(milliseconds in 150..200) { "Pre-roll must be 150–200ms" }
        require(input.size in 44..MAX_BYTES) { "Invalid WAV size" }
        val source = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN)
        fun tag(offset: Int) = String(input, offset, 4, Charsets.US_ASCII)
        require(tag(0) == "RIFF" && tag(8) == "WAVE") { "PCM RIFF/WAVE required" }
        require(source.getInt(4).toLong() + 8 == input.size.toLong()) { "Truncated RIFF" }
        var cursor = 12
        var fmt = -1
        var data = -1
        var dataSize = 0
        while (cursor + 8 <= input.size) {
            val size = source.getInt(cursor + 4)
            require(size >= 0 && cursor.toLong() + 8 + size <= input.size) { "Invalid WAV chunk" }
            when (tag(cursor)) {
                "fmt " -> { require(fmt == -1 && size >= 16); fmt = cursor + 8 }
                "data" -> { require(data == -1); data = cursor + 8; dataSize = size }
            }
            val next = cursor.toLong() + 8 + size + (size and 1)
            require(next <= input.size) { "Missing chunk alignment" }
            cursor = next.toInt()
        }
        require(cursor == input.size && fmt >= 0 && data >= 0) { "Missing PCM format/data" }
        val format = source.getShort(fmt).toInt() and 0xffff
        val channels = source.getShort(fmt + 2).toInt() and 0xffff
        val rate = source.getInt(fmt + 4)
        val byteRate = source.getInt(fmt + 8)
        val align = source.getShort(fmt + 12).toInt() and 0xffff
        val bits = source.getShort(fmt + 14).toInt() and 0xffff
        require(format == 1 && channels in 1..2 && rate in 8000..96000 && bits in listOf(8, 16, 24, 32)) { "Unsupported PCM" }
        require(align == channels * bits / 8 && byteRate == rate * align && dataSize % align == 0) { "Invalid PCM alignment" }
        val silenceSize = (rate.toLong() * milliseconds / 1000 * align).toInt()
        val oldPad = dataSize and 1
        val newPad = (dataSize + silenceSize) and 1
        val delta = silenceSize + newPad - oldPad
        require(input.size.toLong() + delta <= MAX_BYTES) { "Padded audio too large" }
        val output = ByteArray(input.size + delta)
        input.copyInto(output, 0, 0, data)
        if (bits == 8) output.fill(0x80.toByte(), data, data + silenceSize)
        input.copyInto(output, data + silenceSize, data, data + dataSize)
        input.copyInto(output, data + silenceSize + dataSize + newPad, data + dataSize + oldPad)
        val target = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN)
        target.putInt(4, output.size - 8)
        target.putInt(data - 4, dataSize + silenceSize)
        return output
    }
}

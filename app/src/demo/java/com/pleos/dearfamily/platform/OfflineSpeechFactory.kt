package com.pleos.dearfamily.platform

import android.content.Context

/** Demo deliberately keeps AudioNarrator's Android TTS -> PCM -> MediaPlayer path. */
object OfflineSpeechFactory {
    @Suppress("UNUSED_PARAMETER")
    fun create(context: Context, listener: OfflineSpeechAdapter.Listener): OfflineSpeechAdapter? = null
}

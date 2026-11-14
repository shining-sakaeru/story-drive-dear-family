package com.pleos.dearfamily.platform

import android.media.AudioAttributes
import java.util.Locale

/** Optional flavor-specific speech backend; null means the standard Android path remains active. */
interface OfflineSpeechAdapter {
    interface Listener {
        fun onReady()
        fun onStart(requestToken: Long)
        fun onDone(requestToken: Long)
        fun onStop(requestToken: Long)
        fun onError(requestToken: Long, detail: String)
    }

    fun initialize()
    fun setLocale(locale: Locale)
    fun setAudioAttributes(attributes: AudioAttributes)
    fun speak(text: String, requestToken: Long, isFinal: Boolean = true)
    fun stop()
    fun release()
}

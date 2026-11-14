package com.pleos.dearfamily.platform

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.media.AudioAttributes
import java.util.Locale

/** Pleos-only ON_DEVICE adapter. It never registers an app or uses a server. */
class PleosOfflineSpeechAdapter(
    context: Context,
    private val listener: OfflineSpeechAdapter.Listener
) : OfflineSpeechAdapter {
    private val speech = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    private var generation = 0L
    private var initialized = false
    private var released = false
    private var eventGeneration = 0L
    private var registeredListener: EventListener? = null
    private var requestedLocale: Locale? = null
    private var requestedAttributes: AudioAttributes? = null
    private var initializing = false

    override fun initialize() {
        if (released || initialized || initializing) return
        initializing = true
        try {
            speech.initialize(object : OnInitializeListener {
                override fun onSuccess() {
                    if (released) return
                    try {
                        replaceListener(0L)
                        requestedLocale?.let { speech.setLocale(it) }
                        requestedAttributes?.let { speech.setAudioAttributes(it) }
                        initialized = true
                        initializing = false
                        listener.onReady()
                    } catch (e: Exception) {
                        initializing = false
                        listener.onError(0L, "초기화 설정 실패: ${e.javaClass.simpleName}")
                    }
                }
                override fun onFailure(errorType: ErrorType) {
                    initializing = false
                    if (!released) listener.onError(0L, "초기화 실패: ${errorType.name}")
                }
            })
        } catch (e: Exception) {
            initializing = false
            if (!released) listener.onError(0L, "초기화 호출 실패: ${e.javaClass.simpleName}")
        } catch (e: LinkageError) {
            initializing = false
            if (!released) listener.onError(0L, "SDK 연결 실패: ${e.javaClass.simpleName}")
        }
    }

    override fun setLocale(locale: Locale) {
        if (released) return
        requestedLocale = locale
        if (initialized) runCatching { speech.setLocale(locale) }
            .onFailure { listener.onError(0L, "언어 설정 실패: ${it.javaClass.simpleName}") }
    }

    override fun setAudioAttributes(attributes: AudioAttributes) {
        if (released) return
        requestedAttributes = attributes
        if (initialized) runCatching { speech.setAudioAttributes(attributes) }
            .onFailure { listener.onError(0L, "오디오 속성 설정 실패: ${it.javaClass.simpleName}") }
    }

    override fun speak(text: String, requestToken: Long, isFinal: Boolean) {
        if (released || !initialized || text.isBlank() || requestToken == 0L) return
        generation = requestToken
        eventGeneration = requestToken
        replaceListener(requestToken)
        try {
            speech.speak(text, isFinal)
        } catch (e: Exception) {
            listener.onError(requestToken, "합성 호출 실패: ${e.javaClass.simpleName}")
        }
    }

    override fun stop() {
        if (released) return
        ++generation
        eventGeneration = 0L
        runCatching { speech.stop() }
    }

    override fun release() {
        if (released) return
        released = true
        ++generation
        eventGeneration = 0L
        runCatching { speech.stop() }
        registeredListener?.let { runCatching { speech.removeEventListener(it) } }
        registeredListener = null
        runCatching { speech.release() }
    }

    private fun replaceListener(generation: Long) {
        registeredListener?.let { runCatching { speech.removeEventListener(it) } }
        val next = object : EventListener {
            override fun onReady() = Unit // Connection readiness is emitted once by initialize().
            override fun onStart() = ifCurrent(generation) { listener.onStart(generation) }
            override fun onUpdatedRms(rms: Double) = Unit
            override fun onDone() = ifCurrent(generation) { listener.onDone(generation) }
            override fun onStop() = ifCurrent(generation) { listener.onStop(generation) }
            override fun onError(errorType: ErrorType) = ifCurrent(generation) { listener.onError(generation, errorType.name) }
        }
        registeredListener = next
        speech.addEventListener(next)
    }

    private inline fun ifCurrent(callbackGeneration: Long, block: () -> Unit) {
        if (!released && callbackGeneration != 0L && callbackGeneration == generation && eventGeneration == generation) block()
    }
}

package com.pleos.dearfamily.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.pleos.dearfamily.policy.audio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

data class VoiceStatus(val message: String = "한국어 오프라인 음성 초기화 중", val ready: Boolean = false,
    val speaking: Boolean = false, val muted: Boolean = false)

/** Offline synthesis -> PCM silence padding -> focus-protected device speaker demonstration. */
class AudioNarrator(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutable = MutableStateFlow(VoiceStatus())
    val status = mutable.asStateFlow()
    private val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private var paddingJob: Job? = null
    private val clips = mutableMapOf<Long, Pair<File, File>>()
    private var initialized = false
    private var released = false
    private var allowed = false
    private var sleep = false
    private var pending: Pair<Long, String>? = null
    private val session: NarrationSession by lazy { NarrationSession(object : NarrationBackend {
        override fun synthesize(token: Long, text: String) {
            val directory = File(app.cacheDir, "dear-family-voice").apply { mkdirs() }
            val raw = File.createTempFile("voice-$token-", ".wav", directory)
            val padded = File.createTempFile("preroll-$token-", ".wav", directory)
            clips[token] = raw to padded
            check(tts?.synthesizeToFile(text, null, raw, token.toString()) == TextToSpeech.SUCCESS)
        }
        override fun requestFocus(): Boolean {
            val result = audio.requestAudioFocus(focusRequest)
            Log.d("DearFamilyAudio", "focus usage=MEDIA content=SPEECH gain=TRANSIENT_MAY_DUCK result=$result")
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        override fun abandonFocus() {
            runCatching { audio.abandonAudioFocusRequest(focusRequest) }
            Log.d("DearFamilyAudio", "focus abandoned")
        }
        override fun play(token: Long) {
            val clip = checkNotNull(clips[token]).second
            val media = MediaPlayer()
            player = media
            media.setAudioAttributes(attributes)
            media.setOnPreparedListener {
                if (session.token != token || !allowed || released) { it.release(); return@setOnPreparedListener }
                try { it.start(); session.onPlaying(token) } catch (_: RuntimeException) { session.onFinished(token, true) }
            }
            media.setOnCompletionListener { session.onFinished(token) }
            media.setOnErrorListener { _, _, _ -> session.onFinished(token, true); true }
            media.setDataSource(clip.absolutePath)
            media.prepareAsync()
        }
        override fun cancel() {
            paddingJob?.cancel(); paddingJob = null
            runCatching { tts?.stop() }
            runCatching { player?.release() }; player = null
            clips.values.forEach { (raw, padded) -> raw.delete(); padded.delete() }
            clips.clear()
        }
        override fun setVolume(volume: Float) { runCatching { player?.setVolume(volume, volume) } }
    }) { phase ->
        if (!released) mutable.value = mutable.value.copy(speaking = phase == PlaybackPhase.PLAYING,
            message = when {
                sleep -> "수면 케어 · 무음 / TTS 중지"
                phase == PlaybackPhase.DENIED -> "오디오 포커스 거절 · 무음/자막 유지"
                phase == PlaybackPhase.ERROR -> "음성 준비/재생 오류 · 자막 유지"
                phase == PlaybackPhase.SYNTHESIZING -> "오프라인 음성 합성 중"
                phase == PlaybackPhase.PREPARING -> "175ms 선행 묵음 · 출력 준비"
                phase == PlaybackPhase.PLAYING -> "한국어 브리핑 · 기기 공용 스피커"
                phase == PlaybackPhase.BLOCKED -> "주행·백그라운드 보호 · 음성 중지"
                mutable.value.muted -> "시연 음성 음소거"
                mutable.value.ready -> "오프라인 한국어 음성 준비됨"
                initialized -> "오프라인 한국어 음성 사용 불가 · 자막 시연"
                else -> "한국어 오프라인 음성 초기화 중"
            })
    }
    }
    private val focusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes).setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener({ change ->
                Log.d("DearFamilyAudio", "focus change=$change")
                val event = when (change) {
                    AudioManager.AUDIOFOCUS_GAIN -> FocusEvent.GAIN
                    AudioManager.AUDIOFOCUS_LOSS -> FocusEvent.LOSS
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> FocusEvent.TRANSIENT_LOSS
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> FocusEvent.DUCK
                    else -> null
                }
                if (event != null) session.onFocusChange(event)
            }, main).build()
    }

    init { tts = TextToSpeech(app) { result -> main.post { configure(result) } } }
    private fun configure(result: Int) {
        if (released) return
        initialized = true
        val engine = tts
        val voice = if (result == TextToSpeech.SUCCESS) runCatching {
            engine?.voices?.filter { it.locale.language == Locale.KOREAN.language && !it.isNetworkConnectionRequired &&
                !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }?.sortedBy { it.name }?.firstOrNull()
        }.getOrNull() else null
        if (engine == null || voice == null || engine.setVoice(voice) != TextToSpeech.SUCCESS) {
            pending = null
            mutable.value = mutable.value.copy(ready = false, speaking = false,
                message = if (sleep) "수면 케어 · 무음 / 한국어 음성 미설치" else "오프라인 한국어 음성 미설치 · 자막 시연")
            return
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) = Unit // Synthesis is not audible playback.
            override fun onDone(id: String?) { id?.toLongOrNull()?.let { token -> main.post { padAndPrepare(token) } } }
            @Deprecated("Platform callback") override fun onError(id: String?) { failed(id) }
            override fun onError(id: String?, code: Int) { failed(id) }
            private fun failed(id: String?) { id?.toLongOrNull()?.let { token -> main.post { session.onFinished(token, true) } } }
        })
        mutable.value = mutable.value.copy(ready = true, message = if (sleep) "수면 케어 · 무음 / TTS 중지" else "오프라인 한국어 음성 준비됨")
        pending?.let { latest -> pending = null; if (allowed) speak(latest.first, latest.second) }
    }
    private fun padAndPrepare(token: Long) {
        if (released || token != session.token || !allowed) return
        if (paddingJob?.isActive == true) return
        val pair = clips[token] ?: return
        paddingJob = scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    require(pair.first.length() <= PcmWave.MAX_BYTES)
                    pair.second.writeBytes(PcmWave.prependSilence(pair.first.readBytes(), 175))
                }
                if (token == session.token && allowed) session.onSynthesized(token)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { session.onFinished(token, true) }
            finally { if (token != session.token) { pair.first.delete(); pair.second.delete() } }
        }
    }
    fun setAllowed(value: Boolean) {
        allowed = value
        if (!value) pending = null
        session.setAllowed(value)
    }
    fun speak(eventId: Long, text: String) {
        if (released) return
        sleep = false
        pending = null
        session.stop()
        if (!allowed || mutable.value.muted || text.isBlank()) return
        if (!initialized) { pending = eventId to text; return }
        if (!mutable.value.ready) return
        session.speak(text)
    }
    fun silenceForSleep() {
        pending = null; sleep = true; session.stop()
        mutable.value = mutable.value.copy(speaking = false, message = "수면 케어 · 무음 / TTS 중지")
    }
    fun stop() { pending = null; session.stop() }
    fun toggleMute() {
        val mute = !mutable.value.muted
        pending = null
        mutable.value = mutable.value.copy(muted = mute)
        session.setMuted(mute)
    }
    fun release() {
        if (released) return
        pending = null; session.stop(); released = true
        tts?.shutdown(); tts = null; scope.cancel()
    }
}

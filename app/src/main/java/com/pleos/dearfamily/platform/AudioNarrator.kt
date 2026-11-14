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
import com.pleos.dearfamily.model.NarrationFeedback
import com.pleos.dearfamily.model.NarrationOutcome
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val feedbackEvents = MutableSharedFlow<NarrationFeedback>(extraBufferCapacity = 16)
    val feedback = feedbackEvents.asSharedFlow()
    private val feedbackTracker = NarrationFeedbackTracker()
    private val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private val offline: OfflineSpeechAdapter? = OfflineSpeechFactory.create(app, object : OfflineSpeechAdapter.Listener {
        override fun onReady() { Log.d("DearFamilyAudio", "Pleos callback=ready"); main.post { configureOfflineReady() } }
        override fun onStart(requestToken: Long) { Log.d("DearFamilyAudio", "Pleos callback=start"); main.post { if (offlineCallbackCurrent(requestToken)) session.onPlaying(requestToken) } }
        override fun onDone(requestToken: Long) { Log.d("DearFamilyAudio", "Pleos callback=done"); main.post { if (offlineCallbackCurrent(requestToken) && session.phase == PlaybackPhase.PLAYING) { emit(requestToken, NarrationOutcome.COMPLETED); session.onFinished(requestToken) } } }
        override fun onStop(requestToken: Long) { Log.d("DearFamilyAudio", "Pleos callback=stop"); main.post { if (offlineCallbackCurrent(requestToken) && session.phase == PlaybackPhase.PLAYING) { emit(requestToken, NarrationOutcome.CANCELLED); session.onFinished(requestToken) } } }
        override fun onError(requestToken: Long, detail: String) { Log.d("DearFamilyAudio", "Pleos callback=error type=$detail"); main.post { if (!released && (requestToken == 0L || offlineCallbackCurrent(requestToken))) { mutable.value = mutable.value.copy(ready = false, speaking = false, message = "Pleos 음성 오류 · 자막 유지 ($detail)"); if (requestToken != 0L) { emit(requestToken, NarrationOutcome.FAILED); session.onFinished(requestToken, true) } else { initialized = true; pending = null; emitCurrent(NarrationOutcome.UNAVAILABLE) } } } }
    })
    private var paddingJob: Job? = null
    private val clips = mutableMapOf<Long, Pair<File, File>>()
    private var initialized = false
    private var released = false
    private var allowed = false
    private var sleep = false
    private var pending: Pair<Long, String>? = null
    private var offlineText: String? = null
    private var currentToken: Long? = null
    private fun emit(token: Long, outcome: NarrationOutcome) {
        if (currentToken != token) return
        currentToken = null
        feedbackTracker.finishToken(token, outcome)?.let(feedbackEvents::tryEmit)
    }
    private fun emitCurrent(outcome: NarrationOutcome) {
        feedbackTracker.finishCurrent(outcome)?.let(feedbackEvents::tryEmit)
    }
    private fun beginEvent(eventId: Long) {
        feedbackTracker.begin(eventId).forEach(feedbackEvents::tryEmit)
    }
    private fun invalidateTokens() { currentToken = null }
    private fun startSpeech(eventId: Long, text: String, resumingPending: Boolean) {
        if (!resumingPending) beginEvent(eventId)
        session.speak(text)
        if (session.phase == PlaybackPhase.ERROR) {
            invalidateTokens()
            emitCurrent(NarrationOutcome.FAILED)
        }
    }
    private val session: NarrationSession by lazy { NarrationSession(object : NarrationBackend {
        override fun synthesize(token: Long, text: String) {
            currentToken = token
            feedbackTracker.bind(token)
            if (offline != null) {
                offlineText = text
                main.post { if (!released && allowed && session.token == token && mutable.value.ready) session.onSynthesized(token) }
                return
            }
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
            if (offline != null) {
                offline.speak(checkNotNull(offlineText), token)
                return
            }
            val clip = checkNotNull(clips[token]).second
            val media = MediaPlayer()
            player = media
            media.setAudioAttributes(attributes)
            media.setOnPreparedListener {
                if (session.token != token || !allowed || released) { it.release(); return@setOnPreparedListener }
                try { it.start(); session.onPlaying(token) } catch (_: RuntimeException) { emit(token, NarrationOutcome.FAILED); session.onFinished(token, true) }
            }
            media.setOnCompletionListener { emit(token, NarrationOutcome.COMPLETED); session.onFinished(token) }
            media.setOnErrorListener { _, _, _ -> emit(token, NarrationOutcome.FAILED); session.onFinished(token, true); true }
            media.setDataSource(clip.absolutePath)
            media.prepareAsync()
        }
        override fun cancel() {
            paddingJob?.cancel(); paddingJob = null
            offlineText = null
            runCatching { offline?.stop() }
            runCatching { tts?.stop() }
            runCatching { player?.release() }; player = null
            clips.values.forEach { (raw, padded) -> raw.delete(); padded.delete() }
            clips.clear()
        }
        override fun setVolume(volume: Float) { runCatching { player?.setVolume(volume, volume) } }
    }) { phase ->
        when (phase) {
            PlaybackPhase.DENIED -> emitCurrent(NarrationOutcome.CANCELLED)
            PlaybackPhase.ERROR -> emitCurrent(NarrationOutcome.FAILED)
            else -> Unit
        }
        if (!released) mutable.value = mutable.value.copy(speaking = phase == PlaybackPhase.PLAYING,
            message = when {
                sleep -> "수면 케어 · 무음 / TTS 중지"
                phase == PlaybackPhase.DENIED -> "오디오 포커스 거절 · 무음/자막 유지"
                phase == PlaybackPhase.ERROR -> "음성 준비/재생 오류 · 자막 유지"
                phase == PlaybackPhase.SYNTHESIZING -> "오프라인 음성 합성 중"
                phase == PlaybackPhase.PREPARING -> if (offline != null) "Pleos ON_DEVICE 출력 준비" else "175ms 선행 묵음 · 출력 준비"
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
                if (event != null) {
                    if (event == FocusEvent.DUCK && offline != null) {
                        Log.d("DearFamilyAudio", "Pleos focus duck -> stop and subtitle")
                        emitCurrent(NarrationOutcome.CANCELLED)
                        invalidateTokens()
                        session.stop(PlaybackPhase.BLOCKED)
                        mutable.value = mutable.value.copy(speaking = false,
                            message = "Pleos 오디오 간섭 · 안전 중지 / 자막 유지")
                    } else {
                        if (event == FocusEvent.LOSS || event == FocusEvent.TRANSIENT_LOSS) {
                            emitCurrent(NarrationOutcome.CANCELLED)
                            invalidateTokens()
                        }
                        session.onFocusChange(event)
                    }
                }
            }, main).build()
    }

    init {
        if (offline != null) {
            offline.setLocale(Locale.KOREA)
            offline.setAudioAttributes(attributes)
            offline.initialize()
        } else {
            tts = TextToSpeech(app) { result -> main.post { configure(result) } }
        }
    }
    private fun configureOfflineReady() {
        if (released) return
        initialized = true
        mutable.value = mutable.value.copy(ready = true, message = if (sleep) "수면 케어 · 무음 / TTS 중지" else "Pleos ON_DEVICE 음성 서비스 연결됨")
        pending?.let { latest -> pending = null; if (allowed) speakInternal(latest.first, latest.second, true) }
    }
    private fun offlineCallbackCurrent(requestToken: Long): Boolean =
        !released && allowed && !sleep && !mutable.value.muted && session.token == requestToken && session.phase == PlaybackPhase.PREPARING ||
            (!released && allowed && !sleep && !mutable.value.muted && session.token == requestToken && session.phase == PlaybackPhase.PLAYING)

    private fun configure(result: Int) {
        if (released) return
        initialized = true
        if (result != TextToSpeech.SUCCESS) {
            pending = null
            mutable.value = mutable.value.copy(ready = false, speaking = false,
                message = "Android TTS 초기화 실패($result) · 엔진/서비스 상태 확인 · 자막 시연")
            emitCurrent(NarrationOutcome.UNAVAILABLE)
            return
        }
        val engine = tts
        val voice = if (result == TextToSpeech.SUCCESS) runCatching {
            engine?.voices?.filter { it.locale.language == Locale.KOREAN.language && !it.isNetworkConnectionRequired &&
                !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }?.sortedBy { it.name }?.firstOrNull()
        }.getOrNull() else null
        if (engine == null || voice == null || engine.setVoice(voice) != TextToSpeech.SUCCESS) {
            pending = null
            mutable.value = mutable.value.copy(ready = false, speaking = false,
                message = if (sleep) "수면 케어 · 무음 / 한국어 음성 미설치" else "오프라인 한국어 음성 미설치 · 자막 시연")
            emitCurrent(NarrationOutcome.UNAVAILABLE)
            return
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) = Unit // Synthesis is not audible playback.
            override fun onDone(id: String?) { id?.toLongOrNull()?.let { token -> main.post { padAndPrepare(token) } } }
            @Deprecated("Platform callback") override fun onError(id: String?) { failed(id) }
            override fun onError(id: String?, code: Int) { failed(id) }
            private fun failed(id: String?) { id?.toLongOrNull()?.let { token -> main.post { emit(token, NarrationOutcome.FAILED); session.onFinished(token, true) } } }
        })
        mutable.value = mutable.value.copy(ready = true, message = if (sleep) "수면 케어 · 무음 / TTS 중지" else "오프라인 한국어 음성 준비됨")
        pending?.let { latest -> pending = null; if (allowed) speakInternal(latest.first, latest.second, true) }
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
        if (!value) { pending = null; emitCurrent(NarrationOutcome.CANCELLED); invalidateTokens() }
        session.setAllowed(value)
    }
    fun speak(eventId: Long, text: String) = speakInternal(eventId, text, false)

    private fun speakInternal(eventId: Long, text: String, resumingPending: Boolean) {
        if (released) return
        sleep = false
        pending = null
        if (!resumingPending) emitCurrent(NarrationOutcome.CANCELLED)
        invalidateTokens()
        session.stop()
        if (text.isBlank()) return
        if (!allowed || mutable.value.muted) {
            beginEvent(eventId)
            emitCurrent(NarrationOutcome.CANCELLED)
            return
        }
        if (!initialized) { beginEvent(eventId); pending = eventId to text; return }
        if (!mutable.value.ready) { beginEvent(eventId); emitCurrent(NarrationOutcome.UNAVAILABLE); return }
        startSpeech(eventId, text, resumingPending)
    }
    fun silenceForSleep() {
        pending = null; emitCurrent(NarrationOutcome.CANCELLED); invalidateTokens(); sleep = true; session.stop()
        mutable.value = mutable.value.copy(speaking = false, message = "수면 케어 · 무음 / TTS 중지")
    }
    fun stop() { pending = null; emitCurrent(NarrationOutcome.CANCELLED); invalidateTokens(); session.stop() }
    fun toggleMute() {
        val mute = !mutable.value.muted
        pending = null
        if (mute) { emitCurrent(NarrationOutcome.CANCELLED); invalidateTokens() }
        mutable.value = mutable.value.copy(muted = mute)
        session.setMuted(mute)
    }
    fun release() {
        if (released) return
        pending = null; emitCurrent(NarrationOutcome.CANCELLED); invalidateTokens(); session.stop(); released = true
        offline?.release()
        tts?.shutdown(); tts = null; scope.cancel()
    }
}

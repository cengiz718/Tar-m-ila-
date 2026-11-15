package com.tarimmeteoroloji.ai.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

interface VoiceSearchCallbacks {
    fun onReadyForSpeech()
    fun onRmsChanged(rmsdB: Float)
    fun onPartialText(text: String)
    fun onFinalText(text: String)
    fun onError(message: String, shouldOfferSystemIntentFallback: Boolean)
}

class VoiceRecognitionController(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: Pair<String, String>? = null
    private var speechRate: Float = 1.0f

    private val utteranceToSpeechId = ConcurrentHashMap<String, String>()
    private var currentSpeechId: String? = null
    private var speakingListener: ((Boolean, String?) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech
                    val trLocale = Locale("tr", "TR")
                    val res = engine.setLanguage(trLocale)
                    ttsReady = res != TextToSpeech.LANG_MISSING_DATA &&
                        res != TextToSpeech.LANG_NOT_SUPPORTED
                    if (!ttsReady) {
                        engine.setLanguage(Locale.getDefault())
                        ttsReady = true
                    }
                    engine.setSpeechRate(speechRate)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            val sid = utteranceId?.let { utteranceToSpeechId[it] } ?: currentSpeechId
                            notifySpeakingState(true, sid)
                        }

                        override fun onDone(utteranceId: String?) {
                            if (utteranceId != null) {
                                utteranceToSpeechId.remove(utteranceId)
                            }
                            if (utteranceToSpeechId.isEmpty()) {
                                currentSpeechId = null
                                notifySpeakingState(false, null)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            if (utteranceId != null) {
                                utteranceToSpeechId.remove(utteranceId)
                            }
                            if (utteranceToSpeechId.isEmpty()) {
                                currentSpeechId = null
                                notifySpeakingState(false, null)
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            if (utteranceId != null) {
                                utteranceToSpeechId.remove(utteranceId)
                            }
                            if (utteranceToSpeechId.isEmpty()) {
                                currentSpeechId = null
                                notifySpeakingState(false, null)
                            }
                        }
                    })

                    pendingSpeech?.let { (sid, queued) ->
                        pendingSpeech = null
                        speak(queued, sid)
                    }
                }
            }
        } catch (_: Exception) {
            ttsReady = false
        }
    }

    private fun notifySpeakingState(speaking: Boolean, speechId: String?) {
        mainHandler.post {
            speakingListener?.invoke(speaking, if (speaking) speechId else null)
        }
    }

    fun setOnSpeakingStateChangeListener(listener: (Boolean, String?) -> Unit) {
        speakingListener = listener
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.75f, 1.4f)
        try {
            tts?.setSpeechRate(speechRate)
        } catch (_: Exception) {
        }
    }

    fun isOnDeviceOrServiceAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(appContext)
        } catch (_: Exception) {
            false
        }
    }

    fun buildRecognizerIntent(prompt: String = "Ürün, hastalık veya ilaç adı söyleyin..."): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
        }
    }

    fun startListening(callbacks: VoiceSearchCallbacks) {
        stopSpeaking()
        if (!isOnDeviceOrServiceAvailable()) {
            callbacks.onError(
                "Doğrudan ses tanıma servisi bulunamadı; sistem ses penceresi açılabilir.",
                shouldOfferSystemIntentFallback = true
            )
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    callbacks.onReadyForSpeech()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    callbacks.onRmsChanged(rmsdB)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val (msg, offerFallback) = when (error) {
                        SpeechRecognizer.ERROR_AUDIO ->
                            "Mikrofon ses kaydı alınamadı." to false
                        SpeechRecognizer.ERROR_CLIENT ->
                            "Ses tanıma iptal edildi." to false
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Mikrofon izni gerekli." to false
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "Ses tanıma için internet veya çevrimdışı Türkçe dil paketi gerekiyor." to true
                        SpeechRecognizer.ERROR_NO_MATCH ->
                            "Ses anlaşılamadı. Lütfen tekrar konuşun (ör. 'Bağ küllemesi')." to false
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                            "Ses tanıma servisi meşgul, tekrar deneyin." to false
                        SpeechRecognizer.ERROR_SERVER ->
                            "Ses sunucusuna ulaşılamadı." to true
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            "Konuşma algılanmadı. Mikrofona dokunup tekrar söyleyin." to false
                        else ->
                            "Ses tanıma başlatılamadı (kod: $error)." to true
                    }
                    callbacks.onError(msg, offerFallback)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        .orEmpty()
                    val best = matches.firstOrNull()?.trim().orEmpty()
                    if (best.isNotEmpty()) {
                        callbacks.onFinalText(best)
                    } else {
                        callbacks.onError(
                            "Ses anlaşılamadı. Lütfen tekrar deneyin.",
                            shouldOfferSystemIntentFallback = false
                        )
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (partial.isNotEmpty()) {
                        callbacks.onPartialText(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(buildRecognizerIntent())
        } catch (_: Exception) {
            callbacks.onError(
                "Ses tanıma başlatılamadı, alternatif ses penceresi açılabilir.",
                shouldOfferSystemIntentFallback = true
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    fun toggleSpeak(speechId: String, text: String) {
        if (currentSpeechId == speechId && utteranceToSpeechId.isNotEmpty()) {
            stopSpeaking()
        } else {
            speak(text, speechId)
        }
    }

    fun speak(text: String, speechId: String = "default") {
        val cleanText = cleanSpeechText(text)
        if (cleanText.isEmpty()) return

        if (!ttsReady) {
            pendingSpeech = speechId to cleanText
            return
        }

        val chunks = splitSpeechChunks(cleanText)
        if (chunks.isEmpty()) return

        try {
            utteranceToSpeechId.clear()
            currentSpeechId = speechId
            tts?.setSpeechRate(speechRate)
            val stamp = System.currentTimeMillis()
            chunks.forEachIndexed { idx, chunk ->
                val uttId = "tarim_tts_${stamp}_$idx"
                utteranceToSpeechId[uttId] = speechId
                val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(chunk, mode, null, uttId)
            }
            notifySpeakingState(true, speechId)
        } catch (_: Exception) {
            utteranceToSpeechId.clear()
            currentSpeechId = null
            notifySpeakingState(false, null)
        }
    }

    fun stopSpeaking() {
        utteranceToSpeechId.clear()
        currentSpeechId = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        notifySpeakingState(false, null)
    }

    fun shutdown() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {
        }
        try {
            utteranceToSpeechId.clear()
            currentSpeechId = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }

    companion object {
        fun cleanSpeechText(text: String): String {
            return text
                .replace("•", ". ")
                .replace("\n", ". ")
                .replace("TL/LT", "lira bölü litre")
                .replace("TL/kg", "lira bölü kilogram")
                .replace("TL/Ton", "lira bölü ton")
                .replace("km/s", "kilometre bölü saat")
                .replace(Regex("[^\\p{L}\\p{N}\\p{P}\\s°%/-]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun splitSpeechChunks(cleanText: String, maxChunkLen: Int = 2600): List<String> {
            val normalized = cleanText.trim()
            if (normalized.isEmpty()) return emptyList()
            if (normalized.length <= maxChunkLen) return listOf(normalized)

            val chunks = mutableListOf<String>()
            val sentences = normalized.split(Regex("(?<=[.!?])\\s+"))
            val current = StringBuilder()

            for (sentence in sentences) {
                if (sentence.length > maxChunkLen) {
                    if (current.isNotEmpty()) {
                        chunks.add(current.toString().trim())
                        current.clear()
                    }
                    var start = 0
                    while (start < sentence.length) {
                        val end = (start + maxChunkLen).coerceAtMost(sentence.length)
                        chunks.add(sentence.substring(start, end).trim())
                        start = end
                    }
                } else if (current.length + sentence.length + 1 > maxChunkLen) {
                    if (current.isNotEmpty()) {
                        chunks.add(current.toString().trim())
                        current.clear()
                    }
                    current.append(sentence)
                } else {
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(sentence)
                }
            }
            if (current.isNotEmpty()) {
                chunks.add(current.toString().trim())
            }
            return chunks.filter { it.isNotEmpty() }
        }
    }
}

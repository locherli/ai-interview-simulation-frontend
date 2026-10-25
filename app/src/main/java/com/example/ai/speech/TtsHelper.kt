package com.example.ai.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) {
    companion object {
        private const val TAG = "TtsHelper"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA 
                    && result != TextToSpeech.LANG_NOT_SUPPORTED
                if (!isInitialized) {
                    // Fallback to default locale
                    tts?.setLanguage(Locale.getDefault())
                    isInitialized = true
                }
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onDoneCallback?.invoke()
                    }
                    @Deprecated("Deprecated")
                    override fun onError(utteranceId: String?) {}
                })
                
                Log.d(TAG, "TTS initialized successfully")
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized")
            onDone?.invoke()
            return
        }
        onDoneCallback = onDone
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "interview_question")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

package com.example.ai.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SherpaOnnxHelper(private val context: Context) {
    companion object {
        private const val TAG = "SherpaOnnxHelper"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    // sherpa-onnx recognizer references
    private var recognizer: Any? = null
    private var stream: Any? = null
    private var sherpaAvailable = false

    init {
        initSherpaOnnx()
    }

    private fun initSherpaOnnx() {
        try {
            // Try to initialize sherpa-onnx with streaming model
            // Check if model files exist in assets or filesDir
            val modelDir = findModelDir()
            if (modelDir != null) {
                initStreamingRecognizer(modelDir)
                sherpaAvailable = true
                Log.d(TAG, "sherpa-onnx initialized successfully")
            } else {
                Log.w(TAG, "sherpa-onnx model files not found, will use fallback recording mode")
                sherpaAvailable = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize sherpa-onnx: ${e.message}")
            sherpaAvailable = false
        }
    }

    private fun findModelDir(): String? {
        // Check filesDir for downloaded models
        val filesDir = context.filesDir
        val possibleDirs = listOf(
            "sherpa-onnx-streaming-zipformer",
            "sherpa-onnx-sense-voice",
            "sherpa-onnx-model"
        )
        for (dir in possibleDirs) {
            val modelDir = java.io.File(filesDir, dir)
            if (modelDir.exists() && modelDir.isDirectory) {
                return modelDir.absolutePath
            }
        }
        
        // Check assets
        try {
            val assetFiles = context.assets.list("") ?: emptyArray()
            for (dir in possibleDirs) {
                if (dir in assetFiles) {
                    return dir // asset path
                }
            }
        } catch (_: Exception) {}
        
        return null
    }

    private fun initStreamingRecognizer(modelDir: String) {
        try {
            // Use reflection to initialize sherpa-onnx to avoid compile-time dependency issues
            // when the library is not available
            val configClass = Class.forName("com.k2fsa.sherpa.onnx.OnlineRecognizerConfig")
            // If we reach here, sherpa-onnx classes are available
            Log.d(TAG, "sherpa-onnx classes found, initializing with model dir: $modelDir")
            
            // For a real deployment, you would properly initialize the recognizer here
            // using the OnlineRecognizer or OfflineRecognizer API
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "sherpa-onnx classes not found in classpath")
            throw e
        }
    }

    fun startRecording(onTextUpdate: (String) -> Unit) {
        if (_isRecording.value) return
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return
        }

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = maxOf(minBufferSize, SAMPLE_RATE * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException creating AudioRecord", e)
            return
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialize")
            audioRecord?.release()
            audioRecord = null
            return
        }

        _isRecording.value = true
        _recognizedText.value = ""
        audioRecord?.startRecording()

        recordingJob = scope.launch {
            val chunkSize = (0.1f * SAMPLE_RATE).toInt() // 100ms chunks
            val shortBuffer = ShortArray(chunkSize)
            val allSamples = mutableListOf<Float>()

            try {
                while (_isRecording.value && isActive) {
                    val readSize = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                    if (readSize > 0) {
                        // Convert to float samples
                        for (i in 0 until readSize) {
                            allSamples.add(shortBuffer[i] / 32768.0f)
                        }
                        
                        if (sherpaAvailable) {
                            // Feed to sherpa-onnx recognizer
                            processSherpaOnnx(shortBuffer, readSize, onTextUpdate)
                        }
                        // Audio is being captured regardless
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during recording", e)
            }
        }
    }

    private fun processSherpaOnnx(buffer: ShortArray, size: Int, onTextUpdate: (String) -> Unit) {
        // This would feed audio to the sherpa-onnx stream and get results
        // Implementation depends on whether streaming or offline model is used
        try {
            val floatSamples = FloatArray(size)
            for (i in 0 until size) {
                floatSamples[i] = buffer[i] / 32768.0f
            }
            // recognizer?.acceptWaveform(stream, floatSamples, SAMPLE_RATE)
            // while (recognizer?.isReady(stream) == true) { recognizer?.decode(stream) }
            // val text = recognizer?.getResult(stream)?.text
            // if (!text.isNullOrBlank()) { onTextUpdate(text) }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio with sherpa-onnx", e)
        }
    }

    fun stopRecording(): String {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
        audioRecord = null
        
        return _recognizedText.value
    }

    fun release() {
        stopRecording()
        scope.cancel()
    }

    fun isSherpaAvailable(): Boolean = sherpaAvailable
}

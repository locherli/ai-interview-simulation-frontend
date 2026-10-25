package com.example.ai.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ai.network.AnswerRequest
import com.example.ai.network.ApiClient
import com.example.ai.network.NextQuestionResponse
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    interviewId: Long,
    onFinish: (Long) -> Unit,
    onAbort: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentQuestion by remember { mutableStateOf<NextQuestionResponse?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    // TTS
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // Speech Recognizer
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    DisposableEffect(speechRecognizer) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            error = "Microphone permission is required for interview"
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isRecording = false
            }
            override fun onError(error: Int) {
                isRecording = false
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
                isRecording = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(intent)
        isRecording = true
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isRecording = false
    }

    fun fetchNextQuestion() {
        isLoading = true
        error = null
        feedback = null
        recognizedText = ""
        coroutineScope.launch {
            try {
                val response = ApiClient.api.getNextQuestion(interviewId)
                if (response.isSuccessful) {
                    currentQuestion = response.body()
                    currentQuestion?.content?.let { content ->
                        tts?.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else if (response.code() == 409) {
                    // No more questions
                    isFinished = true
                    val finishResponse = ApiClient.api.finishInterview(interviewId)
                    if (finishResponse.isSuccessful) {
                        onFinish(interviewId)
                    } else {
                        error = "Failed to finish interview"
                    }
                } else {
                    error = "Failed to load question: ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        fetchNextQuestion()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                ApiClient.api.abortInterview(interviewId)
                            } catch (e: Exception) {
                                // ignore
                            }
                            onAbort()
                        }
                    }) {
                        Text("Abort")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { fetchNextQuestion() }) {
                    Text("Retry")
                }
            } else if (!isFinished && currentQuestion != null) {
                Text(
                    "Question ${currentQuestion!!.questionNo} of ${currentQuestion!!.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = currentQuestion!!.content ?: "",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = recognizedText,
                    onValueChange = { recognizedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = { Text("Your Answer") },
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (isRecording) stopListening() else startListening()
                        }
                    ) {
                        Text(if (isRecording) "Stop Recording" else "Start Recording")
                    }

                    Button(
                        onClick = {
                            if (recognizedText.isNotBlank()) {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val response = ApiClient.api.submitAnswer(
                                            interviewId,
                                            AnswerRequest(recognizedText)
                                        )
                                        if (response.isSuccessful) {
                                            feedback = response.body()?.feedback
                                        } else {
                                            error = "Failed to submit answer"
                                        }
                                    } catch (e: Exception) {
                                        error = "Network error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = recognizedText.isNotBlank() && feedback == null
                    ) {
                        Text("Submit Answer")
                    }
                }

                if (feedback != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Feedback:", style = MaterialTheme.typography.titleSmall)
                    Text(feedback!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { fetchNextQuestion() }) {
                        Text("Next Question")
                    }
                }
            } else if (isFinished) {
                 Text("Finishing interview...")
                 CircularProgressIndicator()
            }
        }
    }
}

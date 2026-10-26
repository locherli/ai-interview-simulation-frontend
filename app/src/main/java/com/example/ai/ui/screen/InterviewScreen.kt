package com.example.ai.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ai.speech.SherpaOnnxHelper
import com.example.ai.speech.TtsHelper
import com.example.ai.ui.viewmodel.InterviewPhase
import com.example.ai.ui.viewmodel.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    category: String,
    interviewId: Long? = null,
    interviewViewModel: InterviewViewModel,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val uiState by interviewViewModel.uiState.collectAsState()
    
    // Speech helpers
    val ttsHelper = remember { TtsHelper(context) }
    val sherpaHelper = remember { SherpaOnnxHelper(context) }
    val isRecording by sherpaHelper.isRecording.collectAsState()
    
    // Permission handling
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }
    
    // Abort confirmation dialog
    var showAbortDialog by remember { mutableStateOf(false) }
    
    // Start interview when screen opens
    LaunchedEffect(category, interviewId) {
        if (interviewId != null) {
            interviewViewModel.resumeInterview(interviewId)
        } else {
            interviewViewModel.startInterview(category)
        }
    }
    
    // Auto-speak question when it arrives
    LaunchedEffect(uiState.currentQuestion) {
        uiState.currentQuestion?.let { question ->
            ttsHelper.speak(question.content)
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.release()
            sherpaHelper.release()
        }
    }
    
    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("确认中止") },
            text = { Text("确定要中止本次面试吗？中止后不会生成评分报告。") },
            confirmButton = {
                TextButton(onClick = {
                    showAbortDialog = false
                    ttsHelper.stop()
                    sherpaHelper.stopRecording()
                    interviewViewModel.abortInterview()
                    onExit()
                }) {
                    Text("确认中止", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) {
                    Text("继续面试")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(category, style = MaterialTheme.typography.titleMedium)
                        if (uiState.currentQuestion != null) {
                            Text(
                                "第 ${uiState.currentQuestion!!.questionNo} / ${uiState.currentQuestion!!.totalQuestions} 题",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showAbortDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.phase) {
                InterviewPhase.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("正在准备面试...")
                        }
                    }
                }
                
                InterviewPhase.QUESTION, InterviewPhase.RECORDING -> {
                    // Question display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🤖 面试官提问",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Replay question button
                                IconButton(
                                    onClick = {
                                        uiState.currentQuestion?.let { ttsHelper.speak(it.content) }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "重新播放",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.currentQuestion?.content ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Progress indicator
                    uiState.currentQuestion?.let { q ->
                        LinearProgressIndicator(
                            progress = { q.questionNo.toFloat() / q.totalQuestions.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Recognized text display
                    if (uiState.recognizedText.isNotBlank() || isRecording) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "👤 我的回答",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.recognizedText.isBlank() && isRecording) 
                                        "正在聆听..." else uiState.recognizedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (uiState.recognizedText.isBlank()) 
                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Recording button
                    if (!hasAudioPermission) {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("授予麦克风权限")
                        }
                    } else {
                        // Mic button
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    sherpaHelper.stopRecording()
                                    val text = sherpaHelper.recognizedText.value
                                    interviewViewModel.updateRecognizedText(text)
                                    interviewViewModel.startRecording() // Update phase
                                    // Actually stay in QUESTION phase with text
                                } else {
                                    ttsHelper.stop()
                                    interviewViewModel.startRecording()
                                    sherpaHelper.startRecording { text ->
                                        interviewViewModel.updateRecognizedText(text)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = if (isRecording) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "停止录音" else "开始录音",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (isRecording) "点击停止录音" else "点击开始语音回答",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Submit answer button (visible after recording)
                        if (uiState.recognizedText.isNotBlank() && !isRecording) {
                            Button(
                                onClick = {
                                    sherpaHelper.stopRecording()
                                    interviewViewModel.submitAnswer()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("提交回答")
                            }
                        }
                    }
                }
                
                InterviewPhase.SUBMITTING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("AI正在评分...")
                        }
                    }
                }
                
                InterviewPhase.FEEDBACK -> {
                    // Show feedback for last answer
                    val feedback = uiState.lastFeedback
                    if (feedback != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "AI 评分",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${feedback.score}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "/ 100",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = feedback.feedback,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "已回答 ${feedback.answeredCount} / ${feedback.totalQuestions} 题",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (feedback.answeredCount < feedback.totalQuestions) {
                            Button(
                                onClick = { interviewViewModel.fetchNextQuestion() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("下一题")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        } else {
                            Button(
                                onClick = { interviewViewModel.finishInterview() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("完成面试，生成报告")
                            }
                        }
                    }
                }
                
                InterviewPhase.FINISHING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("AI正在生成面试报告...")
                        }
                    }
                }
                
                InterviewPhase.COMPLETED -> {
                    val result = uiState.interviewResult
                    if (result != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉 面试完成！", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (result.interview.score != null) {
                                    Text(
                                        "总分",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${result.interview.score}",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "/ 100",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        if (!result.interview.summary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "📋 AI 面试总结",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        result.interview.summary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                interviewViewModel.resetState()
                                onExit()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("返回首页")
                        }
                    }
                }
                
                InterviewPhase.ERROR -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                uiState.error ?: "未知错误",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    interviewViewModel.abortInterview()
                                    onExit()
                                }) {
                                    Text("退出面试")
                                }
                                Button(onClick = { interviewViewModel.retryFromError() }) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

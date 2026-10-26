package com.example.ai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai.data.model.InterviewDetailDto
import com.example.ai.data.model.MessageDto
import com.example.ai.ui.viewmodel.InterviewDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewDetailScreen(
    viewModel: InterviewDetailViewModel,
    interviewId: Long,
    onBack: () -> Unit,
    onResume: (Long, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(interviewId) {
        viewModel.loadDetail(interviewId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("面试详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.detail != null -> {
                val detail = uiState.detail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Interview summary card
                    item {
                        InterviewSummaryCard(detail)
                    }
                    
                    // Q&A messages
                    item {
                        Text(
                            "问答记录",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // Group messages by question-answer pairs
                    val messages = detail.messages
                    items(messages) { message ->
                        MessageCard(message)
                    }
                    
                    if (detail.interview.status == "ongoing") {
                        item {
                            Button(
                                onClick = { 
                                    onResume(detail.interview.id, detail.interview.category ?: detail.interview.jobTitle) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .height(50.dp)
                            ) {
                                Text("继续答题")
                            }
                        }
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadDetail(interviewId) }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewSummaryCard(detail: InterviewDetailDto) {
    val interview = detail.interview
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = interview.category ?: interview.jobTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                if (interview.score != null) {
                    Text(
                        text = "${interview.score}分",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "时间: ${interview.createdAt.take(16).replace("T", " ")}",
                style = MaterialTheme.typography.bodySmall
            )
            val statusText = when (interview.status) {
                "ongoing" -> "进行中"
                "finished" -> "已完成"
                "aborted" -> "已中止"
                else -> interview.status
            }
            Text(
                text = "状态: $statusText",
                style = MaterialTheme.typography.bodySmall
            )
            if (interview.difficulty != null) {
                Text(
                    text = "难度: ${"★".repeat(interview.difficulty)}${"☆".repeat(5 - interview.difficulty)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!interview.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AI 总结报告",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interview.summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MessageCard(message: MessageDto) {
    val isQuestion = message.role == "assistant" || message.role == "system"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isQuestion) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isQuestion) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isQuestion) "🤖 面试官" else "👤 我的回答",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )
            if (message.score != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "得分: ${message.score}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

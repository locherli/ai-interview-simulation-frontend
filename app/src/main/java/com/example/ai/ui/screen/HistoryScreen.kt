package com.example.ai.ui.screen

import androidx.compose.foundation.clickable
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
import com.example.ai.data.model.InterviewDto
import com.example.ai.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    onInterviewClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by historyViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        historyViewModel.loadHistory()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("面试历史") },
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
            uiState.interviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无面试记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.interviews) { interview ->
                        InterviewHistoryCard(
                            interview = interview,
                            onClick = { onInterviewClick(interview.id) }
                        )
                    }
                    
                    if (uiState.hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { historyViewModel.loadMore() }) {
                                    Text("加载更多")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(uiState.error!!)
            }
        }
    }
}

@Composable
fun InterviewHistoryCard(
    interview: InterviewDto,
    onClick: () -> Unit
) {
    val statusText = when (interview.status) {
        "ongoing" -> "进行中"
        "finished" -> "已完成"
        "aborted" -> "已中止"
        else -> interview.status
    }
    val statusColor = when (interview.status) {
        "ongoing" -> MaterialTheme.colorScheme.primary
        "finished" -> MaterialTheme.colorScheme.tertiary
        "aborted" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = interview.category ?: interview.jobTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interview.createdAt.take(16).replace("T", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium
                )
                if (interview.score != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${interview.score}分",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

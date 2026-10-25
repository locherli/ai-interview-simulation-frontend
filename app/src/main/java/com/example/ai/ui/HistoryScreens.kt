package com.example.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai.network.ApiClient
import com.example.ai.network.InterviewDetailDto
import com.example.ai.network.InterviewDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onInterviewClick: (Long) -> Unit
) {
    var history by remember { mutableStateOf<List<InterviewDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = ApiClient.api.getHistory(page = 0, size = 50)
                if (response.isSuccessful) {
                    history = response.body()?.content ?: emptyList()
                } else {
                    error = "Failed to load history: ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (history.isEmpty()) {
                Text("No interview history found.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { interview ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInterviewClick(interview.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Job: ${interview.jobTitle ?: "N/A"} (${interview.category ?: "N/A"})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Score: ${interview.score ?: "N/A"}")
                                Text("Status: ${interview.status ?: "N/A"}")
                                Text("Date: ${interview.createdAt ?: "N/A"}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    interviewId: Long,
    onNavigateBack: () -> Unit
) {
    var detail by remember { mutableStateOf<InterviewDetailDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(interviewId) {
        coroutineScope.launch {
            try {
                val response = ApiClient.api.getInterviewDetail(interviewId)
                if (response.isSuccessful) {
                    detail = response.body()
                } else {
                    error = "Failed to load details: ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (detail != null) {
                val interview = detail!!.interview
                val messages = detail!!.messages ?: emptyList()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Score: ${interview?.score ?: "N/A"}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(interview?.summary ?: "No summary available.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Conversation", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        val isUser = message.role == "user"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .align(if (isUser) Alignment.End else Alignment.Start),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isUser) "You" else "Interviewer",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(message.content ?: "")
                                if (isUser && message.score != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Score: ${message.score}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

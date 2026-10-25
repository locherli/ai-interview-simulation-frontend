package com.example.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ai.network.ApiClient
import com.example.ai.network.StartInterviewRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartInterview: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val categories = listOf("Java后端", "前端", "HR通用", "测试", "运维", "计算机网络", "数据结构与算法")
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Interview") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Text("History")
                    }
                    IconButton(onClick = onLogout) {
                        Text("Logout")
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
            Text(
                "Select a category to start your interview:",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable {
                                    isLoading = true
                                    error = null
                                    coroutineScope.launch {
                                        try {
                                            val request = StartInterviewRequest(
                                                jobTitle = category,
                                                category = category,
                                                difficulty = 3
                                            )
                                            val response = ApiClient.api.startInterview(request)
                                            if (response.isSuccessful && response.body() != null) {
                                                onStartInterview(response.body()!!.id)
                                            } else {
                                                error = "Failed to start interview: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            error = "Network error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

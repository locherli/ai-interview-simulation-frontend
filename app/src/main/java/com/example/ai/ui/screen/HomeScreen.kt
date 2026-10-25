package com.example.ai.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class InterviewCategory(
    val name: String,
    val icon: ImageVector,
    val description: String
)

val categories = listOf(
    InterviewCategory("Java后端", Icons.Default.Code, "Spring Boot, MyBatis, 微服务"),
    InterviewCategory("前端", Icons.Default.Web, "HTML, CSS, JavaScript, Vue/React"),
    InterviewCategory("HR通用", Icons.Default.Person, "自我介绍, 职业规划, 沟通能力"),
    InterviewCategory("测试", Icons.Default.BugReport, "测试理论, 自动化测试, 性能测试"),
    InterviewCategory("运维", Icons.Default.Settings, "Linux, Docker, K8s, CI/CD"),
    InterviewCategory("计算机网络", Icons.Default.Wifi, "TCP/IP, HTTP, 网络安全"),
    InterviewCategory("数据结构与算法", Icons.Default.AccountTree, "数组, 链表, 排序, 动态规划")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userEmail: String,
    onCategorySelected: (String) -> Unit,
    onViewHistory: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI模拟面试") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Default.History, contentDescription = "面试历史")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "退出登录")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "欢迎，$userEmail",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "选择面试类型",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategorySelected(category.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: InterviewCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}

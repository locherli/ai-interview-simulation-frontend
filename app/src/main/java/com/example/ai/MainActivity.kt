package com.example.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ai.network.TokenManager
import com.example.ai.ui.theme.AI面试Theme
import com.example.ai.ui.LoginScreen
import com.example.ai.ui.RegisterScreen
import com.example.ai.ui.HomeScreen
import com.example.ai.ui.InterviewScreen
import com.example.ai.ui.HistoryScreen
import com.example.ai.ui.HistoryDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        enableEdgeToEdge()
        setContent {
            AI面试Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = if (TokenManager.getToken().isNullOrEmpty()) "login" else "home"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                onStartInterview = { interviewId ->
                    navController.navigate("interview/$interviewId")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onLogout = {
                    TokenManager.clearToken()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable(
            "interview/{interviewId}",
            arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getLong("interviewId") ?: return@composable
            InterviewScreen(
                interviewId = interviewId,
                onFinish = { id ->
                    navController.navigate("history_detail/$id") {
                        popUpTo("home")
                    }
                },
                onAbort = {
                    navController.popBackStack()
                }
            )
        }
        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onInterviewClick = { interviewId ->
                    navController.navigate("history_detail/$interviewId")
                }
            )
        }
        composable(
            "history_detail/{interviewId}",
            arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getLong("interviewId") ?: return@composable
            HistoryDetailScreen(
                interviewId = interviewId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

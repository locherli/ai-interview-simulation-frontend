package com.example.ai.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ai.ui.screen.*
import com.example.ai.ui.viewmodel.*

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val INTERVIEW = "interview/{category}?interviewId={interviewId}"
    const val HISTORY = "history"
    const val INTERVIEW_DETAIL = "interview_detail/{interviewId}"

    fun interview(category: String, interviewId: Long? = null) = 
        if (interviewId != null) "interview/$category?interviewId=$interviewId" 
        else "interview/$category"
    fun interviewDetail(id: Long) = "interview_detail/$id"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val startDest = if (authState.isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                userEmail = authState.email ?: "",
                onCategorySelected = { category ->
                    navController.navigate(Routes.interview(category))
                },
                onViewHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.INTERVIEW,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("interviewId") { 
                    type = NavType.StringType 
                    nullable = true 
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val interviewIdStr = backStackEntry.arguments?.getString("interviewId")
            val interviewId = interviewIdStr?.toLongOrNull() ?: -1L
            val interviewViewModel: InterviewViewModel = viewModel()
            InterviewScreen(
                category = category,
                interviewId = if (interviewId != -1L) interviewId else null,
                interviewViewModel = interviewViewModel,
                onExit = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                historyViewModel = historyViewModel,
                onInterviewClick = { id ->
                    navController.navigate(Routes.interviewDetail(id))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.INTERVIEW_DETAIL,
            arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getLong("interviewId") ?: 0L
            val detailViewModel: InterviewDetailViewModel = viewModel()
            InterviewDetailScreen(
                viewModel = detailViewModel,
                interviewId = interviewId,
                onBack = {
                    navController.popBackStack()
                },
                onResume = { id, category ->
                    navController.navigate(Routes.interview(category, id)) {
                        popUpTo(Routes.HISTORY)
                    }
                }
            )
        }
    }
}

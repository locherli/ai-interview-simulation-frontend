package com.example.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.model.InterviewDto
import com.example.ai.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val interviews: List<InterviewDto> = emptyList(),
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null
)

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getInterviewHistory(page = 0, size = 20)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _uiState.value = HistoryUiState(
                        interviews = body.content,
                        hasMore = body.page < body.totalPages - 1,
                        currentPage = body.page
                    )
                } else {
                    _uiState.value = HistoryUiState(error = "加载失败: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(error = "网络错误: ${e.localizedMessage}")
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value
        if (current.isLoading || !current.hasMore) return
        
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)
            try {
                val nextPage = current.currentPage + 1
                val response = RetrofitClient.apiService.getInterviewHistory(page = nextPage, size = 20)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _uiState.value = current.copy(
                        isLoading = false,
                        interviews = current.interviews + body.content,
                        hasMore = nextPage < body.totalPages - 1,
                        currentPage = nextPage
                    )
                } else {
                    _uiState.value = current.copy(isLoading = false, error = "加载失败")
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(isLoading = false, error = "网络错误")
            }
        }
    }
}

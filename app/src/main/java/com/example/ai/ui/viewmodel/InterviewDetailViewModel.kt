package com.example.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.model.InterviewDetailDto
import com.example.ai.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InterviewDetailUiState(
    val isLoading: Boolean = false,
    val detail: InterviewDetailDto? = null,
    val error: String? = null
)

class InterviewDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InterviewDetailUiState())
    val uiState: StateFlow<InterviewDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(interviewId: Long) {
        viewModelScope.launch {
            _uiState.value = InterviewDetailUiState(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getInterviewDetail(interviewId)
                if (response.isSuccessful) {
                    _uiState.value = InterviewDetailUiState(detail = response.body())
                } else {
                    _uiState.value = InterviewDetailUiState(error = "加载失败: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = InterviewDetailUiState(error = "网络错误: ${e.localizedMessage}")
            }
        }
    }
}

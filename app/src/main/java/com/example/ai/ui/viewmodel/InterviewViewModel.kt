package com.example.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.model.*
import com.example.ai.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class InterviewPhase {
    LOADING,        // Starting interview
    QUESTION,       // Showing question, waiting for answer
    RECORDING,      // Recording user's voice answer
    SUBMITTING,     // Submitting answer to server
    FEEDBACK,       // Showing AI feedback for last answer
    FINISHING,      // Generating final report
    COMPLETED,      // Interview completed, showing result
    ERROR           // Error state
}

data class InterviewUiState(
    val phase: InterviewPhase = InterviewPhase.LOADING,
    val interviewId: Long? = null,
    val currentQuestion: NextQuestionResponse? = null,
    val lastFeedback: AnswerResponse? = null,
    val interviewResult: InterviewDetailDto? = null,
    val recognizedText: String = "",
    val error: String? = null,
    val questionHistory: List<Pair<String, AnswerResponse?>> = emptyList()
)

class InterviewViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    fun startInterview(category: String) {
        viewModelScope.launch {
            _uiState.value = InterviewUiState(phase = InterviewPhase.LOADING)
            try {
                val request = StartInterviewRequest(
                    jobTitle = category,
                    category = category,
                    difficulty = 3
                )
                val response = RetrofitClient.apiService.startInterview(request)
                if (response.isSuccessful) {
                    val interview = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        interviewId = interview.id
                    )
                    fetchNextQuestion()
                } else {
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.ERROR,
                        error = "开始面试失败: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = InterviewPhase.ERROR,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resumeInterview(id: Long) {
        viewModelScope.launch {
            _uiState.value = InterviewUiState(phase = InterviewPhase.LOADING, interviewId = id)
            fetchNextQuestion()
        }
    }

    fun fetchNextQuestion() {
        val interviewId = _uiState.value.interviewId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = InterviewPhase.LOADING)
            try {
                val response = RetrofitClient.apiService.nextQuestion(interviewId)
                if (response.isSuccessful) {
                    val question = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.QUESTION,
                        currentQuestion = question,
                        recognizedText = "",
                        lastFeedback = null
                    )
                } else if (response.code() == 409) {
                    // All questions asked, finish the interview
                    finishInterview()
                } else {
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.ERROR,
                        error = "获取题目失败: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = InterviewPhase.ERROR,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(phase = InterviewPhase.RECORDING, recognizedText = "")
    }

    fun updateRecognizedText(text: String) {
        _uiState.value = _uiState.value.copy(recognizedText = text)
    }

    fun submitAnswer() {
        val interviewId = _uiState.value.interviewId ?: return
        val answerText = _uiState.value.recognizedText
        if (answerText.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = InterviewPhase.SUBMITTING)
            try {
                val response = RetrofitClient.apiService.submitAnswer(
                    interviewId,
                    AnswerRequest(answerText)
                )
                if (response.isSuccessful) {
                    val feedback = response.body()!!
                    val questionContent = _uiState.value.currentQuestion?.content ?: ""
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.FEEDBACK,
                        lastFeedback = feedback,
                        questionHistory = _uiState.value.questionHistory + Pair(questionContent, feedback)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.ERROR,
                        error = "提交回答失败: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = InterviewPhase.ERROR,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun finishInterview() {
        val interviewId = _uiState.value.interviewId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = InterviewPhase.FINISHING)
            try {
                val response = RetrofitClient.apiService.finishInterview(interviewId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.COMPLETED,
                        interviewResult = response.body()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        phase = InterviewPhase.ERROR,
                        error = "结束面试失败: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = InterviewPhase.ERROR,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun abortInterview() {
        val interviewId = _uiState.value.interviewId ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.abortInterview(interviewId)
            } catch (_: Exception) { }
            _uiState.value = InterviewUiState()
        }
    }

    fun retryFromError() {
        _uiState.value = _uiState.value.copy(phase = InterviewPhase.QUESTION, error = null)
    }

    fun resetState() {
        _uiState.value = InterviewUiState()
    }
}

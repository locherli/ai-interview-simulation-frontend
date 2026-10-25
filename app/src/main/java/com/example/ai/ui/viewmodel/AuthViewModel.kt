package com.example.ai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.data.local.TokenManager
import com.example.ai.data.model.LoginRequest
import com.example.ai.data.model.RegisterRequest
import com.example.ai.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val email: String? = null,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSavedToken()
    }

    private fun checkSavedToken() {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.first()
            val email = tokenManager.emailFlow.first()
            if (token != null) {
                RetrofitClient.setToken(token)
                _uiState.value = AuthUiState(isLoggedIn = true, email = email)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    RetrofitClient.setToken(body.token)
                    tokenManager.saveAuthInfo(body.token, body.email, body.userId)
                    _uiState.value = AuthUiState(isLoggedIn = true, email = body.email)
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "邮箱或密码错误"
                        else -> "登录失败: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = RetrofitClient.apiService.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    RetrofitClient.setToken(body.token)
                    tokenManager.saveAuthInfo(body.token, body.email, body.userId)
                    _uiState.value = AuthUiState(isLoggedIn = true, email = body.email)
                } else {
                    val errorMsg = when (response.code()) {
                        409 -> "该邮箱已被注册"
                        else -> "注册失败: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "网络错误: ${e.localizedMessage}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAuthInfo()
            RetrofitClient.setToken(null)
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

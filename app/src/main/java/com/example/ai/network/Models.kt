package com.example.ai.network

import com.google.gson.annotations.SerializedName

data class StartInterviewRequest(
    val jobTitle: String,
    val category: String? = null,
    val difficulty: Int? = null
)

data class InterviewDto(
    val id: Long,
    val jobTitle: String?,
    val category: String?,
    val difficulty: Int?,
    val status: String?,
    val score: Int?,
    val summary: String?,
    val createdAt: String?,
    val finishedAt: String?
)

data class NextQuestionResponse(
    val messageId: Long?,
    val questionNo: Int?,
    val totalQuestions: Int?,
    val type: String?,
    val content: String?
)

data class AnswerRequest(
    val content: String
)

data class AnswerResponse(
    val score: Int?,
    val feedback: String?,
    val answeredCount: Int?,
    val totalQuestions: Int?
)

data class InterviewDetailDto(
    val interview: InterviewDto?,
    val messages: List<MessageDto>?
)

data class MessageDto(
    val id: Long?,
    val role: String?,
    val content: String?,
    val score: Int?,
    val questionId: Long?,
    val createdAt: String?
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val tokenType: String?,
    val userId: Long?,
    val email: String?
)

data class UserDto(
    val id: Long?,
    val email: String?,
    val createdAt: String?
)

data class PageResponseInterviewDto(
    val content: List<InterviewDto>?,
    val page: Int?,
    val size: Int?,
    val totalElements: Long?,
    val totalPages: Int?
)

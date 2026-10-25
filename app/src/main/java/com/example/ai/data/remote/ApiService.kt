package com.example.ai.data.remote

import com.example.ai.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // User
    @GET("api/users/me")
    suspend fun getMe(): Response<UserDto>

    // Interview
    @POST("api/interviews")
    suspend fun startInterview(@Body request: StartInterviewRequest): Response<InterviewDto>

    @POST("api/interviews/{id}/next-question")
    suspend fun nextQuestion(@Path("id") id: Long): Response<NextQuestionResponse>

    @POST("api/interviews/{id}/answers")
    suspend fun submitAnswer(@Path("id") id: Long, @Body request: AnswerRequest): Response<AnswerResponse>

    @POST("api/interviews/{id}/finish")
    suspend fun finishInterview(@Path("id") id: Long): Response<InterviewDetailDto>

    @POST("api/interviews/{id}/abort")
    suspend fun abortInterview(@Path("id") id: Long): Response<InterviewDto>

    @GET("api/interviews")
    suspend fun getInterviewHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponseInterviewDto>

    @GET("api/interviews/{id}")
    suspend fun getInterviewDetail(@Path("id") id: Long): Response<InterviewDetailDto>
}

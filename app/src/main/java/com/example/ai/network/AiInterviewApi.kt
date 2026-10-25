package com.example.ai.network

import retrofit2.Response
import retrofit2.http.*

interface AiInterviewApi {

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/api/users/me")
    suspend fun getMe(): Response<UserDto>

    @GET("/api/interviews")
    suspend fun getHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponseInterviewDto>

    @POST("/api/interviews")
    suspend fun startInterview(@Body request: StartInterviewRequest): Response<InterviewDto>

    @POST("/api/interviews/{id}/next-question")
    suspend fun getNextQuestion(@Path("id") id: Long): Response<NextQuestionResponse>

    @POST("/api/interviews/{id}/answers")
    suspend fun submitAnswer(
        @Path("id") id: Long,
        @Body request: AnswerRequest
    ): Response<AnswerResponse>

    @POST("/api/interviews/{id}/finish")
    suspend fun finishInterview(@Path("id") id: Long): Response<InterviewDetailDto>

    @POST("/api/interviews/{id}/abort")
    suspend fun abortInterview(@Path("id") id: Long): Response<InterviewDto>

    @GET("/api/interviews/{id}")
    suspend fun getInterviewDetail(@Path("id") id: Long): Response<InterviewDetailDto>
}

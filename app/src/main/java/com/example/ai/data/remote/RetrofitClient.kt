package com.example.ai.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"

    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    fun getToken(): String? = token

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }

    // Connects to localhost:8080 by default. On Android emulators without adb reverse,
    // localhost connects to the Android device itself; fallback smoothly to 10.0.2.2:8080.
    private val hostFallbackInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: ConnectException) {
            val url = request.url
            if (url.host == "localhost") {
                val fallbackUrl = url.newBuilder().host("10.0.2.2").build()
                val fallbackRequest = request.newBuilder().url(fallbackUrl).build()
                chain.proceed(fallbackRequest)
            } else {
                throw e
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(hostFallbackInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

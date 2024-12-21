package dev.ywaychitaung.the_memory_game_kotlin.data.api

import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.LoginRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/users/{userId}/premium")
    suspend fun purchasePremium(@Path("userId") userId: String): Response<Unit>
}

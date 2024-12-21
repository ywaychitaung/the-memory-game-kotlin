package dev.ywaychitaung.the_memory_game_kotlin.data.api

import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.LoginRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

package dev.ywaychitaung.the_memory_game_kotlin.networking

import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.AuthRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.AuthResponse
import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.ScoreRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.ScoreResponse
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("api/users/{userId}/premium")
    suspend fun purchasePremium(@Path("userId") userId: String): Response<Unit>

    @POST("api/scores/create")
    suspend fun createScore(@Body request: ScoreRequest): ScoreResponse

    @GET("api/scores/get")
    suspend fun getScores(): List<ScoreResponse>
}

package dev.ywaychitaung.the_memory_game_kotlin.data.api

import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.LoginRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.LoginResponse
import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.ScoreRequest
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.ScoreResponse
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/users/{userId}/premium")
    suspend fun purchasePremium(@Path("userId") userId: String): Response<Unit>

    @POST("api/scores/create")
    suspend fun createScore(@Body request: ScoreRequest): ScoreResponse

    @GET("api/scores/get")
    suspend fun getScores(): List<ScoreResponse>
}

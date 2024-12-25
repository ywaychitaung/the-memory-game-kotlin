package dev.ywaychitaung.the_memory_game_kotlin.data.model.response

data class AuthResponse(
    val userId: String,
    val username: String,
    val isPaidUser: Boolean
)

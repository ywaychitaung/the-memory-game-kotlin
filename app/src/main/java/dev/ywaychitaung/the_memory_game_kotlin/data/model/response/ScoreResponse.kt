package dev.ywaychitaung.the_memory_game_kotlin.data.model.response

data class ScoreResponse(
    val username: String,
    val totalMoves: Int,
    val totalSeconds: Int,
    val createdAt: String
)

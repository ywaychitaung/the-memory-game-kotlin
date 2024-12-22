package dev.ywaychitaung.the_memory_game_kotlin.data.model.request

data class ScoreRequest(
    val userId: String,
    val totalMoves: Int,
    val totalSeconds: Int
)

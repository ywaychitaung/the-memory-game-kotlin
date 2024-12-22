package dev.ywaychitaung.the_memory_game_kotlin.ui.leaderboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dev.ywaychitaung.the_memory_game_kotlin.data.api.RetrofitClient
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.ScoreResponse
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityLeaderboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchScores()
    }

    private fun fetchScores() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authApi.getScores() // Use authApi from RetrofitClient
                withContext(Dispatchers.Main) {
                    displayScores(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LeaderboardActivity, "Failed to fetch scores.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayScores(scores: List<ScoreResponse>) {
        val topScores = scores.take(5)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ScoreAdapter(topScores)
    }
}

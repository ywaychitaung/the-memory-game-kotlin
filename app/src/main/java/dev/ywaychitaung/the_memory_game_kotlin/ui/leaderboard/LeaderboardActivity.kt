package dev.ywaychitaung.the_memory_game_kotlin.ui.leaderboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dev.ywaychitaung.the_memory_game_kotlin.data.api.RetrofitClient
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.ScoreResponse
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityLeaderboardBinding
import dev.ywaychitaung.the_memory_game_kotlin.ui.fetch.FetchActivity
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(android.R.color.transparent)

        setupUI()
        fetchScores()
    }

    private fun setupUI() {
        binding.goBackButton.setOnClickListener {
            startActivity(Intent(this, FetchActivity::class.java))
            finish()
        }

        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fetchScores() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authApi.getScores()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    displayScores(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@LeaderboardActivity, "Failed to fetch scores.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.titleTextView.visibility = if (show) View.GONE else View.VISIBLE
        binding.leaderboardRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun displayScores(scores: List<ScoreResponse>) {
        val topScores = scores.take(5)
        binding.leaderboardRecyclerView.adapter = LeaderboardAdapter(topScores)
    }
}

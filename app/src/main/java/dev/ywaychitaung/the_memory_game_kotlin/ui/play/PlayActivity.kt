package dev.ywaychitaung.the_memory_game_kotlin.ui.play

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.ywaychitaung.the_memory_game_kotlin.data.api.RetrofitClient
import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.ScoreRequest
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityPlayBinding
import dev.ywaychitaung.the_memory_game_kotlin.ui.leaderboard.LeaderboardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding
    private val handler = Handler(Looper.getMainLooper())
    private var matches = 0
    private var startTime = 0L
    private var totalMoves = 0
    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedImages = intent.getStringArrayListExtra("selectedImages") ?: arrayListOf()

        setupGame(selectedImages)
        startTimer()
    }

    private fun setupGame(images: List<String>) {
        val gameImages = (images + images).shuffled()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = PlayAdapter(gameImages) { matchFound ->
            totalMoves++
            if (matchFound) {
                matches++
                binding.matchesTextView.text = "Matches: $matches of 6"
                if (matches == 6) {
                    endGame()
                }
            }
        }
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                val minutes = elapsedTime / 60
                val seconds = elapsedTime % 60
                binding.timerTextView.text = "Time: %02d:%02d".format(minutes, seconds)

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun endGame() {
        handler.removeCallbacksAndMessages(null)

        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val scoreRequest = ScoreRequest(userId, totalMoves, elapsedTime.toInt())
                val response = try {
                    RetrofitClient.authApi.createScore(scoreRequest)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PlayActivity, "Failed to save score.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    sharedPreferences.edit().putInt("lastGameTime", elapsedTime.toInt()).apply()
                    navigateToLeaderboard()
                }
            }
        }
    }

    private fun navigateToLeaderboard() {
        startActivity(Intent(this, LeaderboardActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

package dev.ywaychitaung.the_memory_game_kotlin.ui.play

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.ywaychitaung.the_memory_game_kotlin.R
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
    private val adHandler = Handler(Looper.getMainLooper())
    private var matches = 0
    private var startTime = 0L
    private var totalMoves = 0
    private var adTimer = 0L
    private var currentAdIndex = 0

    private val adStrings = listOf(
        "Shopee: 11.11 Sale",
        "Lazada: Black Friday Sale",
        "Singapore Airlines: Free Luggage Upgrade (30kg)",
        "Grab: 20% off on all rides",
        "Foodpanda: SGD 3$ off minimum order of SGD 20$",
        "Singtel: 30% off on all plans",
        "Starhub: Unlimited wechat data for 1 month (T&C apply)",
    )

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

        val username = sharedPreferences.getString("username", "Guest")
        val isPaidUser = sharedPreferences.getBoolean("isPaidUser", false)

        binding.usernameTextView.text = "Username: $username"
        binding.userStatusTextView.text = if (isPaidUser) "Premium User" else "Free User"
        binding.userStatusIcon.setImageResource(if (isPaidUser) R.drawable.ic_premium else R.drawable.ic_free)
        binding.adTextView.visibility = if (isPaidUser) View.GONE else View.VISIBLE
        binding.purchasePremiumButton.visibility = if (isPaidUser) View.GONE else View.VISIBLE
        binding.adTextView.text = adStrings[0]

        setupGame(selectedImages)
        startTimer()
        startAdRotation()
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

    private fun startAdRotation() {
        val isPaidUser = sharedPreferences.getBoolean("isPaidUser", false)
        if (isPaidUser) return

        adTimer = System.currentTimeMillis()
        adHandler.post(object : Runnable {
            override fun run() {
                val elapsedTime = (System.currentTimeMillis() - adTimer) / 1000
                if (elapsedTime >= 30) {
                    currentAdIndex = (currentAdIndex + 1) % adStrings.size
                    binding.adTextView.text = adStrings[currentAdIndex]
                    adTimer = System.currentTimeMillis()
                }
                adHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun endGame() {
        handler.removeCallbacksAndMessages(null)
        adHandler.removeCallbacksAndMessages(null)

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
        adHandler.removeCallbacksAndMessages(null)
    }
}

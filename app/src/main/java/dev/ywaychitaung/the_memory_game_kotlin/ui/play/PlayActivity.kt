package dev.ywaychitaung.the_memory_game_kotlin.ui.play

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.ywaychitaung.the_memory_game_kotlin.R
import dev.ywaychitaung.the_memory_game_kotlin.networking.RetrofitClient
import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.ScoreRequest
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityPlayBinding
import dev.ywaychitaung.the_memory_game_kotlin.ui.leaderboard.LeaderboardActivity
import dev.ywaychitaung.the_memory_game_kotlin.ui.login.LoginActivity
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

        // Show Login button for guest users and Logout button for logged-in users
        // Show Login button for guest users and Logout button for logged-in users
        if (username == "Guest") {
            binding.purchasePremiumButton.visibility = View.GONE
            binding.logoutButton.text = "Login"
            binding.logoutButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_700))
            binding.logoutButton.setTextColor(ContextCompat.getColor(this, R.color.white)) // Optional: Change text color to white for better contrast
            binding.logoutButton.setOnClickListener {
                navigateToLogin()
            }
        } else {
            binding.logoutButton.text = "Logout"
            binding.logoutButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red)) // Optional: Set logout button color to red
            binding.logoutButton.setTextColor(ContextCompat.getColor(this, R.color.white)) // Optional: Change text color to white for better contrast
            binding.logoutButton.setOnClickListener {
                showLogoutConfirmationDialog()
            }
        }


        binding.purchasePremiumButton.setOnClickListener {
            handlePremiumPurchase()
        }

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

    private fun showGameCompletionDialog() {
        val username = sharedPreferences.getString("username", "Guest")
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60

        val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        val message = if (username != "Guest") {
            "Congratulations $username!\nYour Time: $timeFormatted"
        } else {
            "Congratulations Guest!\nYour Time: $timeFormatted"
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Game Complete!")
            .setMessage(message)
            .setPositiveButton("Show Leaderboard") { _, _ ->
                navigateToLeaderboard()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun endGame() {
        handler.removeCallbacksAndMessages(null)
        adHandler.removeCallbacksAndMessages(null)

        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null && sharedPreferences.getString("username", "Guest") != "Guest") {
            CoroutineScope(Dispatchers.IO).launch {
                val scoreRequest = ScoreRequest(userId, totalMoves, elapsedTime.toInt())
                try {
                    RetrofitClient.api.createScore(scoreRequest)
                    withContext(Dispatchers.Main) {
                        sharedPreferences.edit().putInt("lastGameTime", elapsedTime.toInt()).apply()
                        showGameCompletionDialog()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PlayActivity, "Failed to save score.", Toast.LENGTH_SHORT).show()
                        showGameCompletionDialog()
                    }
                }
            }
        } else {
            showGameCompletionDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                handleLogout()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun handleLogout() {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showPurchaseSuccessDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Premium Purchase")
            .setMessage("Purchase successful! You are now a premium user.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                binding.adTextView.visibility = View.GONE
                binding.purchasePremiumButton.visibility = View.GONE
                binding.userStatusTextView.text = "Premium User"
                binding.userStatusIcon.setImageResource(R.drawable.ic_premium)
                adHandler.removeCallbacksAndMessages(null)
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun handlePremiumPurchase() {
        val userId = sharedPreferences.getString("userId", null)
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.api.purchasePremium(userId)
                withContext(Dispatchers.Main) {
                    sharedPreferences.edit().putBoolean("isPaidUser", true).apply()
                    showPurchaseSuccessDialog()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlayActivity, "Purchase failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLeaderboard() {
        startActivity(Intent(this, LeaderboardActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        adHandler.removeCallbacksAndMessages(null)
    }
}

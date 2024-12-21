package dev.ywaychitaung.the_memory_game_kotlin.ui.play

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
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityPlayBinding

class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding
    private val handler = Handler(Looper.getMainLooper())
    private var matches = 0
    private var startTime = 0L
    private var isAdFree = false // Assume you have logic to check for paid users
    private val adInterval = 30_000L // 30 seconds in milliseconds

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

        // Retrieve username from EncryptedSharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val username = sharedPreferences.getString("username", "Guest")
        val isPaidUser = sharedPreferences.getBoolean("isPaidUser", false)

        // Display username
        binding.usernameTextView.text = "Username: $username"

        // Set the icon and text based on the user's status
        if (isPaidUser) {
            binding.userStatusIcon.setImageResource(R.drawable.ic_premium)
            binding.userStatusTextView.text = "Premium User"
        } else {
            binding.userStatusIcon.setImageResource(R.drawable.ic_free)
            binding.userStatusTextView.text = "Free User"
        }

        // Show "Purchase Premium" button if user is not paid
        if (!isPaidUser) {
            binding.purchasePremiumButton.visibility = View.VISIBLE
//            binding.purchasePremiumButton.setOnClickListener {
//                purchasePremium()
//            }
        }

        val selectedImages = intent.getStringArrayListExtra("selectedImages") ?: arrayListOf()

        setupGame(selectedImages)
        startTimer()
        if (!isAdFree) startAdCycle()
    }

    private fun setupGame(images: List<String>) {
        // Create a shuffled list of 12 placeholders (6 pairs)
        val gameImages = (images + images).shuffled()

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = PlayAdapter(gameImages) { matchFound ->
            if (matchFound) {
                matches++
                binding.matchesTextView.text = "Matches: $matches of 6"
                if (matches == 6) {
                    Toast.makeText(this, "You won!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60)) % 24

                // Format the time as hh:mm:ss
                val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding.timerTextView.text = "Time: $timeString"

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun startAdCycle() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                displayAd()
                handler.postDelayed(this, adInterval)
            }
        }, adInterval)
    }

    private fun displayAd() {
        // Logic to fetch and display a new ad
        binding.adTextView.text = "This is an ad. [Ad changes every 30 seconds]"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

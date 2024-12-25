package dev.ywaychitaung.the_memory_game_kotlin.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.ywaychitaung.the_memory_game_kotlin.R
import dev.ywaychitaung.the_memory_game_kotlin.networking.RetrofitClient
import dev.ywaychitaung.the_memory_game_kotlin.data.model.request.AuthRequest
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityLoginBinding
import dev.ywaychitaung.the_memory_game_kotlin.ui.fetch.FetchActivity
import dev.ywaychitaung.the_memory_game_kotlin.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible: Boolean = false

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set clickable text for the register link
        val fullText = "Don't have a game account? Create one!"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        val startIndex = fullText.indexOf("Create one!")
        val endIndex = startIndex + "Create one!".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.guestButton.setOnClickListener {
            // Save guest status
            saveToSecureStorage(
                userId = "guest",
                username = "Guest",
                isPaidUser = false
            )

            // Navigate to fetch activity
            startActivity(Intent(this, FetchActivity::class.java))
            finish()
        }

        binding.registerLink.text = spannableString
        binding.registerLink.movementMethod = LinkMovementMethod.getInstance()

        // Handle password visibility toggle
        binding.passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Show password
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility) // Use visible icon
            } else {
                // Hide password
                binding.passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off) // Use invisible icon
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text.length) // Maintain cursor position
        }

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false

                    val response = RetrofitClient.api.login(AuthRequest(username, password))

                    // Save the login response securely
                    saveToSecureStorage(
                        userId = response.userId,
                        username = response.username,
                        isPaidUser = response.isPaidUser
                    )

                    // Login successful
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to the next screen
                    startActivity(Intent(this@LoginActivity, FetchActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    // Handle login failure
                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid Credentials! Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
            }
        }
    }

    private fun saveToSecureStorage(userId: String, username: String, isPaidUser: Boolean) {
        with(sharedPreferences.edit()) {
            putString("userId", userId)
            putString("username", username)
            putBoolean("isPaidUser", isPaidUser)
            apply()
        }
    }
}

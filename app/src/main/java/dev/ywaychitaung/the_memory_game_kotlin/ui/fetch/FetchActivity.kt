package dev.ywaychitaung.the_memory_game_kotlin.ui.fetch

import ImagesAdapter
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dev.ywaychitaung.the_memory_game_kotlin.databinding.ActivityFetchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import dev.ywaychitaung.the_memory_game_kotlin.data.api.WebService
import dev.ywaychitaung.the_memory_game_kotlin.ui.play.PlayActivity
import kotlinx.coroutines.withContext

class FetchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFetchBinding
    private lateinit var retrofit: Retrofit
    private lateinit var webService: WebService
    private var currentJob: Job? = null

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
        binding = ActivityFetchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = sharedPreferences.getString("username", "Guest")
        binding.welcomeTextView.text = "Welcome, $username!"

        setupRetrofit()
        setupRecyclerView()

        binding.fetchButton.setOnClickListener {
            val enteredUrl = binding.urlEditText.text.toString().trim()
            if (TextUtils.isEmpty(enteredUrl)) {
                Toast.makeText(this, "Enter a valid URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure valid URL format
            val url = if (!enteredUrl.startsWith("http://") && !enteredUrl.startsWith("https://")) {
                "https://$enteredUrl"
            } else {
                enteredUrl
            }

            fetchImages(url)
        }

        binding.playButton.setOnClickListener {
            val selectedImages = (binding.imagesRecyclerView.adapter as ImagesAdapter).getSelectedImages()
            val intent = Intent(this, PlayActivity::class.java).apply {
                putStringArrayListExtra("selectedImages", ArrayList(selectedImages))
            }
            startActivity(intent)
        }
    }

    private fun setupRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl("https://stocksnap.io")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        webService = retrofit.create(WebService::class.java)
    }

    private fun setupRecyclerView() {
        val adapter = ImagesAdapter { selectedImages ->
            // Enable the "Play" button when 6 images are selected
            binding.playButton.isEnabled = selectedImages.size == 6
            binding.selectionTextView.text = "Image ${selectedImages.size} of 6 selected"
        }
        binding.imagesRecyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.imagesRecyclerView.adapter = adapter
    }

    private fun fetchImages(url: String) {
        currentJob?.cancel() // Cancel the current job if a new URL is entered
        currentJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                updateUIForLoading()

                // Fetch webpage content
                val response = webService.fetchPage(url).execute()
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    throw Exception("Failed to fetch webpage: ${response.message()}")
                }

                // Parse the webpage to extract image URLs
                val document = Jsoup.parse(response.body())
                val imageElements = document.select("img[src]")
                val imageUrls = imageElements.map { it.absUrl("src") }
                    .filter { it.isNotBlank() } // Filter out empty URLs
                    .distinct() // Ensure URLs are unique
                    .take(20) // Take only the first 20 valid URLs

                if (imageUrls.isEmpty()) {
                    throw Exception("No images found at the specified URL.")
                }

                // Download and display images
                imageUrls.forEachIndexed { index, imageUrl ->
                    if (!isActive) return@forEachIndexed // Exit if the coroutine is cancelled

                    withContext(Dispatchers.Main) {
                        (binding.imagesRecyclerView.adapter as ImagesAdapter).addImage(imageUrl)
                        updateProgress(index + 1, imageUrls.size)
                    }
                }

                finishLoading()
            } catch (e: Exception) {
                e.printStackTrace()
                showError(e.message ?: "Error occurred while fetching images.")
            }
        }
    }

    private fun updateUIForLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.progress = 0 // Reset progress bar
            binding.progressBar.max = 100 // Set maximum value to 100 for percentage
            binding.progressTextView.visibility = View.VISIBLE
            binding.selectionTextView.visibility = View.GONE
            binding.playButton.visibility = View.GONE

            // Clear existing images and set the new adapter with the selection listener
            binding.imagesRecyclerView.adapter = ImagesAdapter { selectedImages ->
                // Enable or disable the Play button based on the selected images count
                binding.playButton.isEnabled = selectedImages.size == 6
                binding.selectionTextView.text = "Image ${selectedImages.size} of 6 selected"
                binding.selectionTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateProgress(downloaded: Int, total: Int) {
        val percentage = (downloaded * 100) / total // Calculate percentage
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.progress = percentage // Update progress bar
            binding.progressTextView.text = "$downloaded/$total images downloaded ($percentage%)"
        }
    }

    private fun finishLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.progressTextView.visibility = View.GONE
            binding.playButton.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@FetchActivity, message, Toast.LENGTH_SHORT).show()
            finishLoading()
        }
    }
}

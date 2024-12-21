package dev.ywaychitaung.the_memory_game_kotlin.ui.play

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.ywaychitaung.the_memory_game_kotlin.R

class PlayAdapter(
    private val images: List<String>,
    private val onMatchFound: (Boolean) -> Unit
) : RecyclerView.Adapter<PlayAdapter.ImageViewHolder>() {

    private val revealedPositions = mutableSetOf<Int>()
    private val matchedPositions = mutableSetOf<Int>()
    private var firstRevealed: Int? = null
    private val handler = Handler(Looper.getMainLooper())

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (matchedPositions.contains(position) || revealedPositions.contains(position)) {
                    return@setOnClickListener
                }

                revealImage(position)
                if (firstRevealed == null) {
                    firstRevealed = position
                } else {
                    checkMatch(firstRevealed!!, position)
                    firstRevealed = null
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_play, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (matchedPositions.contains(position) || revealedPositions.contains(position)) {
            Picasso.get()
                .load(images[position])
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder)
        }
    }

    override fun getItemCount(): Int = images.size

    private fun revealImage(position: Int) {
        revealedPositions.add(position)
        notifyItemChanged(position)
    }

    private fun hideImages(first: Int, second: Int) {
        handler.postDelayed({
            revealedPositions.remove(first)
            revealedPositions.remove(second)
            notifyItemChanged(first)
            notifyItemChanged(second)
        }, 1000)
    }

    private fun checkMatch(first: Int, second: Int) {
        if (images[first] == images[second]) {
            matchedPositions.add(first)
            matchedPositions.add(second)
            onMatchFound(true)
        } else {
            hideImages(first, second)
        }
    }
}


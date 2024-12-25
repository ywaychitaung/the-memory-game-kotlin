package dev.ywaychitaung.the_memory_game_kotlin.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.ywaychitaung.the_memory_game_kotlin.R
import dev.ywaychitaung.the_memory_game_kotlin.data.model.response.ScoreResponse

class LeaderboardAdapter(private val scores: List<ScoreResponse>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(score: ScoreResponse, position: Int) {
            // Set rank, username, and time
            itemView.findViewById<TextView>(R.id.numberTextView).text = "#${position + 1}"
            itemView.findViewById<TextView>(R.id.usernameTextView).text = score.username
            itemView.findViewById<TextView>(R.id.timeTextView).text = String.format("%02d:%02d:%02d",
                score.totalSeconds / 3600,           // hours
                (score.totalSeconds % 3600) / 60,    // minutes
                score.totalSeconds % 60              // seconds
            )

            // Set background color based on rank
            val context = itemView.context
            val colorRes = when (position) {
                0 -> R.color.gold
                1 -> R.color.silver
                2 -> R.color.bronze
                else -> R.color.white
            }
            itemView.setBackgroundColor(ContextCompat.getColor(context, colorRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(scores[position], position)
    }

    override fun getItemCount(): Int = scores.size
}

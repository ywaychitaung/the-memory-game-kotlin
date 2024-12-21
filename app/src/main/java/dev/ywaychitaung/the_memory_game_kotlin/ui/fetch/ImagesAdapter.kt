import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.ywaychitaung.the_memory_game_kotlin.R

class ImagesAdapter(
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    private val imageUrls = mutableListOf<String>()
    private val selectedImages = mutableSetOf<String>()

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val overlayView: View = view.findViewById(R.id.overlayView)
        val checkIcon: ImageView = view.findViewById(R.id.checkIcon)

        init {
            view.setOnClickListener {
                val imageUrl = imageUrls[adapterPosition]

                if (selectedImages.contains(imageUrl)) {
                    // Deselect the image
                    selectedImages.remove(imageUrl)
                    overlayView.visibility = View.GONE
                    checkIcon.visibility = View.GONE
                } else if (selectedImages.size < 6) {
                    // Select the image
                    selectedImages.add(imageUrl)
                    overlayView.visibility = View.VISIBLE
                    checkIcon.visibility = View.VISIBLE
                } else {
                    // Show a Toast if trying to select more than 6 images
                    Toast.makeText(view.context, "You can select up to 6 images only!", Toast.LENGTH_SHORT).show()
                }

                // Notify selection change
                onSelectionChanged(selectedImages.toList())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error_image)
            .into(holder.imageView)

        if (selectedImages.contains(imageUrl)) {
            holder.overlayView.visibility = View.VISIBLE
            holder.checkIcon.visibility = View.VISIBLE
        } else {
            holder.overlayView.visibility = View.GONE
            holder.checkIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    fun addImage(url: String) {
        if (url.isNotBlank()) {
            imageUrls.add(url)
            notifyItemInserted(imageUrls.size - 1)
        }
    }

    fun getSelectedImages(): List<String> = selectedImages.toList()
}

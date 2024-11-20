package com.example.a1_2_watch.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.databinding.DiscoverItemLayoutBinding
import com.example.a1_2_watch.models.MinimizedItem
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.utils.Constants

/**
 * DiscoverAdapter class for displaying a list of media items (search results or recommendations).
 *
 * @property items List of items to display in the RecyclerView.
 * @property onItemClick Lambda function to handle item clicks.
 */
class DiscoverAdapter(
    private val items: MutableList<Any> = mutableListOf(),
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val binding =
            DiscoverItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscoverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        try {
            holder.bind(items[position])
        } catch (e: Exception) {
            Log.e(TAG, "Error binding item at position $position", e)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Updates the list of items and refreshes the RecyclerView.
     *
     * @param newItems The new list of items to display.
     */
    fun updateItems(newItems: List<Any>) {
        items.clear()
        if (newItems.isNotEmpty()) {
            items.addAll(newItems)
        }
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for individual items in the RecyclerView.
     *
     * @param binding Binding object for DiscoverItemLayout.
     */
    inner class DiscoverViewHolder(private val binding: DiscoverItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds data to the UI based on the type of media item.
         *
         * @param item The media item to bind.
         */
        fun bind(item: Any) {
            try {
                val title: String?
                val posterPath: String?

                when (item) {
                    is MinimizedItem -> {
                        // For search results, use MinimizedItem fields.
                        title = item.title
                        posterPath = if (item.type == "MOVIES" || item.type == "TV_SHOWS") {
                            Constants.IMAGE_URL + (item.posterPath ?: "")
                        } else {
                            item.posterPath
                        }
                    }
                    is Movie -> {
                        title = item.title ?: "No Title Available"
                        posterPath = Constants.IMAGE_URL + (item.poster_path ?: "")
                    }
                    is Show -> {
                        title = item.name ?: "No Title Available"
                        posterPath = Constants.IMAGE_URL + (item.poster_path ?: "")
                    }
                    is Anime -> {
                        title = item.attributes?.canonicalTitle ?: "No Title Available"
                        posterPath = item.attributes?.posterImage?.original ?: ""
                    }
                    else -> {
                        title = "No Title Available"
                        posterPath = null
                    }
                }

                // Bind the title to the TextView.
                binding.titleTextView.text = title

                // Bind the poster image using Glide.
                if (!posterPath.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(posterPath)
                        .placeholder(R.drawable.placeholder_loading)
                        .into(binding.posterImageView)
                } else {
                    Glide.with(binding.root.context)
                        .load(R.drawable.placeholder_no_image)
                        .into(binding.posterImageView)
                }

                // Set click listener to handle item clicks.
                itemView.setOnClickListener { onItemClick(item) }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding item: ${item::class.java.simpleName}", e)
            }
        }
    }

    companion object {
        private const val TAG = "DiscoverAdapter"
    }
}

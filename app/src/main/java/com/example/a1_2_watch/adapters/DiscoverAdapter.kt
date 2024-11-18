package com.example.a1_2_watch.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.databinding.DiscoverItemLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.PLACEHOLDER_URL

/**
 * DiscoverAdapter class for displaying a list of media items as a result of a search query in the RecyclerView.
 *
 * @property items list of items to be displayed in the RecyclerView, it can contain different types of media.
 * @property onItemClick Lambda function that handles the click of media items.
 */
class DiscoverAdapter(
    // List of the items to be displayed.
    private val items: MutableList<Any> = mutableListOf(),
    // Lambda function that handles the click of media items.
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder>() {

    /**
     * This Function to create a new ViewHolder in the RecycleView for displaying an item.
     *
     * @param parent The parent ViewGroup that holds the views.
     * @param viewType The type of the new view.
     * @return DiscoverViewHolder A new instance of DiscoverViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        // Inflate the discover item layout and create a binding instance.
        val binding =
            DiscoverItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscoverViewHolder(binding)
    }

    /**
     * This Function to display the data at the specified position in the RecycleView.
     *
     * @param holder DiscoverViewHolder that holds the UI elements for each item.
     * @param position Position of the item to be displayed in the RecycleView.
     */
    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        // Bind the data at the given position.
        holder.bind(items[position])
    }

    /**
     * This function to retrieve the total number of items in the list.
     *
     * @return Int The total size of the items list.
     */
    override fun getItemCount(): Int = items.size

    /**
     * This function to update the list of items in the adapter and notify the RecyclerView that the
     * list has changed.
     *
     * @param newItems The new list of items to display.
     */
    fun updateItems(newItems: List<Any>) {
        // Clear the current list of items
        items.clear()
        if (newItems.isNotEmpty()) {
            // Add all new items.
            items.addAll(newItems)
        }
        // Notify the RecyclerView that the data list has changed.
        notifyDataSetChanged()
    }

    /**
     * This Inner class that represents individual item view in the RecyclerView.
     *
     * @param binding The binding object for DiscoverItemLayout, which holds references to UI elements.
     */
    inner class DiscoverViewHolder(private val binding: DiscoverItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * This function binds data to the UI elements in the item view based on the media item type.
         *
         * @param item The media item to bind the data for (Movie, Tv Shows, or Anime).
         */
        fun bind(item: Any) {
            // The title of the media item.
            val title: String?
            // The poster path of the media item.
            val posterPath: String?
            // Check the type of the media item
            when (item) {
                is Movie -> {
                    title = item.title ?: "No Title Available"
                    posterPath = Constants.IMAGE_URL + (item.poster_path ?: "")
                }

                is Show -> {
                    title = item.name ?: "No Title Available"
                    posterPath = Constants.IMAGE_URL + (item.poster_path ?: "")
                }

                is Anime -> {
                    title = item.attributes.canonicalTitle
                    // Direct URL for Anime
                    posterPath = item.attributes.posterImage?.medium
                }
                // Default title for unknown media types.
                else -> {
                    title = "No Title Available"
                    posterPath = null
                }
            }

            binding.titleTextView.text = title

            if (!posterPath.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(posterPath)
                    .into(binding.posterImageView)
            } else {
                // Set a placeholder or hide the ImageView
                Glide.with(binding.root.context)
                    .load(PLACEHOLDER_URL)
                    .into(binding.posterImageView)
            }

            // Load the poster image using Glide library.
            if (!posterPath.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(posterPath)
                    .into(binding.posterImageView)
            } else {
                // Set a placeholder image or hide the ImageView
                Glide.with(binding.root.context)
                    .load(PLACEHOLDER_URL)
                    .into(binding.posterImageView)
            }

            // Set the click listener on the item view to handle item click events.
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
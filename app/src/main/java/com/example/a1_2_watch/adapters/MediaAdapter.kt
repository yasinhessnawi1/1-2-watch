package com.example.a1_2_watch.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.databinding.MediaItemLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.utils.Constants
import java.util.*

/**
 * This class represents RecyclerView adapter for displaying a list of media items like movies, shows, and anime.
 *
 * @param context The context of the activity using this adapter
 * @param onItemClick Lambda function to be called when the item is clicked
 * @param onSaveClick Lambda function to be called when the item is saved (Add to favorites)
 * @param onExpandClick Lambda function to be called when the expand button is clicked
 */
class MediaAdapter<T>(
    // Holds the context of the adapter
    private val context: Context,
    // Lambda for the item click event.
    private val onItemClick: (T) -> Unit,
    // Lambda for the save (Add to favorites) button click event.
    private val onSaveClick: (T) -> Unit,
    // Lambda for the expand button click event.
    private val onExpandClick: (T) -> Unit
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {
    // List for holding the media items.
    private var mediaList: MutableList<T> = mutableListOf()

    // List for holding the expanded items.
    private val expandedItems = mutableSetOf<Int>()

    /**
     * This function to create a new ViewHolder to display items.
     *
     * @param parent The parent ViewGroup for the views.
     * @param viewType The type of the view (movie, show, and anime)
     * @return MediaViewHolder To hold the view UI elements.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        // Inflate the layout for each media item by using the MediaItemLayoutBinding.
        val binding =
            MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    /**
     * This function to bind data to the view holder for each media item.
     *
     * @param holder MediaViewHolder to bind the data.
     * @param position Position of the item in the list of the media.
     */
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        // Get the position of the current media item.
        val mediaItem = mediaList[position]
        // Bind the item data to the view holder.
        holder.bind(mediaItem)

        // Set the click listener for the entire item view.
        holder.itemView.setOnClickListener {
            // Trigger onItemClick lambda when item is clicked
            onItemClick(mediaItem)
        }

        // Set the click listener for the save button.
        holder.binding.saveButton.setOnClickListener {
            // Trigger onSaveClick lambda when save button is clicked.
            onSaveClick(mediaItem)
        }

        // Set the click listener for the expand button.
        holder.binding.expandButton.setOnClickListener {
            // Trigger onExpandClick lambda when expand button is clicked.
            onExpandClick(mediaItem)
        }
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return Int The total count of items in media list.
     */
    override fun getItemCount(): Int = mediaList.size

    /**
     * Sets the media list when new data is loaded and refreshes the view.
     *
     * @param mediaItems List of the media items to be displayed.
     */
    fun setMediaList(mediaItems: List<T>) {
        // Clear the existing items.
        this.mediaList.clear()
        // Add new items to the list.
        this.mediaList.addAll(mediaItems)
        // Refresh the view with the new items.
        notifyDataSetChanged()
    }

    /**
     * This function adds new items to the list and updates the view.
     *
     * @param mediaItems List of the media items to be added
     */
    fun addMediaItems(mediaItems: List<T>) {
        // Get the current size of the list as starting position.
        val startPosition = mediaList.size
        // Add the new items to the list.
        mediaList.addAll(mediaItems)
        // Refresh the view with the new items.
        notifyItemRangeInserted(startPosition, mediaItems.size)
    }

    /**
     * Updates the like status of the specific item and refreshes its view.
     *
     * @param item Item whose like status has changed.
     */
    fun updateLikeStatus(item: T) {
        // Get the position of the item in the list.
        val position = mediaList.indexOf(item)
        // if item in the list.
        if (position != -1) {
            // updates its view.
            notifyItemChanged(position)
        }
    }

    /**
     * MediaViewHolder class representing individual media items in the RecyclerView.
     *
     * @param binding Data binding object for the media item layout.
     */
    inner class MediaViewHolder(val binding: MediaItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * This function to bind data to the view elements for a given media item.
         *
         * @param mediaItem The media item to bind data for.
         */
        fun bind(mediaItem: T) {
            // Variables to hold media details.
            val mediaTitle: String?
            val mediaRating: String?
            val posterPath: String?
            val isLiked: Boolean

            // Get values based on the media item type.
            when (mediaItem) {
                is Movie -> {
                    mediaTitle = mediaItem.title ?: "No Title Available"
                    mediaRating = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                    posterPath = Constants.IMAGE_URL + mediaItem.poster_path
                    isLiked = mediaItem.isLiked
                }

                is Show -> {
                    mediaTitle = mediaItem.name ?: "No Title Available"
                    mediaRating = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                    posterPath = Constants.IMAGE_URL + mediaItem.poster_path
                    isLiked = mediaItem.isLiked
                }

                is Anime -> {
                    mediaTitle = mediaItem.attributes.canonicalTitle
                    mediaRating = if (mediaItem.attributes.averageRating.isNotEmpty()) {
                        String.format(
                            Locale.getDefault(),
                            "%.1f",
                            mediaItem.attributes.averageRating.toFloat() / 10
                        )
                    } else {
                        "N/A"
                    }
                    posterPath = mediaItem.attributes.posterImage?.medium
                    isLiked = mediaItem.isLiked
                }

                else -> {
                    mediaTitle = "No Title Available"
                    mediaRating = "N/A"
                    posterPath = null
                    isLiked = false
                }
            }

            // Set the title and rating on the UI elements.
            binding.mediaTitleTextView.text = mediaTitle
            binding.mediaRatingTextView.text = mediaRating

            // Get the poster image by using Glide library.
            Glide.with(binding.root.context)
                .load(posterPath ?: "")
                .into(binding.mediaImageView)

            // Display the loading animation using Glide.
            Glide.with(binding.root.context)
                .asGif()
                .load(R.drawable.sand_clock)
                .into(binding.sandClockView)

            // Set the save button icon based on the like status.
            binding.saveButton.setImageResource(
                if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
            )

            val isExpanded = expandedItems.contains(adapterPosition)
            binding.expandableDetailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            // Change the expand button icon based on the expansion state
            binding.expandButton.setImageResource(
                if (isExpanded) R.drawable.ic_expand_arrow else R.drawable.ic_expand_arrow
            )
        }
    }

    /**
     * Updates the liked status for each media item based on a list of liked titles.
     *
     * @param likedTitles List of titles that are marked as liked.
     */
    fun refreshLikedStatus(likedTitles: List<String>) {
        mediaList.forEachIndexed { index, item ->
            when (item) {
                is Movie -> item.isLiked = likedTitles.contains(item.title)
                is Show -> item.isLiked = likedTitles.contains(item.name)
                is Anime -> item.isLiked = likedTitles.contains(item.attributes.canonicalTitle)
            }
            // Notify the adapter to update the changed item view.
            notifyItemChanged(index)
        }
    }


    /**
     * This function toggles the expansion state of a media item.
     *
     * @param item The media item whose expansion state is to be toggled.
     */
    fun toggleItemExpansion(item: T) {
        val position = mediaList.indexOf(item)
        if (position != -1) {
            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }
}
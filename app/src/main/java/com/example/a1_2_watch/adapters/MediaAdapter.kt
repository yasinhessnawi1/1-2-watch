package com.example.a1_2_watch.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.databinding.MediaItemLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.AnimeDetails
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.MovieDetails
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.ShowDetails
import com.example.a1_2_watch.repository.DetailsRepository
import com.example.a1_2_watch.utils.Constants
import java.util.*

/**
 * This class represents RecyclerView adapter for displaying a list of media items like movies, shows and anime.
 *
 * @param context The context of the activity using this adapter
 * @param onItemClick Lambda function to be called when the item is clicked
 * @param onSaveClick Lambda function to be called when the item is saved (Add to favorites)
 * @param fetchDetailsFromAPI Boolean flag to specify if the item details should be fetched from API or not.
 */
class MediaAdapter<T>(
    // Holds the context of the adapter
    private val context: Context,
    // Lambda for the item click event.
    private val onItemClick: (T) -> Unit,
    // Lambda for the save (Add to favorites) button click event.
    private val onSaveClick: (T) -> Unit,
    // Flag to determine if the item details are fetched or not.
    private val fetchDetailsFromAPI: Boolean = false
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {
    // List for holding the media items.
    private var mediaList: MutableList<T> = mutableListOf()

    /**
     * This function to create a new ViewHolder to display items.
     *
     * @param parent The parent ViewGroup for the views.
     * @param viewType The type of the view (movie , show, and anime)
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
     * @param item Item whose likes status has changed.
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
        // Flag to check if the details view is expanded.
        private var isExpanded = false

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
                    mediaRating = if (!mediaItem.attributes.averageRating.isNullOrEmpty()) {
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

            // Toggle the expand view if the expand button is clicked.
            binding.expandButton.setOnClickListener {
                if (isExpanded) {
                    binding.expandableDetailsLayout.visibility = View.GONE
                    isExpanded = false
                } else {
                    if (fetchDetailsFromAPI) {
                        binding.nextReleaseLayout.visibility =
                            if (mediaItem is Movie) View.GONE else View.VISIBLE
                        // Fetch and display details from the API.
                        fetchDetails(mediaItem)
                    } else {
                        binding.nextReleaseLayout.visibility = View.GONE
                        // Display locally available details.
                        displayExistingDetails(mediaItem)
                    }
                    // Expand the view.
                    binding.expandableDetailsLayout.visibility = View.VISIBLE
                    // Set the expanded flag to true.
                    isExpanded = true
                }
            }
        }

        /**
         * Fetches detailed information for the media item from the API.
         *
         * @param mediaItem The media item for which details are fetched.
         */
        private fun fetchDetails(mediaItem: T) {
            // Create repository for fetching details.
            val detailsRepository = DetailsRepository()
            // Check the media item type.
            when (mediaItem) {
                is Movie -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.MOVIES) { data ->
                        // Ensure the that the updated UI on main thread.
                        (context as Activity).runOnUiThread {
                            val movieDetails = data as? MovieDetails
                            if (movieDetails != null) {
                                binding.releaseDateTextView.text =
                                    context.getString(
                                        R.string.release_date,
                                        movieDetails.release_date
                                    )
                                binding.overviewTextView.text =
                                    context.getString(R.string.description, movieDetails.overview)
                            } else {
                                binding.releaseDateTextView.text =
                                    context.getString(R.string.release_date_n_a)
                                binding.overviewTextView.text =
                                    context.getString(R.string.no_description)
                            }
                        }
                    }
                }

                is Show -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.TV_SHOWS) { data ->
                        // Ensure the that the updated UI on main thread.
                        (context as Activity).runOnUiThread {
                            val showDetails = data as? ShowDetails
                            if (showDetails != null) {
                                binding.nextReleaseLayout.visibility =
                                    if (showDetails.next_episode_to_air == null) View.GONE else View.VISIBLE
                                binding.releaseDateTextView.text =
                                    context.getString(
                                        R.string.first_air_date,
                                        showDetails.first_air_date
                                    )
                                binding.overviewTextView.text = context.getString(
                                    R.string.description,
                                    showDetails.overview
                                )

                                if (showDetails.status == "Ended") {
                                    binding.endDateTextView.text =
                                        context.getString(
                                            R.string.last_air_date,
                                            showDetails.last_air_date
                                        )
                                } else {
                                    binding.nextReleaseTextView.text =
                                        context.getString(
                                            R.string.next_episode_to_air,
                                            showDetails.next_episode_to_air?.name
                                        )
                                    binding.nextReleaseDateTextView.text = context.getString(
                                        R.string.next_episode,
                                        showDetails.next_episode_to_air?.air_date
                                    )
                                }

                            } else {
                                binding.releaseDateTextView.text =
                                    context.getString(R.string.first_air_date_n_a)
                                binding.overviewTextView.text = context.getString(R.string.no_description)
                            }
                        }
                    }
                }

                is Anime -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.ANIME) { data ->
                        // Ensure the that the updated UI on main thread.
                        (context as Activity).runOnUiThread {
                            val animeDetails = data as? AnimeDetails
                            val attributes = animeDetails?.data?.attributes
                            if (attributes != null) {
                                binding.nextReleaseLayout.visibility =
                                    if (attributes.nextRelease.isNullOrEmpty()) View.GONE else View.VISIBLE
                                binding.releaseDateTextView.text = context.getString(
                                    R.string.first_air_date,
                                    attributes.startDate ?: context.getString(R.string.start_date_n_a)
                                )
                                binding.overviewTextView.text = context.getString(
                                    R.string.description,
                                    attributes.synopsis ?: context.getString(R.string.no_description)
                                )
                                if (attributes.endDate != null) {
                                    binding.endDateTextView.text = context.getString(
                                        R.string.last_air_date,
                                        attributes.endDate
                                    )
                                } else {
                                    binding.nextReleaseDateTextView.text =
                                        context.getString(
                                            R.string.next_episode,
                                            attributes.nextRelease
                                        )
                                }
                            } else {
                                binding.releaseDateTextView.text =
                                    context.getString(R.string.start_date_n_a)
                                binding.overviewTextView.text = context.getString(R.string.no_description)
                            }
                        }
                    }
                }
            }
        }

        /**
         * Displays locally available details for the media item.
         *
         * @param mediaItem The media item for which existing details are displayed.
         */
        private fun displayExistingDetails(mediaItem: T) {
            when (mediaItem) {
                is Movie -> {
                    binding.releaseDateTextView.text = context.getString(
                        R.string.release_date,
                        mediaItem.release_date ?: context.getString(R.string.release_date_n_a)
                    )
                    binding.overviewTextView.text = context.getString(
                        R.string.description,
                        mediaItem.overview
                    )
                }

                is Show -> {
                    binding.releaseDateTextView.text = context.getString(
                        R.string.first_air_date,
                        mediaItem.first_air_date ?: context.getString(R.string.first_air_date_n_a)
                    )
                    binding.overviewTextView.text = context.getString(
                        R.string.description,
                        mediaItem.overview
                    )
                }

                is Anime -> {
                    val attributes = mediaItem.attributes
                    binding.releaseDateTextView.text = context.getString(
                        R.string.first_air_date,
                        attributes.startDate ?: context.getString(R.string.start_date_n_a)
                    )
                    binding.overviewTextView.text = context.getString(
                        R.string.description,
                        attributes.synopsis ?: context.getString(R.string.no_description)
                    )
                }
            }
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
}

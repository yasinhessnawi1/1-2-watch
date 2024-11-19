package com.example.a1_2_watch.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.databinding.MediaItemLayoutBinding
import com.example.a1_2_watch.workers.FetchDetailsWorker
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import com.google.gson.Gson
import java.util.*

/**
 * RecyclerView Adapter for displaying media items like Movies, Shows, and Anime.
 *
 * @param context The context of the activity using this adapter.
 * @param onItemClick Lambda function to handle media item clicks.
 * @param onSaveClick Lambda function to handle save button clicks.
 * @param fetchDetailsFromAPI Boolean flag to determine if details should be fetched from API.
 * @param lifecycleOwner Optional LifecycleOwner to observe LiveData (required for WorkManager).
 */
class MediaAdapter<T>(
    private val context: Context,
    private val onItemClick: (T) -> Unit,
    private val onSaveClick: (T) -> Unit,
    private val fetchDetailsFromAPI: Boolean = false,
    private val lifecycleOwner: LifecycleOwner? = null
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {

    // List to hold all media items to be displayed.
    private var mediaList: MutableList<T> = mutableListOf()

    /**
     * Creates a ViewHolder for a media item.
     *
     * @param parent The parent ViewGroup for the ViewHolder.
     * @param viewType The view type for the item (not used here).
     * @return A new MediaViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     *
     * @param holder The MediaViewHolder instance.
     * @param position The position of the media item in the list.
     */
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        try {
            val mediaItem = mediaList[position]
            holder.bind(mediaItem)

            // Handle item click events.
            holder.itemView.setOnClickListener { onItemClick(mediaItem) }

            // Handle save button click events.
            holder.binding.saveButton.setOnClickListener { onSaveClick(mediaItem) }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding item at position $position", e)
        }
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of the media list.
     */
    override fun getItemCount(): Int = mediaList.size

    /**
     * Replaces the current media list with a new list and refreshes the adapter.
     *
     * @param mediaItems The new list of media items.
     */
    fun setMediaList(mediaItems: List<T>) {
        try {
            mediaList.clear()
            mediaList.addAll(mediaItems)
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting media list", e)
        }
    }

    /**
     * Adds more media items to the list and notifies the adapter.
     *
     * @param mediaItems The new media items to add.
     */
    fun addMediaItems(mediaItems: List<T>) {
        try {
            val startPosition = mediaList.size
            mediaList.addAll(mediaItems)
            notifyItemRangeInserted(startPosition, mediaItems.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding media items", e)
        }
    }

    /**
     * Updates the like status of a specific media item in the list.
     *
     * @param item The media item whose like status needs updating.
     */
    fun updateLikeStatus(item: T) {
        try {
            val index = mediaList.indexOfFirst { mediaItem ->
                when (mediaItem) {
                    is Movie -> mediaItem.id == (item as Movie).id
                    is Show -> mediaItem.id == (item as Show).id
                    is Anime -> mediaItem.id == (item as Anime).id
                    else -> false
                }
            }
            if (index != -1) {
                mediaList[index] = item
                notifyItemChanged(index)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating like status", e)
        }
    }

    /**
     * Updates the liked status of all items based on a list of liked item IDs.
     *
     * @param likedItemIds A list of IDs for liked items.
     */
    fun refreshLikedStatus(likedItemIds: List<Int>) {
        try {
            mediaList.forEachIndexed { index, item ->
                when (item) {
                    is Movie -> item.isLiked = likedItemIds.contains(item.id)
                    is Show -> item.isLiked = likedItemIds.contains(item.id)
                    is Anime -> item.isLiked = likedItemIds.contains(item.id)
                }
                notifyItemChanged(index)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing liked status", e)
        }
    }

    /**
     * Removes a specific media item from the list and refreshes the adapter.
     *
     * @param item The media item to remove.
     */
    fun removeMediaItem(item: T) {
        try {
            val index = mediaList.indexOfFirst { mediaItem ->
                when (mediaItem) {
                    is Movie -> mediaItem.id == (item as Movie).id
                    is Show -> mediaItem.id == (item as Show).id
                    is Anime -> mediaItem.id == (item as Anime).id
                    else -> false
                }
            }
            if (index != -1) {
                mediaList.removeAt(index)
                notifyItemRemoved(index)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing media item", e)
        }
    }

    /**
     * ViewHolder class to manage media item views.
     *
     * @param binding The binding object for the media item layout.
     */
    inner class MediaViewHolder(val binding: MediaItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false

        /**
         * Binds media item details to the UI elements.
         *
         * @param mediaItem The media item to bind.
         */
        fun bind(mediaItem: T) {
            try {
                val mediaTitle: String?
                val mediaRating: String?
                val posterPath: String?
                val isLiked: Boolean

                when (mediaItem) {
                    is Movie -> {
                        mediaTitle = mediaItem.title ?: "No Title Available"
                        mediaRating = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                        posterPath = Constants.IMAGE_URL + (mediaItem.poster_path ?: "")
                        isLiked = mediaItem.isLiked
                    }
                    is Show -> {
                        mediaTitle = mediaItem.name ?: "No Title Available"
                        mediaRating = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                        posterPath = Constants.IMAGE_URL + (mediaItem.poster_path ?: "")
                        isLiked = mediaItem.isLiked
                    }
                    is Anime -> {
                        mediaTitle = mediaItem.attributes?.canonicalTitle
                        mediaRating = if (mediaItem.attributes?.averageRating != null) {
                            String.format(
                                Locale.getDefault(),
                                "%.1f",
                                mediaItem.attributes.averageRating.toFloat() / 10
                            )
                        } else "N/A"
                        posterPath = mediaItem.attributes?.posterImage?.original ?: ""
                        isLiked = mediaItem.isLiked
                    }
                    else -> {
                        mediaTitle = "No Title Available"
                        mediaRating = "N/A"
                        posterPath = null
                        isLiked = false
                    }
                }

                updateMediaItemView(mediaTitle, mediaRating, posterPath, isLiked)
                onExpandButtonClick(mediaItem)
            } catch (e: Exception) {
                Log.e(TAG, "Error binding media item", e)
            }
        }

        /**
         * Updates the media item view with the provided data.
         *
         * @param mediaTitle The title of the media item.
         * @param mediaRating The rating of the media item.
         * @param posterPath The path to the poster image.
         * @param isLiked The like status of the media item.
         */
        private fun updateMediaItemView(
            mediaTitle: String?, mediaRating: String, posterPath: String?, isLiked: Boolean
        ) {
            try {
                // Set the title and rating on the UI elements.
                binding.mediaTitleTextView.text = mediaTitle
                binding.mediaRatingTextView.text = mediaRating
                // Load the poster image using Glide.
                Glide.with(context).load(posterPath ?: "")
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.placeholder_no_image)
                    .into(binding.mediaImageView)
                // Display the loading animation using Glide.
                Glide.with(binding.root.context).asGif().placeholder(R.drawable.placeholder_loading)
                    .load(R.drawable.sand_clock).into(binding.sandClockView)

                // Set the save button icon based on the like status.
                binding.saveButton.setImageResource(
                    if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                )

                isExpanded.let {
                    if (isExpanded) {
                        binding.expandableDetailsLayout.visibility = View.VISIBLE
                        binding.expandButton.setImageResource(R.drawable.arrow_up)
                    } else {
                        binding.expandableDetailsLayout.visibility = View.GONE
                        binding.expandButton.setImageResource(R.drawable.arrow_down)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating media item view", e)
            }
        }

        /**
         * Handles the click event for the expand button to show or hide details.
         *
         * @param mediaItem The media item for which details are to be displayed.
         */
        fun onExpandButtonClick(mediaItem: T) {
            try {
                binding.expandButton.setOnClickListener {
                    if (isExpanded) {
                        // Collapse the details view.
                        binding.expandableDetailsLayout.visibility = View.GONE
                        binding.expandButton.setImageResource(R.drawable.arrow_down)
                        isExpanded = false
                    } else {
                        if (fetchDetailsFromAPI) {
                            // Show or hide specific layouts based on media type.
                            binding.nextReleaseLayout.visibility =
                                if (mediaItem is Movie) View.GONE else View.VISIBLE
                            // Fetch and display details using WorkManager.
                            fetchDetailsWithWorker(mediaItem)
                        } else {
                            // Display existing details.
                            binding.nextReleaseLayout.visibility = View.GONE
                            displayExistingDetails(mediaItem)
                        }
                        // Expand the details view.
                        binding.expandableDetailsLayout.visibility = View.VISIBLE
                        binding.expandButton.setImageResource(R.drawable.arrow_up)
                        isExpanded = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling expand button click", e)
            }
        }

        /**
         * Uses WorkManager to fetch detailed information for the media item.
         *
         * @param mediaItem The media item for which details are to be fetched.
         */
        private fun fetchDetailsWithWorker(mediaItem: T) {
            try {
                // Create input data for the worker.
                val inputData = when (mediaItem) {
                    is Movie -> workDataOf(
                        FetchDetailsWorker.KEY_MEDIA_TYPE to MediaType.MOVIES.name,
                        FetchDetailsWorker.KEY_MEDIA_ID to mediaItem.id
                    )
                    is Show -> workDataOf(
                        FetchDetailsWorker.KEY_MEDIA_TYPE to MediaType.TV_SHOWS.name,
                        FetchDetailsWorker.KEY_MEDIA_ID to mediaItem.id
                    )
                    is Anime -> workDataOf(
                        FetchDetailsWorker.KEY_MEDIA_TYPE to MediaType.ANIME.name,
                        FetchDetailsWorker.KEY_MEDIA_ID to mediaItem.id
                    )
                    else -> workDataOf()
                }

                // Define constraints if needed (e.g., network connectivity).
                val constraints =
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

                // Create the WorkRequest.
                val fetchDetailsWork =
                    OneTimeWorkRequestBuilder<FetchDetailsWorker>().setInputData(inputData)
                        .setConstraints(constraints).build()

                // Enqueue the WorkRequest.
                WorkManager.getInstance(context).enqueue(fetchDetailsWork)

                // Observe the WorkRequest's status.
                WorkManager.getInstance(context).getWorkInfoByIdLiveData(fetchDetailsWork.id)
                    .observe(lifecycleOwner ?: return) { workInfo ->
                        try {
                            if (workInfo != null && workInfo.state.isFinished) {
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    val resultJson =
                                        workInfo.outputData.getString(FetchDetailsWorker.KEY_RESULT)
                                    handleWorkerResult(resultJson, mediaItem)
                                } else {
                                    // Handle failure or retry as needed.
                                    setDetailsError()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error observing work status", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching details with worker", e)
            }
        }

        /**
         * Handles the result returned by the WorkManager.
         *
         * @param resultJson The JSON string containing detailed information.
         * @param mediaItem The media item for which details were fetched.
         */
        private fun handleWorkerResult(resultJson: String?, mediaItem: T) {
            try {
                if (resultJson == null) {
                    setDetailsError()
                    return
                }
                val gson = Gson()
                when (mediaItem) {
                    is Movie -> {
                        val movieDetails = gson.fromJson(resultJson, MovieDetails::class.java)
                        if (movieDetails != null) {
                            binding.releaseDateTextView.text = context.getString(
                                R.string.release_date, movieDetails.release_date
                            )
                            binding.overviewTextView.text =
                                context.getString(R.string.description, movieDetails.overview)
                            binding.endDateTextView.visibility = View.GONE
                            binding.nextReleaseLayout.visibility = View.GONE
                        } else {
                            setDetailsError()
                        }
                    }
                    is Show -> {
                        val showDetails: ShowDetails? =
                            gson.fromJson(resultJson, ShowDetails::class.java)
                        if (showDetails != null) {
                            binding.releaseDateTextView.text = context.getString(
                                R.string.first_air_date, showDetails.first_air_date
                            )
                            binding.overviewTextView.text = context.getString(
                                R.string.description, showDetails.overview
                            )

                            if (showDetails.status == "Ended" || showDetails.next_episode_to_air == null) {
                                binding.endDateTextView.visibility = View.GONE
                                binding.nextReleaseLayout.visibility = View.GONE
                            } else {
                                binding.nextReleaseTextView.text = context.getString(
                                    R.string.next_episode_to_air,
                                    showDetails.next_episode_to_air.name
                                )
                                binding.nextReleaseDateTextView.text = context.getString(
                                    R.string.next_episode,
                                    showDetails.next_episode_to_air.air_date
                                )
                            }
                        } else {
                            setDetailsError()
                        }
                    }
                    is Anime -> {
                        val animeDetails: AnimeDetails? =
                            gson.fromJson(resultJson, AnimeDetails::class.java)
                        val attributes = animeDetails?.data?.attributes
                        if (attributes != null) {
                            binding.releaseDateTextView.text = context.getString(
                                R.string.first_air_date,
                                attributes.startDate ?: context.getString(R.string.start_date_n_a)
                            )
                            binding.overviewTextView.text = context.getString(
                                R.string.description,
                                attributes.synopsis ?: context.getString(R.string.no_description)
                            )
                            if (attributes.endDate != null) {
                                binding.endDateTextView.visibility = View.GONE
                                binding.nextReleaseLayout.visibility = View.GONE
                            } else {
                                binding.nextReleaseDateTextView.text = context.getString(
                                    R.string.next_episode, attributes.nextRelease ?: "N/A"
                                )
                            }
                        } else {
                            setDetailsError()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling worker result", e)
            }
        }

        /**
         * Sets error messages or default texts when details fetching fails.
         */
        private fun setDetailsError() {
            try {
                binding.releaseDateTextView.text = context.getString(R.string.release_date_n_a)
                binding.overviewTextView.text = context.getString(R.string.no_description)
                binding.nextReleaseLayout.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error setting details error view", e)
            }
        }

        /**
         * Displays existing details for the media item without fetching from the API.
         *
         * @param mediaItem The media item for which details are to be displayed.
         */
        private fun displayExistingDetails(mediaItem: T) {
            try {
                when (mediaItem) {
                    is Movie -> {
                        binding.releaseDateTextView.text = context.getString(
                            R.string.release_date,
                            mediaItem.release_date ?: context.getString(R.string.release_date_n_a)
                        )
                        binding.overviewTextView.text = context.getString(
                            R.string.description, mediaItem.overview
                        )
                        binding.endDateTextView.visibility = View.GONE
                    }
                    is Show -> {
                        binding.releaseDateTextView.text = context.getString(
                            R.string.first_air_date,
                            mediaItem.first_air_date ?: context.getString(R.string.first_air_date_n_a)
                        )
                        binding.overviewTextView.text = context.getString(
                            R.string.description, mediaItem.overview
                        )
                        binding.endDateTextView.visibility = View.GONE
                    }
                    is Anime -> {
                        binding.releaseDateTextView.text = context.getString(
                            R.string.first_air_date,
                            mediaItem.attributes?.startDate ?: context.getString(R.string.start_date_n_a)
                        )
                        binding.overviewTextView.text = context.getString(
                            R.string.description,
                            mediaItem.attributes?.synopsis ?: context.getString(R.string.no_description)
                        )
                        binding.endDateTextView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error displaying existing details", e)
            }
        }
    }

    companion object {
        private const val TAG = "MediaAdapter"
    }
}
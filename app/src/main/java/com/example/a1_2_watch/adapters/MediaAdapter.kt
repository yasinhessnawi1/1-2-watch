package com.example.a1_2_watch.adapters

import android.app.Activity
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
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * RecyclerView Adapter for displaying media items like Movies, Shows, and Anime.
 *
 * @param context The context of the activity using this adapter.
 * @param onItemClick Lambda function to be called when the item is clicked.
 * @param onSaveClick Lambda function to be called when the save button is clicked.
 * @param fetchDetailsFromAPI Boolean flag to determine if details should be fetched from API.
 * @param lifecycleOwner LifecycleOwner to manage coroutine scopes within the adapter.
 */
class MediaAdapter<T>(
    private val context: Context,
    private val onItemClick: (T) -> Unit,
    private val onSaveClick: (T) -> Unit,
    private val fetchDetailsFromAPI: Boolean = false,
    private val lifecycleOwner: LifecycleOwner? = null // Optional: For lifecycle-aware operations
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {

    // List holding the media items.
    private var mediaList: MutableList<T> = mutableListOf()

    /**
     * Creates a new ViewHolder for each media item.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of the view.
     * @return A new instance of MediaViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder The ViewHolder instance.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem)

        // Set the click listener for the entire item view.
        holder.itemView.setOnClickListener {
            onItemClick(mediaItem)
        }

        // Set the click listener for the save button.
        holder.binding.saveButton.setOnClickListener {
            onSaveClick(mediaItem)
        }
    }

    /**
     * Returns the total number of items.
     *
     * @return The size of the media list.
     */
    override fun getItemCount(): Int = mediaList.size

    /**
     * Sets the media list and refreshes the adapter.
     *
     * @param mediaItems The new list of media items.
     */
    fun setMediaList(mediaItems: List<T>) {
        mediaList.clear()
        mediaList.addAll(mediaItems)
        notifyDataSetChanged()
    }

    /**
     * Adds new media items to the existing list and notifies the adapter.
     *
     * @param mediaItems The list of media items to add.
     */
    fun addMediaItems(mediaItems: List<T>) {
        val startPosition = mediaList.size
        mediaList.addAll(mediaItems)
        notifyItemRangeInserted(startPosition, mediaItems.size)
    }

    /**
     * Updates the like status of a specific media item.
     *
     * @param item The media item whose like status has changed.
     */
    fun updateLikeStatus(item: T) {
        val index = mediaList.indexOfFirst { mediaItem ->
            when (mediaItem) {
                is Movie -> (mediaItem as Movie).id == (item as Movie).id
                is Show -> (mediaItem as Show).id == (item as Show).id
                is Anime -> (mediaItem as Anime).id == (item as Anime).id
                else -> false
            }
        }
        if (index != -1) {
            mediaList[index] = item
            notifyItemChanged(index)
        }
    }


    /**
     * Refreshes the liked status of all media items based on a list of liked titles.
     *
     * @param likedTitles List of titles that are marked as liked.
     */
    fun refreshLikedStatus(likedItemIds: List<Int>) {
        mediaList.forEachIndexed { index, item ->
            when (item) {
                is Movie -> item.isLiked = likedItemIds.contains(item.id)
                is Show -> item.isLiked = likedItemIds.contains(item.id)
                is Anime -> item.isLiked = likedItemIds.contains(item.id)
            }
            notifyItemChanged(index)
        }
    }

    fun removeMediaItem(item: T) {
        val index = mediaList.indexOfFirst { mediaItem ->
            when (mediaItem) {
                is Movie -> (mediaItem as Movie).id == (item as Movie).id
                is Show -> (mediaItem as Show).id == (item as Show).id
                is Anime -> (mediaItem as Anime).id == (item as Anime).id
                else -> false
            }
        }
        if (index != -1) {
            mediaList.removeAt(index)
            notifyItemRemoved(index)
        }
    }



    /**
     * ViewHolder class for media items.
     *
     * @param binding The binding object for the media item layout.
     */
    inner class MediaViewHolder(val binding: MediaItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false


        /**
         * Binds the media item data to the view elements.
         *
         * @param mediaItem The media item to bind.
         * @param position The position of the item in the list.
         */
        fun bind(mediaItem: T) {
            // Variables to hold media details.
            val mediaTitle: String?
            val mediaRating: String?
            val posterPath: String?
            val isLiked: Boolean


            // Determine the media type and extract relevant details.
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

            // Load the poster image using Glide.
            Glide.with(context)
                .load(posterPath ?: "")
                //.placeholder(R.drawable.placeholder_image) // Optional: Add a placeholder image.
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

            isExpanded.let {
                if (isExpanded) {
                    binding.expandableDetailsLayout.visibility = View.VISIBLE
                    binding.expandButton.setImageResource(R.drawable.arrow_up)
                } else {
                    binding.expandableDetailsLayout.visibility = View.GONE
                    binding.expandButton.setImageResource(R.drawable.arrow_down)
                }
            }

            // Handle expand button click.
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
        }

        /**
         * Uses WorkManager to fetch detailed information for the media item.
         *
         * @param mediaItem The media item for which details are to be fetched.
         * @param position The position of the item in the list.
         */
        private fun fetchDetailsWithWorker(mediaItem: T) {
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
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Create the WorkRequest.
            val fetchDetailsWork = OneTimeWorkRequestBuilder<FetchDetailsWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            // Enqueue the WorkRequest.
            WorkManager.getInstance(context).enqueue(fetchDetailsWork)

            // Observe the WorkRequest's status.
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(fetchDetailsWork.id)
                .observe(lifecycleOwner ?: return) { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        println("Work finished")
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            println("Work succeeded")
                            val resultJson = workInfo.outputData.getString(FetchDetailsWorker.KEY_RESULT)
                            handleWorkerResult(resultJson, mediaItem)
                        } else {
                            // Handle failure or retry as needed.
                            setDetailsError()
                        }
                    }
                }
        }

        /**
         * Handles the result returned by the WorkManager.
         *
         * @param resultJson The JSON string containing detailed information.
         * @param mediaItem The media item for which details were fetched.
         * @param position The position of the item in the list.
         */
        private fun handleWorkerResult(resultJson: String?, mediaItem: T) {
            if (resultJson == null) {
                print("Result is null")
                setDetailsError()
                return
            }
            println(resultJson)
            val gson = Gson()
            when (mediaItem) {
                is Movie -> {
                    val movieDetails = gson.fromJson(resultJson, MovieDetails::class.java)
                    println("Movie details: $movieDetails")
                    if (movieDetails != null) {
                        binding.releaseDateTextView.text =
                            context.getString(
                                R.string.release_date,
                                movieDetails.release_date
                            )
                        binding.overviewTextView.text =
                            context.getString(R.string.description, movieDetails.overview)
                    } else {
                        setDetailsError()
                    }
                }

                is Show -> {
                    val showDetails: ShowDetails? = gson.fromJson(resultJson, ShowDetails::class.java)
                    if (showDetails != null) {
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
                            binding.nextReleaseLayout.visibility = View.GONE
                        } else {
                            binding.nextReleaseTextView.text =
                                context.getString(
                                    R.string.next_episode_to_air,
                                    showDetails.next_episode_to_air?.name ?: "N/A"
                                )
                            binding.nextReleaseDateTextView.text = context.getString(
                                R.string.next_episode,
                                showDetails.next_episode_to_air?.air_date ?: "N/A"
                            )
                        }
                    } else {
                        setDetailsError()
                    }
                }

                is Anime -> {
                    val animeDetails: AnimeDetails? = gson.fromJson(resultJson, AnimeDetails::class.java)
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
                            binding.endDateTextView.text = context.getString(
                                R.string.last_air_date,
                                attributes.endDate
                            )
                            binding.nextReleaseLayout.visibility = View.GONE
                        } else {
                            binding.nextReleaseDateTextView.text = context.getString(
                                R.string.next_episode,
                                attributes.nextRelease ?: "N/A"
                            )
                        }
                    } else {
                        setDetailsError()
                    }
                }
            }
        }

        /**
         * Sets error messages or default texts when details fetching fails.
         */
        private fun setDetailsError() {
            binding.releaseDateTextView.text =
                context.getString(R.string.release_date_n_a)
            binding.overviewTextView.text =
                context.getString(R.string.no_description)
            binding.nextReleaseLayout.visibility = View.GONE
        }

        /**
         * Displays existing details for the media item without fetching from the API.
         *
         * @param mediaItem The media item for which details are to be displayed.
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
                        mediaItem.overview ?: context.getString(R.string.no_description)
                    )
                }

                is Show -> {
                    binding.releaseDateTextView.text = context.getString(
                        R.string.first_air_date,
                        mediaItem.first_air_date ?: context.getString(R.string.first_air_date_n_a)
                    )
                    binding.overviewTextView.text = context.getString(
                        R.string.description,
                        mediaItem.overview ?: context.getString(R.string.no_description)
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
}

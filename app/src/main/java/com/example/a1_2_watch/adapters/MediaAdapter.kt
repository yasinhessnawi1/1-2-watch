package com.example.a1_2_watch.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginEnd
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

class MediaAdapter<T>(
    private val context: Context,
    private val onItemClick: (T) -> Unit,
    private val onSaveClick: (T) -> Unit,
    private val fetchDetailsFromAPI: Boolean = false
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {

    private var mediaList: MutableList<T> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem)

        holder.itemView.setOnClickListener {
            onItemClick(mediaItem)
        }

        holder.binding.saveButton.setOnClickListener {
            onSaveClick(mediaItem)
        }
    }

    override fun getItemCount(): Int = mediaList.size

    fun setMediaList(mediaItems: List<T>) {
        this.mediaList.clear()
        this.mediaList.addAll(mediaItems)
        notifyDataSetChanged()
    }

    fun addMediaItems(mediaItems: List<T>) {
        val startPosition = mediaList.size
        mediaList.addAll(mediaItems)
        notifyItemRangeInserted(startPosition, mediaItems.size)
    }

    fun updateLikeStatus(item: T) {
        val position = mediaList.indexOf(item)
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class MediaViewHolder(val binding: MediaItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false
        fun bind(mediaItem: T) {
            val mediaTitle: String?
            val mediaRating: String?
            val posterPath: String?
            val isLiked: Boolean

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

            binding.mediaTitleTextView.text = mediaTitle
            binding.mediaRatingTextView.text = mediaRating

            Glide.with(binding.root.context)
                .load(posterPath ?: "")
                .into(binding.mediaImageView)

            Glide.with(binding.root.context)
                .asGif()
                .load(R.drawable.sand_clock)
                .into(binding.sandClockView)

            binding.saveButton.setImageResource(
                if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
            )

            binding.expandButton.setOnClickListener {
                if (isExpanded) {
                    binding.expandableDetailsLayout.visibility = View.GONE
                    isExpanded = false
                } else {
                    if (fetchDetailsFromAPI) {
                        binding.nextReleaseLayout.visibility = if (mediaItem is Movie) View.GONE else View.VISIBLE
                        fetchDetails(mediaItem)
                    } else {
                        binding.nextReleaseLayout.visibility =  View.GONE
                        displayExistingDetails(mediaItem)
                    }
                    binding.expandableDetailsLayout.visibility = View.VISIBLE
                    isExpanded = true
                }
            }
        }

        private fun fetchDetails(mediaItem: T) {
            val detailsRepository = DetailsRepository()
            when (mediaItem) {
                is Movie -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.MOVIES) { data ->
                        (context as Activity).runOnUiThread {
                            val movieDetails = data as? MovieDetails
                            if (movieDetails != null) {
                                binding.releaseDateTextView.text = "Release Date: ${movieDetails.release_date}"
                                binding.overviewTextView.text = "Description: \n ${movieDetails.overview}"
                            } else {
                                binding.releaseDateTextView.text = "Release Date: N/A"
                                binding.overviewTextView.text = "No Description"
                            }
                        }
                    }
                }
                is Show -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.TV_SHOWS) { data ->
                        (context as Activity).runOnUiThread {
                            val showDetails = data as? ShowDetails
                            if (showDetails != null) {
                                binding.nextReleaseLayout.visibility = if (showDetails.next_episode_to_air == null) View.GONE else View.VISIBLE
                                binding.releaseDateTextView.text = "First Air Date: ${showDetails.first_air_date}"
                                binding.overviewTextView.text = "Description: \n ${showDetails.overview}"

                                if(showDetails.status == "Ended"){
                                    binding.endDateTextView.text = "Last Air Date: ${showDetails.last_air_date}"
                                }else{
                                    binding.nextReleaseTextView.text = "Next Episode to Air: ${showDetails.next_episode_to_air?.name}"
                                    binding.nextReleaseDateTextView.text = "When? ${showDetails.next_episode_to_air?.air_date}"
                                }

                            } else {
                                binding.releaseDateTextView.text = "First Air Date: N/A"
                                binding.overviewTextView.text = "No Description"
                            }
                        }
                    }
                }
                is Anime -> {
                    detailsRepository.fetchDetails(mediaItem.id, MediaType.ANIME) { data ->
                        (context as Activity).runOnUiThread {
                            val animeDetails = data as? AnimeDetails
                            val attributes = animeDetails?.data?.attributes
                            if (attributes != null) {
                                binding.nextReleaseLayout.visibility = if (attributes.nextRelease.isNullOrEmpty()) View.GONE else View.VISIBLE
                                binding.releaseDateTextView.text = "First Air Date: ${attributes.startDate} "
                                binding.overviewTextView.text = "Description: \n${attributes.synopsis} "
                                if(attributes.endDate != null){
                                    binding.endDateTextView.text = "Last Air Date: ${attributes.endDate}"
                                }else {
                                    binding.
                                    nextReleaseDateTextView.text =
                                        "Next episode: ${attributes.nextRelease}"
                                }
                            } else {
                                binding.releaseDateTextView.text = "Start Date: N/A"
                                binding.overviewTextView.text = "No Description"
                            }
                        }
                    }
                }
            }
        }

        private fun displayExistingDetails(mediaItem: T) {
            when (mediaItem) {
                is Movie -> {
                    binding.releaseDateTextView.text = "Release Date: ${mediaItem.release_date ?: "Unknown"}"
                    binding.overviewTextView.text = "Description: \n${mediaItem.overview}"
                }
                is Show -> {
                    binding.releaseDateTextView.text = "First Air Date: ${mediaItem.first_air_date ?: "Unknown"}"
                    binding.overviewTextView.text = "Description: \n${mediaItem.overview}"
                }
                is Anime -> {
                    val attributes = mediaItem.attributes
                    binding.releaseDateTextView.text = "First Air Date: ${attributes.startDate ?: "Unknown"}"
                    binding.overviewTextView.text = "Description: \n${attributes.synopsis}"
                }
            }
        }

    }


fun refreshLikedStatus(likedTitles: List<String>) {
    mediaList.forEachIndexed { index, item ->
        when (item) {
            is Movie -> item.isLiked = likedTitles.contains(item.title)
            is Show -> item.isLiked = likedTitles.contains(item.name)
            is Anime -> item.isLiked = likedTitles.contains(item.attributes.canonicalTitle)
        }
        notifyItemChanged(index)
    }
}
}

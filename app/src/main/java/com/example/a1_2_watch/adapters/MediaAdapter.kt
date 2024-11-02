package com.example.a1_2_watch.adapters

import android.content.Context
import android.view.LayoutInflater
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

class MediaAdapter<T>(
    private val context: Context,
    private val onItemClick: (T) -> Unit,
    private val onSaveClick: (T) -> Unit
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

            binding.saveButton.setImageResource(
                if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
            )
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

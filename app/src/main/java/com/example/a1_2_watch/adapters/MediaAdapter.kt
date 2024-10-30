package com.example.a1_2_watch.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.databinding.MediaItemLayoutBinding
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.utils.Constants
import java.util.Locale

class MediaAdapter<T>(
    private val onItemClick: (T) -> Unit,
    private val onSaveClick: (T) -> Unit
) : RecyclerView.Adapter<MediaAdapter<T>.MediaViewHolder>() {

    private var mediaList: MutableList<T> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.bind(mediaItem)
        holder.itemView.setOnClickListener {
            onItemClick(mediaItem)
        }
        // Handle Save Button Click
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

    inner class MediaViewHolder(val binding: MediaItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: T) {
            val mediaTitle: String?
            val mediaRating: String?
            val posterPath: String?
            val runtime: String?

            when (mediaItem) {
                is Movie -> {
                    mediaTitle = mediaItem.title ?: "No Title Available"
                    mediaRating  = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                    posterPath = Constants.IMAGE_URL + mediaItem.poster_path
                }
                is Show -> {
                    mediaTitle = mediaItem.name ?: "No Title Available"
                    mediaRating = String.format(Locale.getDefault(), "%.1f", mediaItem.vote_average)
                    posterPath = Constants.IMAGE_URL + mediaItem.poster_path
                }
                is Anime -> {
                    mediaTitle = mediaItem.attributes.canonicalTitle
                    mediaRating = if (mediaItem.attributes.averageRating > 0.toString()) {
                        // Divide the average rating by 10 and format to one decimal place
                        String.format(Locale.getDefault(), "%.1f", mediaItem.attributes.averageRating.toFloat() / 10)
                    } else {
                        "N/A"
                    }
                    posterPath = mediaItem.attributes.posterImage?.medium
                }
                else -> {
                    mediaTitle = "No Title Available"
                    mediaRating = "N/A"
                    posterPath = null
                    runtime = "Runtime: N/A"
                }
            }

            binding.mediaTitleTextView.text = mediaTitle
            binding.mediaRatingTextView.text = mediaRating

            Glide.with(binding.root.context)
                .load(posterPath ?: "")
                .into(binding.mediaImageView)
        }
    }
}


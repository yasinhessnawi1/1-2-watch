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

class DiscoverAdapter(
    private val items: MutableList<Any> = mutableListOf(),
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val binding = DiscoverItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscoverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Any>) {
        items.clear()
        if (newItems.isNotEmpty()) {
            items.addAll(newItems)
        }
        notifyDataSetChanged()
    }

    inner class DiscoverViewHolder(private val binding: DiscoverItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Any) {
            val title: String?
            val posterPath: String?

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
                    posterPath = item.attributes.posterImage?.medium // Direct URL for Anime
                }
                else -> {
                    title = "No Title Available"
                    posterPath = null
                }
            }

            binding.titleTextView.text = title

            Glide.with(binding.root.context)
                .load(posterPath ?: "")
                .into(binding.posterImageView)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}

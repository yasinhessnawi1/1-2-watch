package com.example.a1_2_watch.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.moduls.Movie
import com.example.a1_2_watch.databinding.MediaItemLayoutBinding

class MediaAdapter(
    private val onItemClick: (Movie) -> Unit,
    private val onSaveClick: (Movie) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MovieViewHolder>() {

    private var movies: MutableList<Movie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MediaItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            onItemClick(movie)
        }
        // Handle Save Button Click
        holder.binding.saveButton.setOnClickListener {
            onSaveClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun setMovies(movieList: List<Movie>) {
        this.movies.clear()
        this.movies.addAll(movieList)
        notifyDataSetChanged()
    }

    fun addMovies(movieList: List<Movie>) {
        val startPosition = movies.size
        movies.addAll(movieList)
        notifyItemRangeInserted(startPosition, movieList.size)
    }

    class MovieViewHolder(val binding: MediaItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            val mediaTitle = movie.title ?: movie.name ?: "No Title Available"
            binding.mediaTitleTextView.text = mediaTitle
            binding.mediaRatingTextView.text = "Rating: ${movie.vote_average}"
            Glide.with(binding.root.context)
                .load("https://image.tmdb.org/t/p/original/${movie.poster_path}")
                .into(binding.mediaImageView)
        }
    }
}

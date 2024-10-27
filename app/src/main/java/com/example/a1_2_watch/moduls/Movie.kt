package com.example.a1_2_watch.moduls

data class Movie(
    val id: Int,
    val title: String?,  // Nullable for TV shows
    val name: String?,   // Nullable for movies
    val overview: String,
    val poster_path: String?,
    val vote_average: Double
)

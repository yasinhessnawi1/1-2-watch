package com.example.a1_2_watch.moduls

data class MovieDetails(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val release_date: String?, // For movies
    val first_air_date: String? // For TV shows and anime
)

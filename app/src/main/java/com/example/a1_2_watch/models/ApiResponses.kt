package com.example.a1_2_watch.models

data class MovieResponse(
    val results: List<Movie>
)

data class ShowResponse(
    val results: List<Show>
)

data class AnimeResponse(
    val data: List<Anime>
)
package com.example.a1_2_watch.models

/**
 * Response model for a list of movies.
 *
 * @property results The list of movies to be returned from the API.
 */
data class MovieResponse(
    val results: List<Movie>
)

/**
 * Response model for a list of TV shows.
 *
 * @property results The list of TV shows to be returned from the API.
 */
data class ShowResponse(
    val results: List<Show>
)

/**
 * Response model for a list of anime.
 *
 * @property data The list of anime to be returned from the API.
 */
data class AnimeResponse(
    val data: List<Anime>
)
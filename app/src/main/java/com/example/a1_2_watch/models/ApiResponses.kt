package com.example.a1_2_watch.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for a list of movies.
 *
 * @property results The list of movies to be returned from the API.
 */
data class MovieResponse(
    @SerializedName("results") val results: List<Movie>
)

/**
 * Response model for a list of TV shows.
 *
 * @property results The list of TV shows to be returned from the API.
 */
data class ShowResponse(
    @SerializedName("results") val results: List<Show>
)

/**
 * Response model for a list of anime.
 *
 * @property data The list of anime to be returned from the API.
 */
data class AnimeResponse(
    @SerializedName("data") val data: List<Anime>
)
package com.example.a1_2_watch.models

/**
 * Data class representing a movie.
 *
 * @property id The unique ID of the movie.
 * @property title The title of the movie.
 * @property overview The brief description of the movie.
 * @property poster_path The path to the movie's poster image.
 * @property vote_average The average rating of the movie.
 * @property isLiked Indicates if the movie is marked as liked by the user or not.
 */
data class Movie(
    val id: Int,
    val title: String?,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    var isLiked: Boolean = false
)

/**
 * Data class representing a TV show.
 *
 * @property id The unique ID of the TV show.
 * @property name The name of the TV show.
 * @property overview The brief description of the TV show
 * @property poster_path The path to the TV show poster image.
 * @property vote_average The average rating of the TV show.
 * @property isLiked Indicates if the TV show is marked as liked by the user or not.
 */
data class Show(
    val id: Int,
    val name: String?,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    var isLiked: Boolean = false

)

/**
 * Data class representing an anime
 *
 * @property id The unique ID of the anime.
 * @property attributes Additional attributes containing additional details about the anime.
 * @property isLiked Indicates if the anime is marked as liked by the user or not.
 */
data class Anime(
    val id: Int,
    val attributes: Attributes,
    var isLiked: Boolean = false
)


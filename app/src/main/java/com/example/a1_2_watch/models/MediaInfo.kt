package com.example.a1_2_watch.models

import com.google.gson.annotations.SerializedName

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
    @SerializedName("id")  val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("vote_average") val vote_average: Double,
    @SerializedName("release_date") val release_date: String?,
    @SerializedName("isLiked") var isLiked: Boolean = false,
    @SerializedName("media_type")  var mediaType: MediaType = MediaType.MOVIES
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
    @SerializedName("id")  val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("vote_average") val vote_average: Double,
    @SerializedName("first_air_date") val first_air_date: String?,
    @SerializedName("isLiked") var isLiked: Boolean = false,
    @SerializedName("media_type")  var mediaType: MediaType = MediaType.TV_SHOWS
)



/**
 * Data class representing an anime
 *
 * @property id The unique ID of the anime.
 * @property attributes Additional attributes containing additional details about the anime.
 * @property isLiked Indicates if the anime is marked as liked by the user or not.
 */
data class Anime(
    @SerializedName("id")  val id: Int,
    @SerializedName("attributes") val attributes: Attributes,
    @SerializedName("isLiked") var isLiked: Boolean = false,
    @SerializedName("media_type")  var mediaType: MediaType = MediaType.ANIME
)




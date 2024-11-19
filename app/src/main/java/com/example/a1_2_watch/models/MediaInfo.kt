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
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("vote_average") val vote_average: Double?,
    @SerializedName("release_date") val release_date: String?,
    @SerializedName("isLiked") var isLiked: Boolean = false,
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
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("vote_average") val vote_average: Double?,
    @SerializedName("first_air_date") val first_air_date: String?,
    @SerializedName("isLiked") var isLiked: Boolean = false,
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
    @SerializedName("attributes") val attributes: MiniAttributes?,
    @SerializedName("isLiked") var isLiked: Boolean = false,
)

/**
 * Additional Attributes for an anime.
 *
 * @property synopsis Summary or plot of the anime.
 * @property canonicalTitle Primary title.
 * @property averageRating Average rating score.
 * @property posterImage Poster image details.
 */
data class MiniAttributes(
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("canonicalTitle") val canonicalTitle: String?,
    @SerializedName("averageRating") val averageRating: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("posterImage") val posterImage: PosterImage?,
)


/**
 * Data class representing a minimized media item for search results.
 *
 * @param id The ID of the media item.
 * @param title The title of the media item.
 * @param posterPath The poster path for the media item.
 * @param type The type of the media (e.g., MOVIE, TV_SHOW, ANIME).
 */
data class MinimizedItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("posterPath") val posterPath: String?,
    @SerializedName("type") val type: String,
)





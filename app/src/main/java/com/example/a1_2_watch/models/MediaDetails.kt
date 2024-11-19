package com.example.a1_2_watch.models

import com.google.gson.annotations.SerializedName

/**
 * Detailed information about a movie.
 *
 * @property backdrop_path Path to the backdrop image.
 * @property budget Budget of the movie in USD.
 * @property genres List of genres for the movie.
 * @property id Unique ID for the movie.
 * @property overview Short description or plot summary.
 * @property popularity Popularity score.
 * @property poster_path Path to the poster image.
 * @property release_date Release date in YYYY-MM-DD format.
 * @property revenue Total revenue in USD.
 * @property runtime Runtime in minutes.
 * @property status Current status.
 * @property title Title of the movie.
 * @property vote_average Average vote score.
 */
data class MovieDetails(
    @SerializedName("backdrop_path") val backdrop_path: String?,
    @SerializedName("budget") val budget: Int,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("id") val id: Int,
    @SerializedName("overview") val overview: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("release_date") val release_date: String,
    @SerializedName("revenue") val revenue: Int,
    @SerializedName("runtime") val runtime: Int,
    @SerializedName("status") val status: String,
    @SerializedName("title") val title: String,
    @SerializedName("vote_average") val vote_average: Double,
)

/**
 * Detailed information about a TV show.
 *
 * @property episode_run_time Average runtime of episodes.
 * @property first_air_date Date of the first episode.
 * @property genres List of genres for the show.
 * @property id Unique identifier for the show.
 * @property last_air_date Date of the last aired episode.
 * @property last_episode_to_air Details of the last aired episode.
 * @property name Name of the show.
 * @property next_episode_to_air Details of the next episode.
 * @property number_of_episodes Total number of episodes.
 * @property number_of_seasons Total number of seasons.
 * @property overview Short description or plot summary.
 * @property poster_path Path to the poster image.
 * @property status Current status (e.g., Ended).
 * @property vote_average Average vote score.
 */
data class ShowDetails(
    @SerializedName("episode_run_time") val episode_run_time: List<Int>,
    @SerializedName("first_air_date") val first_air_date: String,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("id") val id: Int,
    @SerializedName("last_air_date") val last_air_date: String?,
    @SerializedName("last_episode_to_air") val last_episode_to_air: LastEpisodeToAir?,
    @SerializedName("name") val name: String,
    @SerializedName("next_episode_to_air") val next_episode_to_air: LastEpisodeToAir?,
    @SerializedName("number_of_episodes") val number_of_episodes: Int,
    @SerializedName("number_of_seasons") val number_of_seasons: Int,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("backdrop_path") val backdrop_path: String?,
    @SerializedName("status") val status: String,
    @SerializedName("vote_average") val vote_average: Double,
)


/**
 * Detailed information about an anime.
 *
 * @property data Main anime data.
 */
data class AnimeDetails(
    @SerializedName("data") val data: AnimeData
)

/**
 * Main data model for an anime.
 *
 * @property id Unique ID.
 * @property type Type of media.
 * @property attributes Attributes of the anime (e.g., titles, poster).
 */
data class AnimeData(
   @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: Attributes?
)

/**
 * Additional Attributes for an anime.
 *
 * @property synopsis Summary or plot of the anime.
 * @property canonicalTitle Primary title.
 * @property averageRating Average rating score.
 * @property startDate Start date of the anime.
 * @property endDate End date of the anime.
 * @property subtype Subtype.
 * @property status Current status (e.g., finished).
 * @property posterImage Poster image details.
 */
data class Attributes(
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("canonicalTitle") val canonicalTitle: String?,
    @SerializedName("averageRating") val averageRating: String,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("nextRelease") val nextRelease: String?,
    @SerializedName("subtype") val subtype: String,
    @SerializedName("status") val status: String,
    @SerializedName("posterImage") val posterImage : PosterImage?,
    @SerializedName("episodeCount") val episodeCount: String?,
    @SerializedName("episodeLength") val episodeLength: Int
)



/**
 * Titles in multiple languages for an anime.
 *
 * @property data Main anime data.
 * @property included Additional data witch is related items.
 */
data class AnimeResponseIncluded(
    @SerializedName("data") val data: Anime,
    @SerializedName("included") val included: List<IncludedData>?
)

/**
 * Main data for included items.
 *
 * @property id Unique ID.
 * @property type Type of media.
 * @property relationships Relationships to other media.
 */
data class IncludedData(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("relationships") val relationships: Relationships?
)

/**
 * Relationships to other media.
 *
 * @property destination Destination of the relationship.
 */
data class Relationships(
    @SerializedName("destination") val destination: Destination?
)

/**
 * Destination of the relationship.
 *
 * @property data Destination data.
 */
data class Destination(
 @SerializedName("data") val data: DestinationData
)

/**
 * Destination data.
 *
 * @property id Unique ID.
 * @property type Type of media.
 */
data class DestinationData(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)


/**
 * Poster image details in various sizes.
 *
 * @property original Original image URL.
 */
data class PosterImage(
    @SerializedName("original") val original: String?
)



/**
 * Links to a related resource.
 *
 * @property links Links to access the related resource.
 */
data class RelationshipLinks(
    @SerializedName("links") val links: Links
)

/**
 * URLs for related resources.
 *
 * @property self Link to this resource.
 */
data class Links(
    @SerializedName("self") val self: String
)


/**
 * Information about the last aired episode of a TV show.
 *
 * @property id Unique ID of the episode.
 * @property name Name of the episode.
 * @property air_date Air date of the episode.
 * @property episodeNumber Episode number.
 */
data class LastEpisodeToAir(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("air_date") val air_date: String,
    @SerializedName("episode_number") val episodeNumber: Int,
)

/**
 * Genre information.
 *
 * @property id Unique ID of the genre.
 * @property name Name of the genre.
 */
data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)



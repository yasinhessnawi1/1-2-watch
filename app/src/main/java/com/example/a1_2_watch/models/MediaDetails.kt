package com.example.a1_2_watch.models

import com.google.gson.annotations.SerializedName

/**
 * Detailed information about a movie.
 *
 * @property backdrop_path Path to the backdrop image.
 * @property budget Budget of the movie in USD.
 * @property genres List of genres for the movie.
 * @property id Unique ID for the movie.
 * @property original_language Original language code.
 * @property original_title Original title of the movie.
 * @property overview Short description or plot summary.
 * @property popularity Popularity score.
 * @property poster_path Path to the poster image.
 * @property release_date Release date in YYYY-MM-DD format.
 * @property revenue Total revenue in USD.
 * @property runtime Runtime in minutes.
 * @property status Current status.
 * @property tagline Catchy phrase or tagline.
 * @property title Title of the movie.
 * @property video Indicates if movie has a video associated with it.
 * @property vote_average Average vote score.
 */
data class MovieDetails(
    @SerializedName("backdrop_path") val backdrop_path: String?,
    @SerializedName("budget") val budget: Int,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("id") val id: Int,
    @SerializedName("original_language") val original_language: String,
    @SerializedName("original_title") val original_title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("release_date") val release_date: String,
    @SerializedName("revenue") val revenue: Int,
    @SerializedName("runtime") val runtime: Int,
    @SerializedName("status") val status: String,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("title") val title: String,
    @SerializedName("video") val video: Boolean,
    @SerializedName("vote_average") val vote_average: Double,
)

/**
 * Detailed information about a TV show.
 *
 * @property adult Indicates if the show is for adults.
 * @property backdrop_path Path to the backdrop image.
 * @property created_by List of creators of the show.
 * @property episode_run_time Average runtime of episodes.
 * @property first_air_date Date of the first episode.
 * @property genres List of genres for the show.
 * @property id Unique identifier for the show.
 * @property in_production Indicates if new episodes are being produced.
 * @property last_air_date Date of the last aired episode.
 * @property last_episode_to_air Details of the last aired episode.
 * @property name Name of the show.
 * @property next_episode_to_air Details of the next episode.
 * @property number_of_episodes Total number of episodes.
 * @property number_of_seasons Total number of seasons.
 * @property overview Short description or plot summary.
 * @property popularity Popularity score.
 * @property poster_path Path to the poster image.
 * @property seasons List of seasons.
 * @property status Current status (e.g., Ended).
 * @property tagline Catchy phrase or tagline.
 * @property vote_average Average vote score.
 * @property vote_count Number of votes received.
 */
data class ShowDetails(
    @SerializedName("backdrop_path") val backdrop_path: String?,
    @SerializedName("created_by") val created_by: List<Creator>,
    @SerializedName("episode_run_time") val episode_run_time: List<Int>,
    @SerializedName("first_air_date") val first_air_date: String,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("id") val id: Int,
    @SerializedName("in_production") val in_production: Boolean,
    @SerializedName("last_air_date") val last_air_date: String?,
    @SerializedName("last_episode_to_air") val last_episode_to_air: LastEpisodeToAir?,
    @SerializedName("name") val name: String,
    @SerializedName("next_episode_to_air") val next_episode_to_air: LastEpisodeToAir?,
    @SerializedName("number_of_episodes") val number_of_episodes: Int,
    @SerializedName("number_of_seasons") val number_of_seasons: Int,
    @SerializedName("overview") val overview: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("seasons") val seasons: List<Season>,
    @SerializedName("status") val status: String,
    @SerializedName("tagline") val tagline: String?,
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
 * @property links Links related to the anime.
 * @property attributes Attributes of the anime (e.g., titles, poster).
 */
data class AnimeData(
   @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("links") val links: Links,
    @SerializedName("attributes") val attributes: Attributes
)

/**
 * Additional Attributes for an anime.
 *
 * @property createdAt Creation date of the anime record.
 * @property updatedAt Last update date of the anime record.
 * @property slug URL-friendly title.
 * @property synopsis Summary or plot of the anime.
 * @property description Detailed description.
 * @property coverImageTopOffset Offset for the cover image.
 * @property titles Titles in different languages.
 * @property canonicalTitle Primary title.
 * @property abbreviatedTitles List of short titles.
 * @property averageRating Average rating score.
 * @property ratingFrequencies Frequency distribution of ratings.
 * @property userCount Number of users who rated or marked the anime.
 * @property favoritesCount Number of users who favorites the anime.
 * @property startDate Start date of the anime.
 * @property endDate End date of the anime.
 * @property popularityRank Popularity rank.
 * @property ratingRank Rating rank.
 * @property ageRating Age rating.
 * @property ageRatingGuide Guide for the age rating.
 * @property subtype Subtype.
 * @property status Current status (e.g., finished).
 * @property posterImage Poster image details.
 * @property coverImage Cover image details.
 */
data class Attributes(
    val synopsis: String?,
    val canonicalTitle: String?,
    val averageRating: String,
    val startDate: String?,
    val endDate: String?,
    val nextRelease: String?,
    val subtype: String,
    val status: String,
    val posterImage: PosterImage?,
    val episodeCount: String?,
    val episodeLength: Int
)

/**
 * Titles in multiple languages for an anime.
 *
 * @property en English title.
 * @property enJp Japanese title (in English).
 * @property jaJp Japanese title.
 */
data class AnimeResponseIncluded(
    val data: Anime,
    val included: List<IncludedData>?
)
data class IncludedData(
    val id: String,
    val type: String,
    val relationships: Relationships?
)

data class Relationships(
    val destination: Destination?
)

data class Destination(
    val data: DestinationData
)

data class DestinationData(
    val id: String,
    val type: String
)


/**
 * Poster image details in various sizes.
 *
 * @property tiny Tiny-sized image URL.
 * @property large Large-sized image URL.
 * @property small Small-sized image URL.
 * @property medium Medium-sized image URL.
 * @property original Original image URL.
 */
data class PosterImage(
    val small: String?,
    val medium: String?,
    val original: String?
)



/**
 * Links to a related resource.
 *
 * @property links Links to access the related resource.
 */
data class RelationshipLinks(
    val links: Links
)

/**
 * URLs for related resources.
 *
 * @property self Link to this resource.
 */
data class Links(
    val self: String
)

/**
 * Information about a TV show's creator.
 *
 * @property id Unique ID of the creator.
 * @property creditId Credit ID of the creator.
 * @property name Name of the creator.
 * @property originalName Original name of the creator.
 * @property gender Gender code of the creator.
 * @property profilePath Path to the profile image.
 */
data class Creator(
    val id: Int,
    val creditId: String,
    val name: String,
    val originalName: String,
    val gender: Int,
    val profilePath: String?
)

/**
 * Information about the last aired episode of a TV show.
 *
 * @property id Unique ID of the episode.
 * @property name Name of the episode.
 * @property overview Summary of the episode.
 * @property voteAverage Average rating for the episode.
 * @property voteCount Total votes for the episode.
 * @property air_date Air date of the episode.
 * @property episodeNumber Episode number.
 * @property episodeType Type of episode.
 * @property productionCode Production code for the episode.
 * @property runtime Duration in minutes.
 * @property seasonNumber Season number.
 * @property showId ID of the show.
 * @property stillPath Path to the episode's still image.
 */
data class LastEpisodeToAir(
    val id: Int,
    val name: String,
    val overview: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val air_date: String,
    val episodeNumber: Int,
    val episodeType: String,
    val productionCode: String,
    val runtime: Int,
    val seasonNumber: Int,
    val showId: Int,
    val stillPath: String?
)

/**
 * Genre information.
 *
 * @property id Unique ID of the genre.
 * @property name Name of the genre.
 */
data class Genre(
    val id: Int,
    val name: String
)

/**
 * Season details for a TV show.
 *
 * @property airDate Air date of the season.
 * @property episodeCount Number of episodes in the season.
 * @property id Unique ID of the season.
 * @property name Name of the season.
 * @property overview Summary of the season.
 * @property posterPath Path to the poster image.
 * @property seasonNumber Season number.
 * @property voteAverage Average rating for the season.
 */
data class Season(
    val airDate: String?,
    val episodeCount: Int,
    val id: Int,
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val seasonNumber: Int,
    val voteAverage: Double
)


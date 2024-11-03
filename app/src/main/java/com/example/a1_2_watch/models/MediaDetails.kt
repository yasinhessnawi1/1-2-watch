package com.example.a1_2_watch.models

/**
 * Detailed information about a movie.
 *
 * @property adult Indicates if the movie is for adults.
 * @property backdrop_path Path to the backdrop image.
 * @property belongs_to_collection Collection information, if any.
 * @property budget Budget of the movie in USD.
 * @property genres List of genres for the movie.
 * @property homepage Official homepage URL.
 * @property id Unique ID for the movie.
 * @property imdb_id IMDb ID of the movie.
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
 * @property vote_count Number of votes received.
 */
data class MovieDetails(
    val adult: Boolean,
    val backdrop_path: String?,
    val belongs_to_collection: Any?,
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val imdb_id: String?,
    val original_language: String,
    val original_title: String,
    val overview: String?,
    val popularity: Double,
    val poster_path: String?,
    val release_date: String,
    val revenue: Int,
    val runtime: Int,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
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
    val adult: Boolean,
    val backdrop_path: String?,
    val created_by: List<Creator>,
    val episode_run_time: List<Int>,
    val first_air_date: String,
    val genres: List<Genre>,
    val id: Int,
    val in_production: Boolean,
    val last_air_date: String?,
    val last_episode_to_air: LastEpisodeToAir?,
    val name: String,
    val next_episode_to_air: Any?,
    val number_of_episodes: Int,
    val number_of_seasons: Int,
    val overview: String?,
    val popularity: Double,
    val poster_path: String?,
    val seasons: List<Season>,
    val status: String,
    val tagline: String?,
    val vote_average: Double,
    val vote_count: Int
)

/**
 * Detailed information about an anime.
 *
 * @property data Main anime data.
 */
data class AnimeDetails(
    val data: AnimeData?
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
    val id: String?,
    val type: String?,
    val links: Links?,
    val attributes: Attributes?
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
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val synopsis: String?,
    val description: String?,
    val coverImageTopOffset: Int,
    val titles: Titles,
    val canonicalTitle: String,
    val abbreviatedTitles: List<String>,
    val averageRating: String,
    val ratingFrequencies: Map<String, String>,
    val userCount: Int,
    val favoritesCount: Int,
    val startDate: String?,
    val endDate: String?,
    val popularityRank: Int,
    val ratingRank: Int,
    val ageRating: String?,
    val ageRatingGuide: String?,
    val subtype: String,
    val status: String,
    val posterImage: PosterImage?,
    val coverImage: CoverImage?
)

/**
 * Titles in multiple languages for an anime.
 *
 * @property en English title.
 * @property enJp Japanese title (in English).
 * @property jaJp Japanese title.
 */
data class Titles(
    val en: String?,
    val enJp: String?,
    val jaJp: String?
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
    val tiny: String?,
    val large: String?,
    val small: String?,
    val medium: String?,
    val original: String?
)

/**
 * Cover image details in various sizes.
 *
 * @property tiny Tiny-sized cover image URL.
 * @property large Large-sized cover image URL.
 * @property small Small-sized cover image URL.
 * @property original Original cover image URL.
 */
data class CoverImage(
    val tiny: String?,
    val large: String?,
    val small: String?,
    val original: String?
)

/**
 * Relationships with other anime or media.
 *
 * @property genres Link to related genres.
 * @property categories Link to related categories.
 * @property castings Link to related castings.
 * @property installments Link to related installments.
 * @property mappings Link to related mappings.
 * @property reviews Link to related reviews.
 * @property mediaRelationships Link to related media.
 * @property characters Link to related characters.
 * @property staff Link to related staff.
 * @property productions Link to related productions.
 * @property quotes Link to related quotes.
 * @property episodes Link to related episodes.
 * @property streamingLinks Link to related streaming sources.
 * @property animeProductions Link to anime production information.
 * @property animeCharacters Link to anime character information.
 * @property animeStaff Link to anime staff information.
 */
data class Relationships(
    val genres: RelationshipLinks,
    val categories: RelationshipLinks,
    val castings: RelationshipLinks,
    val installments: RelationshipLinks,
    val mappings: RelationshipLinks,
    val reviews: RelationshipLinks,
    val mediaRelationships: RelationshipLinks,
    val characters: RelationshipLinks,
    val staff: RelationshipLinks,
    val productions: RelationshipLinks,
    val quotes: RelationshipLinks,
    val episodes: RelationshipLinks,
    val streamingLinks: RelationshipLinks,
    val animeProductions: RelationshipLinks,
    val animeCharacters: RelationshipLinks,
    val animeStaff: RelationshipLinks
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
 * @property airDate Air date of the episode.
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
    val airDate: String,
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


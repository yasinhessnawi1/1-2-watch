package com.example.a1_2_watch.models

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

data class AnimeDetails(
    val data: AnimeData?
)

data class AnimeData(
    val id: String?,
    val type: String?,
    val links: Links?,
    val attributes: Attributes?
)

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

data class Titles(
    val en: String?,
    val enJp: String?,
    val jaJp: String?
)

data class PosterImage(
    val tiny: String?,
    val large: String?,
    val small: String?,
    val medium: String?,
    val original: String?
)

data class CoverImage(
    val tiny: String?,
    val large: String?,
    val small: String?,
    val original: String?
)

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

data class RelationshipLinks(
    val links: Links
)

data class Links(
    val self: String
)

data class Creator(
    val id: Int,
    val creditId: String,
    val name: String,
    val originalName: String,
    val gender: Int,
    val profilePath: String?
)

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

data class Genre(
    val id: Int,
    val name: String
)

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


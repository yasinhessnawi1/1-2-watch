package com.example.a1_2_watch.models

data class MovieDetails(
    val adult: Boolean,
    val backdropPath: String?,
    val belongsToCollection: Any?,
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val imdbId: String?,
    val originCountry: List<String>,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String?,
    val popularity: Double,
    val posterPath: String?,
    val releaseDate: String,
    val revenue: Int,
    val runtime: Int,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int
)


data class ShowDetails(
    val adult: Boolean,
    val backdropPath: String?,
    val createdBy: List<Creator>,
    val episodeRunTime: List<Int>,
    val firstAirDate: String,
    val genres: List<Genre>,
    val id: Int,
    val inProduction: Boolean,
    val lastAirDate: String,
    val lastEpisodeToAir: LastEpisodeToAir?,
    val name: String,
    val nextEpisodeToAir: Any?,
    val numberOfEpisodes: Int,
    val numberOfSeasons: Int,
    val overview: String?,
    val popularity: Double,
    val posterPath: String?,
    val seasons: List<Season>,
    val status: String,
    val tagline: String?,
    val voteAverage: Double,
    val voteCount: Int
)
data class AnimeDetails(
    val id: String,
    val type: String,
    val links: Links,
    val attributes: Attributes,
    val relationships: Relationships
)




data class Attributes(
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val synopsis: String,
    val description: String,
    val coverImageTopOffset: Int,
    val titles: Titles,
    val canonicalTitle: String,
    val abbreviatedTitles: List<String>,
    val averageRating: String,
    val ratingFrequencies: Map<String, String>,
    val userCount: Int,
    val favoritesCount: Int,
    val startDate: String,
    val endDate: String?,
    val nextRelease: Any?,
    val popularityRank: Int,
    val ratingRank: Int,
    val ageRating: String,
    val ageRatingGuide: String,
    val subtype: String,
    val status: String,
    val tba: Any?,
    val posterImage: PosterImage?,
    val coverImage: CoverImage?,
    val episodeCount: Int,
    val episodeLength: Int,
    val totalLength: Int,
    val youtubeVideoId: String?,
    val showType: String,
    val nsfw: Boolean
)

data class Titles(
    val en: String,
    val enJp: String,
    val jaJp: String
)

data class PosterImage(
    val tiny: String,
    val large: String,
    val small: String,
    val medium: String,
    val original: String,
)

data class CoverImage(
    val tiny: String,
    val large: String,
    val small: String,
    val original: String,
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


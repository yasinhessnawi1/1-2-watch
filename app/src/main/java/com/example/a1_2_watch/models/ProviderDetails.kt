package com.example.a1_2_watch.data

import com.example.a1_2_watch.models.RelationshipLinks

data class Provider(
    val provider_name: String,
    val logo_path: String,
)

data class WatchProvidersResponse(
    val results: Map<String, WatchProviderDetails> // Mapping country codes to providers
)

data class WatchProviderDetails(
    val link: String, // Link to the provider page on TMDB
    val flatrate: List<Provider>? // List of streaming services (optional)
)

data class AnimeStreamingLinksResponse(
    val data: List<StreamingLink>
)

data class StreamingLink(
    val id: String,
    val attributes: StreamingLinkAttributes,
    val relationships: StreamingLinkRelationships
)

data class StreamingLinkAttributes(
    val url: String,
    val subs: List<String>?,
    val dubs: List<String>?
)

data class StreamingLinkRelationships(
    val streamer: RelationshipLinks
)

data class StreamerDetailsResponse(
    val data: Streamer
)

data class Streamer(
    val id: String,
    val attributes: StreamerAttributes
)

data class StreamerAttributes(
    val siteName: String
)

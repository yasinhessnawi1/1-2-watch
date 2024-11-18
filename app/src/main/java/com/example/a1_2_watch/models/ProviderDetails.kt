package com.example.a1_2_watch.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a streaming provider with its name and logo path.
 *
 * @property providerName The name of the provider.
 * @property logoPath The path to the provider's logo.
 */
data class Provider(
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("logo_path") val logoPath: String?
)

/**
 * Response model for watch providers, mapping country codes to available providers.
 *
 * @property results A map of country codes to watch provider details.
 */
data class WatchProvidersResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("results") val results: Map<String, ProvidersRegion>
)

/**
 * Details about a watch provider, including a link and available services.
 *
 * @property flatrate Optional list of streaming services available in the region.
 */
data class ProvidersRegion(
    @SerializedName("flatrate") val flatrate: List<Provider>? = null,
)

/**
 * Response model for anime streaming links.
 *
 * @property data List of available streaming links for anime.
 */
data class AnimeStreamingLinksResponse(
    val data: List<StreamingLink>
)

/**
 * Represents a specific streaming link for anime content.
 *
 * @property id Unique identifier of the streaming link.
 * @property attributes Attributes of the streaming link, such as URL and languages.
 * @property relationships Relationships of the streaming link, like linked streamer.
 */
data class StreamingLink(
    val id: String,
    val attributes: StreamingLinkAttributes,
    val relationships: StreamingLinkRelationships
)

/**
 * Attributes for a streaming link, including URL and available languages.
 *
 * @property url Direct URL to the streaming content.
 * @property subs Optional list of available subtitle languages.
 * @property dubs Optional list of available dubbed languages.
 */
data class StreamingLinkAttributes(
    val url: String,
    val subs: List<String>?,
    val dubs: List<String>?
)

/**
 * Relationships for a streaming link, such as linked streamer information.
 *
 * @property streamer Links to related streamer details.
 */
data class StreamingLinkRelationships(
    val streamer: RelationshipLinks
)

/**
 * Response model for details about a specific streamer.
 *
 * @property data Contains details of the streamer.
 */
data class StreamerDetailsResponse(
    val data: Streamer
)

/**
 * Represents a streamer with unique ID and attributes.
 *
 * @property id Unique identifier of the streamer.
 * @property attributes Additional attributes of the streamer, like site name.
 */
data class Streamer(
    val id: String,
    val attributes: StreamerAttributes
)

/**
 * Attributes of a streamer, such as the name of the streaming site.
 *
 * @property siteName Name of the streaming site.
 */
data class StreamerAttributes(
    val siteName: String
)

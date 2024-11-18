package com.example.a1_2_watch.repository

import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DetailsRepository class responsible for fetching detailed information about media items.
 */
class DetailsRepository {
    // The API key used to authenticate API requests for TMDB.
    private val apiKey = Constants.API_KEY
    private val apiService = ApiClient.getApiService()
    private val kitsuApiService = ApiClient.getApiService(Constants.KITSU_URL)

    /**
     * Fetches detailed information about a movie.
     *
     * @param movieId The unique ID of the movie.
     * @return The MovieDetails object or null if the request fails.
     */
    suspend fun fetchMovieDetails(movieId: Int): MovieDetails? = withContext(Dispatchers.IO) {
        val response = apiService.getMovieDetails(movieId, apiKey)
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Fetches detailed information about a TV show.
     *
     * @param tvId The unique ID of the TV show.
     * @return The ShowDetails object or null if the request fails.
     */
    suspend fun fetchTVShowDetails(tvId: Int): ShowDetails? = withContext(Dispatchers.IO) {
        val response = apiService.getTVShowDetails(tvId, apiKey)
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Fetches detailed information about an anime.
     *
     * @param animeId The unique ID of the anime.
     * @return The AnimeDetails object or null if the request fails.
     */
    suspend fun fetchAnimeDetails(animeId: Int): AnimeDetails? = withContext(Dispatchers.IO) {
        val response = kitsuApiService.getAnimeDetails(animeId.toString())
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Fetches watch providers for a movie or TV show.
     *
     * @param mediaId The unique ID of the media item.
     * @param mediaType The type of the media item (movie or TV show).
     * @return The WatchProvidersResponse object or null if the request fails.
     */
    suspend fun fetchWatchProviders(
        mediaId: Int,
        mediaType: MediaType
    ): WatchProvidersResponse? = withContext(Dispatchers.IO) {
        val response = when (mediaType) {
            MediaType.MOVIES -> apiService.getWatchProviders(mediaId, apiKey)
            MediaType.TV_SHOWS -> apiService.getTVShowWatchProviders(mediaId, apiKey)
            else -> return@withContext null
        }
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Fetches streaming links for an anime.
     *
     * @param animeId The unique ID of the anime.
     * @return The list of StreamingLink objects or null if the request fails.
     */
    suspend fun fetchAnimeStreamingLinks(animeId: String): List<StreamingLink>? = withContext(Dispatchers.IO) {
        val response = kitsuApiService.getAnimeStreamingLinks(animeId)
        if (response.isSuccessful) response.body()?.data else null
    }

    /**
     * Fetches streamer details based on the streaming link ID.
     *
     * @param streamerLinkId The unique ID of the streaming link.
     * @return The StreamerDetailsResponse object or null if the request fails.
     */
    suspend fun fetchStreamerDetails(streamerLinkId: String): StreamerDetailsResponse? = withContext(Dispatchers.IO) {
        val response = kitsuApiService.getStreamerDetails(streamerLinkId)
        if (response.isSuccessful) response.body() else null
    }
}
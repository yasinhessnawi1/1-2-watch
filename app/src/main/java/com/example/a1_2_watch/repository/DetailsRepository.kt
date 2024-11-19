package com.example.a1_2_watch.repository

import android.util.Log
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
    private val tmdbapiService = ApiClient.getApiService()
    private val kitsuApiService = ApiClient.getApiService(Constants.KITSU_URL)

    /**
     * Fetches detailed information about a movie.
     *
     * @param movieId The unique ID of the movie.
     * @return The MovieDetails object or null if the request fails.
     */
    suspend fun fetchMovieDetails(movieId: Int): MovieDetails? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = tmdbapiService.getMovieDetails(movieId, apiKey)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching movie details for movieId: $movieId", e)
            null
        }
    }

    /**
     * Fetches detailed information about a TV show.
     *
     * @param tvId The unique ID of the TV show.
     * @return The ShowDetails object or null if the request fails.
     */
    suspend fun fetchTVShowDetails(tvId: Int): ShowDetails? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = tmdbapiService.getTVShowDetails(tvId, apiKey)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching TV show details for tvId: $tvId", e)
            null
        }
    }

    /**
     * Fetches detailed information about an anime.
     *
     * @param animeId The unique ID of the anime.
     * @return The AnimeDetails object or null if the request fails.
     */
    suspend fun fetchAnimeDetails(animeId: Int): AnimeDetails? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = kitsuApiService.getAnimeDetails(animeId.toString())
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching anime details for animeId: $animeId", e)
            null
        }
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
        return@withContext try {
            val response = when (mediaType) {
                MediaType.MOVIES -> tmdbapiService.getWatchProviders(mediaId, apiKey)
                MediaType.TV_SHOWS -> tmdbapiService.getTVShowWatchProviders(mediaId, apiKey)
                else -> return@withContext null
            }
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching watch providers for mediaId: $mediaId, mediaType: $mediaType", e)
            null
        }
    }

    /**
     * Fetches streaming links for an anime.
     *
     * @param animeId The unique ID of the anime.
     * @return The list of StreamingLink objects or null if the request fails.
     */
    suspend fun fetchAnimeStreamingLinks(animeId: String): List<StreamingLink>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = kitsuApiService.getAnimeStreamingLinks(animeId)
            if (response.isSuccessful) response.body()?.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching anime streaming links for animeId: $animeId", e)
            null
        }
    }

    /**
     * Fetches streamer details based on the streaming link ID.
     *
     * @param streamerLinkId The unique ID of the streaming link.
     * @return The StreamerDetailsResponse object or null if the request fails.
     */
    suspend fun fetchStreamerDetails(streamerLinkId: String): StreamerDetailsResponse? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = kitsuApiService.getStreamerDetails(streamerLinkId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching streamer details for streamerLinkId: $streamerLinkId", e)
            null
        }
    }

    companion object {
        private const val TAG = "DetailsRepository"
    }
}

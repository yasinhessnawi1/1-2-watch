package com.example.a1_2_watch.repository

import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.AnimeResponse
import com.example.a1_2_watch.models.MovieResponse
import com.example.a1_2_watch.models.ShowResponse
import com.example.a1_2_watch.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DiscoverRepository class for performing media searches using the TMDB API for movies and TV shows,
 * and Kitsu API for anime.
 */
class DiscoverRepository {
    // TMDB API for movies and TV shows searches.
    private val tmdbApiService = ApiClient.getApiService(Constants.TMDB_URL)
    // Kitsu API for anime searches.
    private val kitsuApiService = ApiClient.getApiService(Constants.KITSU_URL)
    private val apiKey = Constants.API_KEY

    /**
     * Searches for movies using the TMDB API.
     *
     * @param query The search query for movie titles.
     * @return The MovieResponse object or null if the request fails.
     */
    suspend fun searchMovies(query: String): MovieResponse? = withContext(Dispatchers.IO) {
        val response = tmdbApiService.searchMovies(apiKey, query)
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Searches for TV shows using the TMDB API.
     *
     * @param query The search query for TV show titles.
     * @return The ShowResponse object or null if the request fails.
     */
    suspend fun searchTVShows(query: String): ShowResponse? = withContext(Dispatchers.IO) {
        val response = tmdbApiService.searchTVShows(apiKey, query)
        if (response.isSuccessful) response.body() else null
    }

    /**
     * Searches for anime using the Kitsu API.
     *
     * @param query The search query for anime titles.
     * @return The AnimeResponse object or null if the request fails.
     */
    suspend fun searchAnime(query: String): AnimeResponse? = withContext(Dispatchers.IO) {
        val response = kitsuApiService.searchAnime(query)
        if (response.isSuccessful) response.body() else null
    }
}
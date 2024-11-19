package com.example.a1_2_watch.repository

import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.KITSU_URL

/**
 * Repository responsible for interacting with remote APIs to fetch media data, such as movies, TV shows, and anime.
 * Provides an abstraction layer between the API clients and the rest of the application.
 */
class MediaRepository {

    // API key used for authenticating requests to the TMDB API.
    private val apiKey = Constants.API_KEY

    // TMDB API service instance.
    private val tmdbApiService = ApiClient.getApiService()

    // Kitsu API service instance for fetching anime data.
    private val kitsuApiService = ApiClient.getApiService(KITSU_URL)

    /**
     * Fetches a list of popular movies from the TMDB API.
     *
     * @param page The page number for pagination.
     * @return A list of [Movie] objects representing popular movies, or an empty list if the request fails.
     */
    suspend fun fetchPopularMovies(page: Int): List<Movie> {
        return try {
            val response = tmdbApiService.getPopularMovies(apiKey, page)
            if (response.isSuccessful) {
                // Retrieve up to 15 popular movies, or return an empty list if no results are found.
                response.body()?.results?.subList(0, 15) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular movies for page: $page", e)
            emptyList()
        }
    }

    /**
     * Fetches a list of popular TV shows from the TMDB API.
     *
     * @param page The page number for pagination.
     * @return A list of [Show] objects representing popular TV shows, or an empty list if the request fails.
     */
    suspend fun fetchPopularTVShows(page: Int): List<Show> {
        return try {
            val response = tmdbApiService.getPopularTVShows(apiKey, page)
            if (response.isSuccessful) {
                // Retrieve up to 15 popular TV shows, or return an empty list if no results are found.
                response.body()?.results?.subList(0, 15) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular TV shows for page: $page", e)
            emptyList()
        }
    }

    /**
     * Fetches a list of popular anime from the Kitsu API.
     *
     * @param page The page number for pagination.
     * @param limit The maximum number of items per page.
     * @return A list of [Anime] objects representing popular anime, or an empty list if the request fails.
     */
    suspend fun fetchPopularAnime(page: Int, limit: Int): List<Anime> {
        return try {
            val offset = (page - 1) * limit
            val response = kitsuApiService.getPopularAnimeKitsu(limit, offset)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular anime for page: $page, limit: $limit", e)
            emptyList()
        }
    }

    /**
     * Fetches a list of movies related to a specific movie from the TMDB API.
     *
     * @param movieId The unique ID of the movie.
     * @return A list of [Movie] objects representing related movies, or an empty list if the request fails.
     */
    suspend fun fetchRelatedMovies(movieId: Int): List<Movie> {
        return try {
            val response = tmdbApiService.getRelatedMovies(movieId, apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching related movies for movieId: $movieId", e)
            emptyList()
        }
    }

    /**
     * Fetches a list of TV shows related to a specific TV show from the TMDB API.
     *
     * @param tvShowId The unique ID of the TV show.
     * @return A list of [Show] objects representing related TV shows, or an empty list if the request fails.
     */
    suspend fun fetchRelatedTVShows(tvShowId: Int): List<Show> {
        return try {
            val response = tmdbApiService.getRelatedTVShows(tvShowId, apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching related TV shows for tvShowId: $tvShowId", e)
            emptyList()
        }
    }

    /**
     * Fetches detailed information about a specific anime, including its relationships, from the Kitsu API.
     *
     * @param animeId The unique ID of the anime.
     * @return An [AnimeResponseIncluded] object containing the anime's relationships, or `null` if the request fails.
     */
    suspend fun getAnimeWithRelationships(animeId: String): AnimeResponseIncluded? {
        return try {
            val response = kitsuApiService.getAnime(animeId, "mediaRelationships.destination")
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching anime with relationships for animeId: $animeId", e)
            null
        }
    }

    /**
     * Extracts related anime IDs from the response containing anime relationships.
     *
     * @param animeResponse The response object containing the anime's relationships.
     * @return A list of related anime IDs, or an empty list if no relationships are found.
     */
    fun extractRelatedAnimeIds(animeResponse: AnimeResponseIncluded): List<String> {
        return try {
            animeResponse.included
                ?.filter { it.type == "mediaRelationships" } // Filter relationships by type.
                ?.mapNotNull { it.relationships?.destination?.data } // Extract related data.
                ?.filter { it.type == "anime" } // Ensure only anime types are included.
                ?.map { it.id } // Map to the list of IDs.
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting related anime IDs", e)
            emptyList()
        }
    }

    /**
     * Fetches a list of anime related to a specific anime ID using the Kitsu API.
     *
     * @param animeId The unique ID of the anime.
     * @return A list of [Anime] objects representing related anime, or an empty list if the request fails.
     */
    suspend fun getRelatedAnime(animeId: String): List<Anime> {
        val animeWithRelationships = getAnimeWithRelationships(animeId) ?: return emptyList()
        val relatedAnimeIds = extractRelatedAnimeIds(animeWithRelationships)

        return if (relatedAnimeIds.isNotEmpty()) {
            try {
                val response = kitsuApiService.getAnimeList(relatedAnimeIds.joinToString(","))
                if (response.isSuccessful) {
                    response.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related anime for animeId: $animeId", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    companion object {
        private const val TAG = "MediaRepository"
    }
}

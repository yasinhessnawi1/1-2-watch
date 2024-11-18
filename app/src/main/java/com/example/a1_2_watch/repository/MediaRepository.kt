package com.example.a1_2_watch.repository

import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.KITSU_URL

/**
 * MediaRepository is responsible for fetching media data such as popular movies, TV shows, and anime.
 * It interacts with the TMDB and Kitsu APIs to retrieve the necessary information.
 */
class MediaRepository {
    // The API key for TMDB API authentication.
    private val apiKey = Constants.API_KEY

    // Instances of ApiService for TMDB and Kitsu APIs.
    private val tmdbApiService = ApiClient.getApiService()
    private val kitsuApiService = ApiClient.getApiService(KITSU_URL)

    /**
     * Fetches a list of popular movies from the TMDB API.
     *
     * @param page The page number for pagination.
     * @return A list of popular movies or an empty list if the request fails.
     */
    suspend fun fetchPopularMovies(page: Int): List<Movie> {
        return try {
            val response = tmdbApiService.getPopularMovies(apiKey, page)
            if (response.isSuccessful) {
                response.body()?.results?.subList(0,15) ?: emptyList() // Limit to 15 movies
            } else {
                Log.e("MediaRepository", "Failed to fetch movies: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchPopularMovies: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches a list of popular TV shows from the TMDB API.
     *
     * @param page The page number for pagination.
     * @return A list of popular TV shows or an empty list if the request fails.
     */
    suspend fun fetchPopularTVShows(page: Int): List<Show> {
        return try {
            val response = tmdbApiService.getPopularTVShows(apiKey, page)
            if (response.isSuccessful) {
                response.body()?.results?.subList(0,15) ?: emptyList() // Limit to 15 TV shows
            } else {
                Log.e("MediaRepository", "Failed to fetch TV shows: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchPopularTVShows: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches a list of popular anime from the Kitsu API.
     *
     * @param page The page number for pagination.
     * @param limit The number of items per page.
     * @return A list of popular anime or an empty list if the request fails.
     */
    suspend fun fetchPopularAnime(page: Int, limit: Int): List<Anime> {
        return try {
            val offset = (page - 1) * limit
            val response = kitsuApiService.getPopularAnimeKitsu(limit, offset)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to fetch anime: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchPopularAnime: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches a list of movies related to a specific movie by its ID from the TMDB API.
     *
     * @param movieId The unique ID of the movie.
     * @return A list of related movies or an empty list if the request fails.
     */
    suspend fun fetchRelatedMovies(movieId: Int): List<Movie> {
        return try {
            val response = tmdbApiService.getRelatedMovies(movieId, apiKey)
            if (response.isSuccessful) {
                Log.d("MediaRepository", "Related movies response: ${response.body()}")
                response.body()?.results ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to fetch related movies: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchRelatedMovies: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches a list of TV shows related to a specific TV show by its ID from the TMDB API.
     *
     * @param tvShowId The unique ID of the TV show.
     * @return A list of related TV shows or an empty list if the request fails.
     */
    suspend fun fetchRelatedTVShows(tvShowId: Int): List<Show> {
        return try {
            val response = tmdbApiService.getRelatedTVShows(tvShowId, apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to fetch related TV shows: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchRelatedTVShows: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches a list of anime by type from the Kitsu API.
     *
     * @param type The subtype of the anime.
     * @return A list of anime matching the subtype or an empty list if the request fails.
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
            Log.e("AnimeRepository", "Error fetching anime with relationships", e)
            null
        }
    }
    fun extractRelatedAnimeIds(animeResponse: AnimeResponseIncluded): List<String> {
        return animeResponse.included
            ?.filter { it.type == "mediaRelationships" }
            ?.mapNotNull { it.relationships?.destination?.data }
            ?.filter { it.type == "anime" }
            ?.map { it.id }
            ?: emptyList()
    }
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
                Log.e("AnimeRepository", "Error fetching related anime", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

}
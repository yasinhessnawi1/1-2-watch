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
                response.body()?.results ?: emptyList()
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
                response.body()?.results ?: emptyList()
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
     * Fetches detailed information about a specific movie by its ID.
     *
     * @param movieId The ID of the movie.
     * @return The movie details or null if the request fails.
     */
    suspend fun fetchMovieDetails(movieId: Int): MovieDetails? {
        return try {
            val response = tmdbApiService.getMovieDetails(movieId, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch movie details: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchMovieDetails: ${e.message}")
            null
        }
    }

    /**
     * Fetches detailed information about a specific TV show by its ID.
     *
     * @param tvShowId The ID of the TV show.
     * @return The TV show details or null if the request fails.
     */
    suspend fun fetchTVShowDetails(tvShowId: Int): ShowDetails? {
        return try {
            val response = tmdbApiService.getTVShowDetails(tvShowId, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch TV show details: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchTVShowDetails: ${e.message}")
            null
        }
    }

    /**
     * Fetches detailed information about a specific anime by its ID.
     *
     * @param animeId The ID of the anime.
     * @return The anime details or null if the request fails.
     */
    suspend fun fetchAnimeDetails(animeId: String): AnimeDetails? {
        return try {
            val response = kitsuApiService.getAnimeDetails(animeId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch anime details: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchAnimeDetails: ${e.message}")
            null
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
    suspend fun fetchAnimeByType(type: String): List<Anime> {
        return try {
            val response = kitsuApiService.searchAnimeByType(type)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to fetch anime by type: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchAnimeByType: ${e.message}")
            emptyList()
        }
    }

    /**
     * Searches for movies on TMDB by a query string.
     *
     * @param query The search query string.
     * @return A list of movies matching the query or an empty list if the request fails.
     */
    suspend fun searchMovies(query: String): List<Movie> {
        return try {
            val response = tmdbApiService.searchMovies(apiKey, query)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to search movies: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in searchMovies: ${e.message}")
            emptyList()
        }
    }

    /**
     * Searches for TV shows on TMDB by a query string.
     *
     * @param query The search query string.
     * @return A list of TV shows matching the query or an empty list if the request fails.
     */
    suspend fun searchTVShows(query: String): List<Show> {
        return try {
            val response = tmdbApiService.searchTVShows(apiKey, query)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to search TV shows: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in searchTVShows: ${e.message}")
            emptyList()
        }
    }

    /**
     * Searches for anime on Kitsu by a query string.
     *
     * @param query The search query string.
     * @return A list of anime matching the query or an empty list if the request fails.
     */
    suspend fun searchAnime(query: String): List<Anime> {
        return try {
            val response = kitsuApiService.searchAnime(query)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to search anime: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in searchAnime: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches watch providers for a specific movie by its ID.
     *
     * @param movieId The ID of the movie.
     * @return The watch providers response or null if the request fails.
     */
    suspend fun fetchMovieWatchProviders(movieId: Int): WatchProvidersResponse? {
        return try {
            val response = tmdbApiService.getWatchProviders(movieId, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch movie watch providers: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchMovieWatchProviders: ${e.message}")
            null
        }
    }

    /**
     * Fetches watch providers for a specific TV show by its ID.
     *
     * @param tvShowId The ID of the TV show.
     * @return The watch providers response or null if the request fails.
     */
    suspend fun fetchTVShowWatchProviders(tvShowId: Int): WatchProvidersResponse? {
        return try {
            val response = tmdbApiService.getTVShowWatchProviders(tvShowId, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch TV show watch providers: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchTVShowWatchProviders: ${e.message}")
            null
        }
    }

    /**
     * Fetches anime streaming links for a specific anime by its ID.
     *
     * @param animeId The ID of the anime.
     * @return A list of anime streaming links or an empty list if the request fails.
     */
    suspend fun fetchAnimeStreamingLinks(animeId: String): List<StreamingLink> {
        return try {
            val response = kitsuApiService.getAnimeStreamingLinks(animeId)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("MediaRepository", "Failed to fetch anime streaming links: ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchAnimeStreamingLinks: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches streamer details by streaming link ID.
     *
     * @param streamingLinkId The ID of the streaming link.
     * @return The streamer details or null if the request fails.
     */
    suspend fun fetchStreamerDetails(streamingLinkId: String): StreamerDetailsResponse? {
        return try {
            val response = kitsuApiService.getStreamerDetails(streamingLinkId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MediaRepository", "Failed to fetch streamer details: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Exception in fetchStreamerDetails: ${e.message}")
            null
        }
    }
}
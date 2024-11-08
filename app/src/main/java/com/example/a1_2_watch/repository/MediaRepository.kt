package com.example.a1_2_watch.repository

import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.AnimeResponse
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.MovieResponse
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.ShowResponse
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.KITSU_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * MediaRepository class responsible for fetching popular media items (movie, TV shows, and anime)
 * by using the TMBD and Kitsu APIs.
 */
class MediaRepository {
    // The API key for TMBD API authentication.
    private val apiKey = Constants.API_KEY

    /**
     * This functions fetches a list of popular movies from the TMBD API.
     *
     * @param page The page number for pagination.
     * @return List<Movie> The list of popular movies or an empty list if the request fails.
     */
    suspend fun fetchPopularMovies(page: Int): List<Movie> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                // Make API call to fetch the popular movies.
                val call = ApiClient.getApiService().getPopularMovies(apiKey, page)
                call.enqueue(object : Callback<MovieResponse> {

                    /**
                     * This function called when the API response is successfully received.
                     *
                     * @param call The API call.
                     * @param response The response from the API, containing movie information.
                     */
                    override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                        // If successful, pass the list of popular movies to the continuation.
                        if (response.isSuccessful) {
                            val movies = response.body()?.results ?: emptyList()
                            continuation.resume(movies)
                        } else {
                            // Log an error message.
                            Log.e("MediaHandler", "Failed to fetch movies: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    /**
                     * This function called when the API call fails.
                     *
                     * @param call The API call.
                     * @param t The throwable indicating the failure reason.
                     */
                    override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                        Log.e("MediaHandler", "Exception in fetchPopularMovies: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
                // Cancel the API call if the coroutine is cancelled.
                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }

    /**
     * This functions fetches a list of popular TV show from the TMBD API.
     *
     * @param page The page number for pagination.
     * @return List<Show> The list of popular TV show or an empty list if the request fails.
     */
    suspend fun fetchPopularTVShows(page: Int): List<Show> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                // Make API call to fetch the TV shows.
                val call = ApiClient.getApiService().getPopularTVShows(apiKey, page)
                call.enqueue(object : Callback<ShowResponse> {

                    /**
                     * This function called when the API response is successfully received.
                     *
                     * @param call The API call.
                     * @param response The response from the API, containing TV show information.
                     */
                    override fun onResponse(call: Call<ShowResponse>, response: Response<ShowResponse>) {
                        // If successful, pass the list of popular TV shows to the continuation.
                        if (response.isSuccessful) {
                            val shows = response.body()?.results ?: emptyList()
                            continuation.resume(shows)
                        } else {
                            // Log an error message.
                            Log.e("MediaHandler", "Failed to fetch TV shows: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    /**
                     * This function called when the API call fails.
                     *
                     * @param call The API call.
                     * @param t The throwable indicating the failure reason.
                     */
                    override fun onFailure(call: Call<ShowResponse>, t: Throwable) {
                        Log.e("MediaHandler", "Exception in fetchPopularTVShows: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
                // Cancel the API call if the coroutine is cancelled.
                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }

    /**
     * This functions fetches a list of popular anime from the Kitsu API.
     *
     * @param page The page number for pagination.
     * @return List<Anime> The list of popular Anime or an empty list if the request fails.
     */
    suspend fun fetchPopularAnime(page: Int, limit: Int): List<Anime> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                // Make API call to fetch the anime.
                val call = ApiClient.getApiService(KITSU_URL).getPopularAnimeKitsu(limit, page * limit)
                call.enqueue(object : Callback<AnimeResponse> {

                    /**
                     * This function called when the API response is successfully received.
                     *
                     * @param call The API call.
                     * @param response The response from the API, containing anime information.
                     */
                    override fun onResponse(call: Call<AnimeResponse>, response: Response<AnimeResponse>) {
                        // If successful, pass the list of popular anime to the continuation.
                        if (response.isSuccessful) {
                            val animeList = response.body()?.data ?: emptyList()
                            continuation.resume(animeList)
                        } else {
                            // Log an error message.
                            Log.e("MediaHandler", "Failed to fetch anime: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    /**
                     * This function called when the API call fails.
                     *
                     * @param call The API call.
                     * @param t The throwable indicating the failure reason.
                     */
                    override fun onFailure(call: Call<AnimeResponse>, t: Throwable) {
                        Log.e("MediaHandler", "Exception in fetchPopularAnime: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
                // Cancel the API call if the coroutine is cancelled
                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }

    /**
     * This function fetches a list of movies related to a specific movie by its ID from the TMDB API.
     *
     * @param movieId The unique ID of the movie.
     * @return List<Movie> A list of related movies or an empty list if the request fails.
     */
    suspend fun fetchRelatedMovies(movieId: Int): List<Movie> {
        return withContext(Dispatchers.IO) {
            // Make the API call to fetch related movies by movie ID.
            val call = ApiClient.getApiService().getRelatedMovies(movieId, apiKey)
            // Execute the call
            val response = call.execute()
            // Return the list of related movies or an empty list if the request fails.
            response.body()?.results ?: emptyList()
        }
    }

    /**
     * This function fetches a list of TV shows related to a specific TV show by its ID from the TMDB API.
     *
     * @param tvShowId The unique ID of the TV show.
     * @return List<Show> A list of related TV shows or an empty list if the request fails.
     */
    suspend fun fetchRelatedTVShows(tvShowId: Int): List<Show> {
        return withContext(Dispatchers.IO) {
            // Make the API call to fetch related TV shows by TV show ID.
            val call = ApiClient.getApiService().getRelatedTVShows(tvShowId, apiKey)
            // Execute the call
            val response = call.execute()
            // Return the list of related TV shows or an empty list if the request fails.
            response.body()?.results ?: emptyList()
        }
    }

    /**
     * This function fetches a list of anime of a specified type from the Kitsu API.
     *
     * @param type The type of anime to filter results by.
     * @return List<Anime> A list of anime of the specified type, or an empty list if the request fails.
     */
    suspend fun fetchAnimeByType(type: String): List<Anime> {
        return withContext(Dispatchers.IO) {
            // Make the API call to fetch related anime based on the specified type.
            val call = ApiClient.getApiService(KITSU_URL).searchAnimeByType(type)
            // Execute the call
            val response = call.execute()
            // Return the list of related anime or an empty list if the request fails.
            response.body()?.data ?: emptyList()
        }
    }
}

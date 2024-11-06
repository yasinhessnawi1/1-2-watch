package com.example.a1_2_watch.repository

import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.AnimeResponse
import com.example.a1_2_watch.models.MovieResponse
import com.example.a1_2_watch.models.ShowResponse
import com.example.a1_2_watch.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * DiscoverRepository class for performing media searches using the TMDB API for movie and TV shows,
 * and Kitsu API for anime.
 */
class DiscoverRepository {
    // TMDB API for movies and TV shows searches.
    private val tmdbApiService = ApiClient.getApiService(Constants.TMDB_URL)
    // Kitsu API for anime searches.
    private val kitsuApiService = ApiClient.getApiService(Constants.KITSU_URL)

    /**
     * This function searches for movies by using the TMDB API.
     *
     * @param apiKey The API key for TMDB authentication.
     * @param query The search query for movies titles.
     * @param onResult Callback function with the search results, or null otherwise.
     */
    fun searchMovies(apiKey: String, query: String, onResult: (MovieResponse?) -> Unit) {
        // Make the API call to search for movie titles.
        tmdbApiService.searchMovies(apiKey, query).enqueue(object : Callback<MovieResponse> {
            /**
             * This function called when the API response is successfully received.
             *
             * @param call The API call.
             * @param response The response from the API, containing the movie search results.
             */
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                // Pass the response body to onResult if successful.
                onResult(response.body())
            }

            /**
             * This function called if the API call failed.
             *
             * @param call The API call.
             * @param t The throwable indicating the failure reason.
             */
            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                // Log an error message and pass null to indicate failure.
                Log.e("DiscoverHandler", "Movie search failed", t)
                onResult(null)
            }
        })
    }

    /**
     * This function searches for TV shows by using the TMDB API.
     *
     * @param apiKey The API key for TMDB authentication.
     * @param query The search query for TV shows titles.
     * @param onResult Callback function with the search results, or null otherwise.
     */
    fun searchTVShows(apiKey: String, query: String, onResult: (ShowResponse?) -> Unit) {
        // Make the API call to search for TV shows titles.
        tmdbApiService.searchTVShows(apiKey, query).enqueue(object : Callback<ShowResponse> {
            /**
             * This function called when the API response is successfully received.
             *
             * @param call The API call.
             * @param response The response from the API, containing the TV show search results.
             */
            override fun onResponse(call: Call<ShowResponse>, response: Response<ShowResponse>) {
                // Pass the response body to onResult if successful.
                onResult(response.body())
            }

            /**
             * This function called if the API call failed.
             *
             * @param call The API call.
             * @param t The throwable indicating the failure reason.
             */
            override fun onFailure(call: Call<ShowResponse>, t: Throwable) {
                // Log an error message and pass null to indicate failure.
                Log.e("DiscoverHandler", "TV show search failed", t)
                onResult(null)
            }
        })
    }

    /**
     * This function searches for anime by using the Kitsu API.
     *
     * @param query The search query for anime titles.
     * @param onResult Callback function with the search results, or null otherwise.
     */
    fun searchAnime(query: String, onResult: (AnimeResponse?) -> Unit) {
        // Make the API call to search for anime titles.
        kitsuApiService.searchAnime(query).enqueue(object : Callback<AnimeResponse> {
            /**
             * This function called when the API response is successfully received.
             *
             * @param call The API call.
             * @param response The response from the API, containing the anime search results.
             */
            override fun onResponse(call: Call<AnimeResponse>, response: Response<AnimeResponse>) {
                // Pass the response body to onResult if successful.
                onResult(response.body())
            }

            /**
             * This function called if the API call failed.
             *
             * @param call The API call.
             * @param t The throwable indicating the failure reason.
             */
            override fun onFailure(call: Call<AnimeResponse>, t: Throwable) {
                // Log an error message and pass null to indicate failure.
                Log.e("DiscoverHandler", "Anime search failed", t)
                onResult(null)
            }
        })
    }
}

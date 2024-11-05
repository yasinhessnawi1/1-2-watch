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

class DiscoverRepository {

    // TMDB API for movies and TV shows
    private val tmdbApiService = ApiClient.getApiService(Constants.TMDB_URL)

    // Kitsu API for anime
    private val kitsuApiService = ApiClient.getApiService(Constants.KITSU_URL)

    fun searchMovies(apiKey: String, query: String, onResult: (MovieResponse?) -> Unit) {
        tmdbApiService.searchMovies(apiKey, query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Log.e("DiscoverHandler", "Movie search failed", t)
                onResult(null)
            }
        })
    }

    fun searchTVShows(apiKey: String, query: String, onResult: (ShowResponse?) -> Unit) {
        tmdbApiService.searchTVShows(apiKey, query).enqueue(object : Callback<ShowResponse> {
            override fun onResponse(call: Call<ShowResponse>, response: Response<ShowResponse>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<ShowResponse>, t: Throwable) {
                Log.e("DiscoverHandler", "TV show search failed", t)
                onResult(null)
            }
        })
    }

    fun searchAnime(query: String, onResult: (AnimeResponse?) -> Unit) {
        kitsuApiService.searchAnime(query).enqueue(object : Callback<AnimeResponse> {
            override fun onResponse(call: Call<AnimeResponse>, response: Response<AnimeResponse>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<AnimeResponse>, t: Throwable) {
                Log.e("DiscoverHandler", "Anime search failed", t)
                onResult(null)
            }
        })
    }
}

package com.example.a1_2_watch.repository

import android.content.Context
import android.util.Log
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.moduls.MovieResponse
import com.example.a1_2_watch.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MediaHandler {
    private val apiKey = Constants.API_KEY

    fun fetchPopularMovies(page: Int, adapter: MediaAdapter, context: Context, onComplete: () -> Unit) {
        ApiClient.apiService.getPopularMovies(apiKey, page)
            .enqueue(object : Callback<MovieResponse> {
                override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                    onComplete()
                    if (response.isSuccessful) {
                        val movies = response.body()?.results ?: emptyList()
                        adapter.addMovies(movies)
                    }
                }

                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    onComplete()
                    Log.e("MediaHandler", "Failed to load movies", t)
                }
            })
    }

    fun fetchPopularTVShows(page: Int, adapter: MediaAdapter, context: Context, onComplete: () -> Unit) {
        ApiClient.apiService.getPopularTVShows(apiKey, page)
            .enqueue(object : Callback<MovieResponse> {
                override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                    onComplete()
                    if (response.isSuccessful) {
                        val tvShows = response.body()?.results ?: emptyList()
                        adapter.addMovies(tvShows)
                    }
                }

                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    onComplete()
                    Log.e("MediaHandler", "Failed to load TV shows", t)
                }
            })
    }

    fun fetchPopularAnime(page: Int, genreId: Int, adapter: MediaAdapter, context: Context, onComplete: () -> Unit) {
        ApiClient.apiService.getPopularAnime(apiKey, genreId, page)
            .enqueue(object : Callback<MovieResponse> {
                override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                    onComplete()
                    if (response.isSuccessful) {
                        val anime = response.body()?.results ?: emptyList()
                        adapter.addMovies(anime)
                    }
                }

                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    onComplete()
                    Log.e("MediaHandler", "Failed to load anime", t)
                }
            })
    }
}
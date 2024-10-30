package com.example.a1_2_watch.repository

import android.content.Context
import android.util.Log
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.KITSU_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MediaHandler {
    private val apiKey = Constants.API_KEY

    fun <T, R> fetchMedia(
        call: Call<R>,
        adapter: MediaAdapter<T>,
        context: Context,
        onComplete: () -> Unit,
        mapResponse: (R) -> List<T>
    ) {
        call.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                onComplete()
                if (response.isSuccessful) {
                    val mediaItems = response.body()?.let { mapResponse(it) } ?: emptyList()
                    adapter.addMediaItems(mediaItems)
                }
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                onComplete()
                Log.e("MediaHandler", "Failed to load media", t)
            }
        })
    }

    fun fetchPopularMovies(page: Int, adapter: MediaAdapter<Movie>, context: Context, onComplete: () -> Unit) {
        val call = ApiClient.getApiService().getPopularMovies(apiKey, page)
        fetchMedia(call, adapter, context, onComplete) { it.results }
    }

    fun fetchPopularTVShows(page: Int, adapter: MediaAdapter<Show>, context: Context, onComplete: () -> Unit) {
        val call = ApiClient.getApiService().getPopularTVShows(apiKey, page)
        fetchMedia(call, adapter, context, onComplete) { it.results }
    }

    fun fetchPopularAnime(page: Int, limit: Int, adapter: MediaAdapter<Anime>, context: Context, onComplete: () -> Unit) {
        val call = ApiClient.getApiService(KITSU_URL).getPopularAnimeKitsu(limit, page * limit)
        fetchMedia(call, adapter, context, onComplete) { it.data }
    }
}

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

class MediaHandler {
    private val apiKey = Constants.API_KEY

    suspend fun fetchPopularMovies(page: Int): List<Movie> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val call = ApiClient.getApiService().getPopularMovies(apiKey, page)
                call.enqueue(object : Callback<MovieResponse> {
                    override fun onResponse(
                        call: Call<MovieResponse>,
                        response: Response<MovieResponse>
                    ) {
                        if (response.isSuccessful) {
                            val movies = response.body()?.results ?: emptyList()
                            continuation.resume(movies)
                        } else {
                            Log.e("MediaHandler", "Failed to fetch movies: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    override fun onFailure(
                        call: Call<MovieResponse>,
                        t: Throwable
                    ) {
                        Log.e("MediaHandler", "Exception in fetchPopularMovies: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })

                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }

    suspend fun fetchPopularTVShows(page: Int): List<Show> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val call = ApiClient.getApiService().getPopularTVShows(apiKey, page)
                call.enqueue(object : Callback<ShowResponse> {
                    override fun onResponse(
                        call: Call<ShowResponse>,
                        response: Response<ShowResponse>
                    ) {
                        if (response.isSuccessful) {
                            val shows = response.body()?.results ?: emptyList()
                            continuation.resume(shows)
                        } else {
                            Log.e("MediaHandler", "Failed to fetch TV shows: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    override fun onFailure(
                        call: Call<ShowResponse>,
                        t: Throwable
                    ) {
                        Log.e("MediaHandler", "Exception in fetchPopularTVShows: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })

                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }

    suspend fun fetchPopularAnime(page: Int, limit: Int): List<Anime> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val call =
                    ApiClient.getApiService(KITSU_URL).getPopularAnimeKitsu(limit, page * limit)
                call.enqueue(object : Callback<AnimeResponse> {
                    override fun onResponse(
                        call: Call<AnimeResponse>,
                        response: Response<AnimeResponse>
                    ) {
                        if (response.isSuccessful) {
                            val animeList = response.body()?.data ?: emptyList()
                            continuation.resume(animeList)
                        } else {
                            Log.e("MediaHandler", "Failed to fetch anime: ${response.message()}")
                            continuation.resume(emptyList())
                        }
                    }

                    override fun onFailure(
                        call: Call<AnimeResponse>,
                        t: Throwable
                    ) {
                        Log.e("MediaHandler", "Exception in fetchPopularAnime: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })

                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }
        }
    }
}

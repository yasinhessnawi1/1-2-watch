// FetchRelatedItemsWorker.kt
package com.example.a1_2_watch.workers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.MediaRepository
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchRelatedItemsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val gson = Gson()
    private val mediaRepository = MediaRepository()
    private val likeButtonUtils = LikeButtonUtils(context)
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val likedMovies = likeButtonUtils.getLikedMovies()
            val likedShows = likeButtonUtils.getLikedShows()
            val likedAnime = likeButtonUtils.getLikedAnime()

            val relatedMovies = fetchRelatedMovies(likedMovies)
            val relatedShows = fetchRelatedTVShows(likedShows)
            val relatedAnime = fetchRelatedAnime(likedAnime)

            // Store the related items in SharedPreferences
            val sharedPreferences = applicationContext.getSharedPreferences("related_items", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("related_movies", gson.toJson(relatedMovies))
                .putString("related_shows", gson.toJson(relatedShows))
                .putString("related_anime", gson.toJson(relatedAnime))
                .apply()

            Result.success()
        } catch (e: Exception) {
            Log.e("FetchRelatedItemsWorker", "Error fetching related items: ${e.message} ${e.stackTraceToString()}")
            Result.retry()
        }
    }


    private suspend fun fetchRelatedMovies(likedMovies: List<Movie>): List<Movie> {
        val relatedMovies = mutableListOf<Movie>()
        for (movie in likedMovies) {
            val related = mediaRepository.fetchRelatedMovies(movie.id)
            relatedMovies.addAll(related.take(10))
        }
        return relatedMovies
    }

    private suspend fun fetchRelatedTVShows(likedShows: List<Show>): List<Show> {
        val relatedShows = mutableListOf<Show>()
        for (show in likedShows) {
            val related = mediaRepository.fetchRelatedTVShows(show.id)
            relatedShows.addAll(related.take(10))
        }
        return relatedShows
    }

    private suspend fun fetchRelatedAnime(likedAnime: List<Anime>): List<Anime> {
        val animeTypes = likedAnime.map { it.attributes.subtype }.distinct()
        val relatedAnime = mutableListOf<Anime>()
        for (type in animeTypes) {
            val related = mediaRepository.fetchAnimeByType(type)
            relatedAnime.addAll(related.take(10))
        }
        return relatedAnime
    }
}

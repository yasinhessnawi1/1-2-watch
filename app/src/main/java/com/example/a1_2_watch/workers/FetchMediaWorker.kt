package com.example.a1_2_watch.workers

import android.content.Context
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.MediaRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchMediaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        const val KEY_PAGE = "KEY_PAGE"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val mediaRepository = MediaRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val page = inputData.getInt(KEY_PAGE, 1)

            val resultJson: String = when (mediaType) {
                MediaType.MOVIES -> {
                    val movies = mediaRepository.fetchPopularMovies(page)
                    gson.toJson(movies)
                }
                MediaType.TV_SHOWS -> {
                    val shows = mediaRepository.fetchPopularTVShows(page)
                    gson.toJson(shows)
                }
                MediaType.ANIME -> {
                    val animeList = mediaRepository.fetchPopularAnime(page, limit = 10)
                    gson.toJson(animeList)
                }
            }

            val output = workDataOf(KEY_RESULT to resultJson)
            Result.success(output)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
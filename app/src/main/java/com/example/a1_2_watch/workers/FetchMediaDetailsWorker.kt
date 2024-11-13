package com.example.a1_2_watch.workers

import android.content.Context
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.DetailsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchDetailsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val detailsRepository = DetailsRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)
            if (mediaId == -1) return@withContext Result.failure()

            val resultJson: String = when (mediaType) {
                MediaType.MOVIES -> {
                    val details = detailsRepository.fetchMovieDetails(mediaId)
                    gson.toJson(details)
                }
                MediaType.TV_SHOWS -> {
                    val details = detailsRepository.fetchTVShowDetails(mediaId)
                    gson.toJson(details)
                }
                MediaType.ANIME -> {
                    val details = detailsRepository.fetchAnimeDetails(mediaId)
                    gson.toJson(details)
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
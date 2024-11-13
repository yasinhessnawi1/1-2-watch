package com.example.a1_2_watch.workers


import android.content.Context
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.MediaRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchRelatedMediaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val mediaRepository = MediaRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)
            if (mediaId == -1) return@withContext Result.failure()

            val resultJson: String = when (mediaType) {
                MediaType.MOVIES -> {
                    val relatedMovies = mediaRepository.fetchRelatedMovies(mediaId)
                    gson.toJson(relatedMovies)
                }
                MediaType.TV_SHOWS -> {
                    val relatedShows = mediaRepository.fetchRelatedTVShows(mediaId)
                    gson.toJson(relatedShows)
                }
                MediaType.ANIME -> {
                    // For anime, you might fetch by type or other criteria
                    gson.toJson(emptyList<Anime>())
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
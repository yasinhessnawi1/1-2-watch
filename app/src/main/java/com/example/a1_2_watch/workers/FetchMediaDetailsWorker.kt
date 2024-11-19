package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.repository.DetailsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for fetching detailed information about a media item.
 * Supports fetching details for Movies, TV Shows, and Anime using a background thread with WorkManager.
 *
 * @param context The context of the application.
 * @param params The parameters for the worker, including input data and runtime constraints.
 */
class FetchDetailsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // Keys for input and output data for WorkManager.
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE" // Input key for the media type.
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID" // Input key for the media ID.
        const val KEY_RESULT = "KEY_RESULT" // Output key for the fetched details in JSON format.
        private const val TAG = "FetchDetailsWorker"
    }

    // Gson instance for JSON serialization/deserialization.
    private val gson = Gson()

    // Repository responsible for fetching media details from APIs.
    private val detailsRepository = DetailsRepository()

    /**
     * Executes the work to fetch media details.
     * This method is called in the background thread by WorkManager.
     *
     * @return [Result.success] with the fetched details if successful,
     *         [Result.failure] if input data is invalid, or [Result.retry] if an exception occurs.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve input data.
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)

            // Validate the input data.
            if (mediaId == -1) {
                Log.e(TAG, "Invalid media ID: $mediaId")
                return@withContext Result.failure()
            }

            // Fetch details based on media type and serialize to JSON.
            val resultJson: String = when (mediaType) {
                MediaType.MOVIES -> {
                    val details = detailsRepository.fetchMovieDetails(mediaId)
                    gson.toJson(details) // Convert movie details to JSON.
                }

                MediaType.TV_SHOWS -> {
                    val details = detailsRepository.fetchTVShowDetails(mediaId)
                    gson.toJson(details) // Convert TV show details to JSON.
                }

                MediaType.ANIME -> {
                    val details = detailsRepository.fetchAnimeDetails(mediaId)
                    gson.toJson(details) // Convert anime details to JSON.
                }
            }

            // Prepare the output data with the serialized JSON result.
            val output = workDataOf(KEY_RESULT to resultJson)

            // Return a successful result with the output data.
            Result.success(output)
        } catch (e: Exception) {
            // Log the error and return a retry result in case of an exception.
            Log.e(TAG, "Error fetching media details", e)
            Result.retry()
        }
    }
}

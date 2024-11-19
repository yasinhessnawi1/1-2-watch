package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.repository.MediaRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for fetching a list of media items (Movies, TV Shows, or Anime) using WorkManager.
 * This worker operates in a background thread and fetches data based on the provided media type and page number.
 *
 * @param context The application context.
 * @param params Parameters for the worker, including input data and runtime constraints.
 */
class FetchPopularMediaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // Keys for input and output data for WorkManager.
        const val KEY_MEDIA_TYPE =
            "KEY_MEDIA_TYPE" // Input key for the type of media to fetch (e.g., Movies, TV Shows, Anime).
        const val KEY_PAGE = "KEY_PAGE" // Input key for the page number to fetch for pagination.
        const val KEY_RESULT =
            "KEY_RESULT" // Output key for the fetched list of media items in JSON format.
        private const val TAG = "FetchPopularMediaWorker"
    }

    // Gson instance for serializing and deserializing JSON data.
    private val gson = Gson()

    // Repository responsible for fetching media data from APIs.
    private val mediaRepository = MediaRepository()

    /**
     * Executes the work to fetch media items based on the provided input data.
     * This method is called in a background thread by WorkManager.
     *
     * @return [Result.success] containing the fetched media list in JSON format if successful,
     *         [Result.failure] if the input data is invalid, or [Result.retry] if an exception occurs.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve input data.
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val page = inputData.getInt(KEY_PAGE, 1)

            // Fetch the media list based on the media type and serialize to JSON.
            val resultJson: String = when (mediaType) {
                MediaType.MOVIES -> {
                    val movies = mediaRepository.fetchPopularMovies(page)
                    gson.toJson(movies) // Convert the list of movies to JSON.
                }

                MediaType.TV_SHOWS -> {
                    val shows = mediaRepository.fetchPopularTVShows(page)
                    gson.toJson(shows) // Convert the list of TV shows to JSON.
                }

                MediaType.ANIME -> {
                    val animeList = mediaRepository.fetchPopularAnime(page, limit = 10)
                    gson.toJson(animeList) // Convert the list of anime to JSON.
                }
            }

            // Prepare the output data with the serialized JSON result.
            val output = workDataOf(KEY_RESULT to resultJson)

            // Return a successful result with the output data.
            Result.success(output)
        } catch (e: Exception) {
            // Log the exception and return a retry result.
            Log.e(TAG, "Error fetching media list", e)
            Result.retry()
        }
    }
}

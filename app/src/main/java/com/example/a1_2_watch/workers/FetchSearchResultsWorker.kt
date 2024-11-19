package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.a1_2_watch.models.MinimizedItem
import com.example.a1_2_watch.repository.DiscoverRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for fetching search results for Movies, TV Shows, and Anime based on a user query.
 * Optimized to return only essential fields (e.g., title, poster_path).
 *
 * @param context The application context.
 * @param params Worker parameters, including input data and runtime constraints.
 */
class FetchSearchResultsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // Keys for input and output data for WorkManager.
        const val KEY_QUERY = "KEY_QUERY" // Input key for the search query.
        const val KEY_RESULT = "KEY_RESULT" // Output key for the search results in JSON format.
        private const val TAG = "FetchSearchResultsWorker"
    }

    // Gson instance for serializing and deserializing data.
    private val gson = Gson()

    // Repository responsible for performing search operations.
    private val searchRepository = DiscoverRepository()

    /**
     * Executes the work to fetch search results based on the provided query.
     * Optimized to return only essential fields (e.g., title, poster_path).
     *
     * @return [Result.success] with the fetched search results if successful,
     *         [Result.failure] if the query is invalid, or [Result.retry] in case of exceptions.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve the search query from input data.
            val query = inputData.getString(KEY_QUERY)
            if (query.isNullOrEmpty()) {
                Log.e(TAG, "Invalid search query: $query")
                return@withContext Result.failure() // Fail the work if the query is empty.
            }

            val minimizedResults = mutableListOf<MinimizedItem>() // Aggregated minimized results.

            // Perform movie search.
            val movieResponse = searchRepository.searchMovies(query)
            movieResponse?.results?.let { movies ->
                movies.forEach { movie ->
                    minimizedResults.add(
                        MinimizedItem(
                            id = movie.id,
                            title = movie.title.toString(),
                            posterPath = movie.poster_path,
                            type = "MOVIES"
                        )
                    )
                }
            }

            // Perform TV show search.
            val showResponse = searchRepository.searchTVShows(query)
            showResponse?.results?.let { shows ->
                shows.forEach { show ->
                    minimizedResults.add(
                        MinimizedItem(
                            id = show.id,
                            title = show.name.toString(),
                            posterPath = show.poster_path,
                            type = "TV_SHOWS"
                        )
                    )
                }
            }

            // Perform anime search.
            val animeResponse = searchRepository.searchAnime(query)
            animeResponse?.data?.let { animes ->
                animes.forEach { anime ->
                    minimizedResults.add(
                        MinimizedItem(
                            id = anime.id,
                            title = anime.attributes?.canonicalTitle ?: "title not available",
                            posterPath = anime.attributes?.posterImage?.original,
                            type = "ANIME"
                        )
                    )
                }
            }

            Log.d(TAG, "Fetched search results: ${minimizedResults.size} items found for query: $query")

            // Convert the aggregated minimized results to JSON.
            val resultJson = gson.toJson(minimizedResults)

            // Prepare the output data with the results in JSON format.
            val outputData = workDataOf(KEY_RESULT to resultJson)

            // Return success with the output data.
            Result.success(outputData)
        } catch (e: Exception) {
            // Log the exception and retry the work.
            Log.e(TAG, "Error fetching search results", e)
            Result.retry()
        }
    }
}

// FetchSearchResultsWorker.kt
package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.DiscoverRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchSearchResultsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_QUERY = "KEY_QUERY"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val searchRepository = DiscoverRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val query = inputData.getString(KEY_QUERY)
            if (query.isNullOrEmpty()) {
                return@withContext Result.failure()
            }

            val searchResults = mutableListOf<Any>()

            // Perform movie search
            val movieResponse = searchRepository.searchMovies(query)
            movieResponse?.results?.let { movies ->
                if (movies.size > 5) {
                    searchResults.addAll(movies.subList(0, 5))
                } else
                    searchResults.addAll(movies)

            }

            // Perform TV show search
            val showResponse = searchRepository.searchTVShows(query)
            showResponse?.results?.let { shows ->
                if (shows.size > 5){
                    searchResults.addAll(shows.subList(0, 5))
                }
                else
                searchResults.addAll(shows)
            }

            // Perform anime search
            val animeResponse = searchRepository.searchAnime(query)
            animeResponse?.data?.let { animes ->
                if (animes.size > 2) {
                    searchResults.addAll(animes.subList(0, 2))
                } else
                searchResults.addAll(animes)
            }

            val resultJson = gson.toJson(searchResults)
            println("Search results: $resultJson")
            val outputData = workDataOf(KEY_RESULT to resultJson)
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e("FetchSearchResultsWorker", "Error fetching search results: ${e.message}")
            Result.retry()
        }
    }
}

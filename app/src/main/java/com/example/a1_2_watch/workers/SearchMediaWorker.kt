package com.example.a1_2_watch.workers

import android.content.Context
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.DiscoverRepository
import com.example.a1_2_watch.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchMediaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_QUERY = "KEY_QUERY"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val discoverRepository = DiscoverRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val query = inputData.getString(KEY_QUERY) ?: return@withContext Result.failure()
            val searchResults = mutableListOf<Any>()

            val movieResponse = discoverRepository.searchMovies(query)
            movieResponse?.results?.let { searchResults.addAll(it) }

            val showResponse = discoverRepository.searchTVShows(query)
            showResponse?.results?.let { searchResults.addAll(it) }

            val animeResponse = discoverRepository.searchAnime(query)
            animeResponse?.data?.let { searchResults.addAll(it) }

            val resultJson = gson.toJson(searchResults)
            val output = workDataOf(KEY_RESULT to resultJson)
            Result.success(output)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
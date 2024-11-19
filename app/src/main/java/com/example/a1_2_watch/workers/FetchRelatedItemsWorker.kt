package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.repository.MediaRepository
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for fetching related media items (Movies, TV Shows, Anime) based on liked items.
 * Stores the results in SharedPreferences for further use.
 *
 * @param context The application context.
 * @param params Worker parameters, including input data and runtime constraints.
 */
class FetchRelatedItemsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FetchRelatedItemsWorker"
    }

    // Gson instance for serializing and deserializing data.
    private val gson = Gson()

    // Repository for fetching media data.
    private val mediaRepository = MediaRepository()

    // Utility for retrieving liked media items.
    private val likeButtonUtils = LikeButtonUtils(context)

    /**
     * Executes the work to fetch related items for liked Movies, TV Shows, and Anime.
     * Results are stored in SharedPreferences.
     *
     * @return [Result.success] if the related items are successfully fetched and stored,
     *         [Result.retry] in case of exceptions.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve liked media items.
            val likedMovies = likeButtonUtils.getLikedMovies()
            val likedShows = likeButtonUtils.getLikedShows()
            val likedAnime = likeButtonUtils.getLikedAnime()

            Log.d(TAG, "Fetched liked movies: ${likedMovies.size}, shows: ${likedShows.size}, anime: ${likedAnime.size}")

            // Fetch related items for each media type.
            val relatedMovies = fetchRelatedMovies(likedMovies)
            val relatedShows = fetchRelatedTVShows(likedShows)
            val relatedAnime = fetchRelatedAnime(likedAnime)

            Log.d(TAG, "Fetched related movies: ${relatedMovies.size}, shows: ${relatedShows.size}, anime: ${relatedAnime.size}")

            // Store the fetched related items in SharedPreferences.
            val sharedPreferences =
                applicationContext.getSharedPreferences("related_items", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("related_movies", gson.toJson(relatedMovies))
                .putString("related_shows", gson.toJson(relatedShows))
                .putString("related_anime", gson.toJson(relatedAnime))
                .apply()

            Result.success()
        } catch (e: Exception) {
            // Log the exception and retry the work.
            Log.e(TAG, "Error fetching related items", e)
            Result.retry()
        }
    }

    /**
     * Fetches related movies based on the provided list of liked movies.
     *
     * @param likedMovies A list of liked [Movie] objects.
     * @return A list of related [Movie] objects.
     */
    private suspend fun fetchRelatedMovies(likedMovies: List<Movie>): List<Movie> {
        val relatedMovies = mutableListOf<Movie>()
        for (movie in likedMovies) {
            try {
                // Fetch related movies for each liked movie.
                val related = mediaRepository.fetchRelatedMovies(movie.id)
                // Limit the number of related items to 10.
                relatedMovies.addAll(related.take(10))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related movies for movieId: ${movie.id}", e)
            }
        }
        return relatedMovies
    }

    /**
     * Fetches related TV shows based on the provided list of liked shows.
     *
     * @param likedShows A list of liked [Show] objects.
     * @return A list of related [Show] objects.
     */
    private suspend fun fetchRelatedTVShows(likedShows: List<Show>): List<Show> {
        val relatedShows = mutableListOf<Show>()
        for (show in likedShows) {
            try {
                // Fetch related TV shows for each liked show.
                val related = mediaRepository.fetchRelatedTVShows(show.id)
                // Limit the number of related items to 10.
                relatedShows.addAll(related.take(10))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related TV shows for showId: ${show.id}", e)
            }
        }
        return relatedShows
    }

    /**
     * Fetches related anime based on the provided list of liked anime.
     *
     * @param likedAnime A list of liked [Anime] objects.
     * @return A list of related [Anime] objects.
     */
    private suspend fun fetchRelatedAnime(likedAnime: List<Anime>): List<Anime> {
        val animeIds = likedAnime.map { it.id } // Extract IDs from liked anime.
        val relatedAnime = mutableListOf<Anime>()
        for (id in animeIds) {
            try {
                // Fetch related anime for each liked anime ID.
                val related = mediaRepository.getRelatedAnime(id.toString())
                // Limit the number of related items to 10.
                relatedAnime.addAll(related.take(10))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related anime for animeId: $id", e)
            }
        }
        return relatedAnime
    }
}

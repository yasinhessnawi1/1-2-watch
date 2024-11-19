package com.example.a1_2_watch.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.a1_2_watch.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.a1_2_watch.R

/**
 * Utility class for handling like button interactions and liked items.
 *
 * @property context The application context.
 */
class LikeButtonUtils(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Toggles the like status of a media item and updates SharedPreferences.
     *
     * @param item The media item to toggle the like status for.
     */
    suspend fun toggleLikeToItem(item: Any) = withContext(Dispatchers.IO) {
        try {
            val key = when (item) {
                is Movie -> "liked_movies"
                is MovieDetails -> "liked_movies"
                is Show -> "liked_shows"
                is ShowDetails -> "liked_shows"
                is Anime -> "liked_anime"
                is AnimeDetails -> "liked_anime"
                else -> return@withContext
            }

            val minimalItem = when (item) {
                is Movie -> item
                is MovieDetails -> item.toMinimalMovie()
                is Show -> item
                is ShowDetails -> item.toMinimalShow()
                is Anime -> item
                is AnimeDetails -> item.toMinimalAnime()
                else -> return@withContext
            }

            when (minimalItem) {
                is Movie -> toggleLike(minimalItem, key, Movie::class.java)
                is Show -> toggleLike(minimalItem, key, Show::class.java)
                is Anime -> toggleLike(minimalItem, key, Anime::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for item: $item", e)
        }
    }

    /**
     * Toggles the like status for a given media item in SharedPreferences.
     *
     * @param item The media item to toggle.
     * @param key The SharedPreferences key for the media type.
     * @param type The class type of the media item.
     */
    private fun <T> toggleLike(item: T, key: String, type: Class<T>) {
        try {
            val likedItemsList = getLikedItems(key, type)

            // Check if the item is already liked and toggle its status.
            val existingItemIndex = likedItemsList.indexOfFirst {
                when (type) {
                    Movie::class.java -> (it as Movie).id == (item as Movie).id
                    Show::class.java -> (it as Show).id == (item as Show).id
                    Anime::class.java -> (it as Anime).id == (item as Anime).id
                    else -> false
                }
            }

            if (existingItemIndex != -1) {
                likedItemsList.removeAt(existingItemIndex) // Remove if already liked.
            } else {
                likedItemsList.add(0, item) // Add to the list if not liked.
            }

            saveLikedItems(key, likedItemsList)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like status for item: $item", e)
        }
    }

    /**
     * Retrieves liked items for a specific key from SharedPreferences.
     *
     * @param key The SharedPreferences key.
     * @param type The class type of the media item.
     * @return A mutable list of liked items.
     */
    private fun <T> getLikedItems(key: String, type: Class<T>): MutableList<T> {
        return try {
            val likedItemsJson = sharedPreferences.getString(key, null)
            if (likedItemsJson != null) {
                gson.fromJson(
                    likedItemsJson,
                    TypeToken.getParameterized(MutableList::class.java, type).type
                )
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving liked items for key: $key", e)
            mutableListOf()
        }
    }

    /**
     * Saves liked items to SharedPreferences.
     *
     * @param key The SharedPreferences key.
     * @param items The list of items to save.
     */
    private fun <T> saveLikedItems(key: String, items: List<T>) {
        try {
            val updatedJson = gson.toJson(items)
            sharedPreferences.edit().putString(key, updatedJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving liked items for key: $key", e)
        }
    }

    /**
     * Retrieves liked movies.
     *
     * @return A list of liked [Movie] objects.
     */
    suspend fun getLikedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            getLikedItems("liked_movies", Movie::class.java).onEach { it.isLiked = true }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving liked movies", e)
            emptyList()
        }
    }

    /**
     * Retrieves liked TV shows.
     *
     * @return A list of liked [Show] objects.
     */
    suspend fun getLikedShows(): List<Show> = withContext(Dispatchers.IO) {
        try {
            getLikedItems("liked_shows", Show::class.java).onEach { it.isLiked = true }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving liked shows", e)
            emptyList()
        }
    }

    /**
     * Retrieves liked anime.
     *
     * @return A list of liked [Anime] objects.
     */
    suspend fun getLikedAnime(): List<Anime> = withContext(Dispatchers.IO) {
        try {
            getLikedItems("liked_anime", Anime::class.java).onEach { it.isLiked = true }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving liked anime", e)
            emptyList()
        }
    }

    /**
     * Checks if a media item is liked.
     *
     * @param item The media item to check.
     * @return `true` if the item is liked, otherwise `false`.
     */
    suspend fun isItemLiked(item: Any): Boolean = withContext(Dispatchers.IO) {
        try {
            when (item) {
                is Movie -> getLikedMovies().any { it.id == item.id }
                is MovieDetails -> getLikedMovies().any { it.id == item.id }
                is Show -> getLikedShows().any { it.id == item.id }
                is ShowDetails -> getLikedShows().any { it.id == item.id }
                is Anime -> getLikedAnime().any { it.id == item.id }
                is AnimeDetails -> item.data.id.toIntOrNull()?.let { animeId ->
                    getLikedAnime().any { it.id == animeId }
                } == true
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if item is liked: $item", e)
            false
        }
    }

    /**
     * Extension function to convert [MovieDetails] to a minimal [Movie].
     */
    private fun MovieDetails.toMinimalMovie() = Movie(
        id = id,
        title = title,
        overview = overview ?: "",
        poster_path = poster_path,
        vote_average = vote_average,
        release_date = release_date,
        isLiked = true
    )

    /**
     * Extension function to convert [ShowDetails] to a minimal [Show].
     */
    private fun ShowDetails.toMinimalShow() = Show(
        id = id ?: 0,
        name = name,
        overview = overview ?: "",
        poster_path = poster_path,
        vote_average = vote_average,
        first_air_date = first_air_date,
        isLiked = true
    )

    /**
     * Extension function to convert [AnimeDetails] to a minimal [Anime].
     */
    private fun AnimeDetails.toMinimalAnime() = Anime(
        id = data.id.toInt(),
        isLiked = true,
        attributes = MiniAttributes(
            canonicalTitle = data.attributes?.canonicalTitle,
            posterImage = data.attributes?.posterImage,
            synopsis = data.attributes?.synopsis,
            averageRating = data.attributes?.averageRating ?: R.string._0_0.toString(),
            startDate = data.attributes?.startDate
        )
    )

    companion object {
        private const val TAG = "LikeButtonUtils"
    }
}
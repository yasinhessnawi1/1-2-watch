// LikeButtonUtils.kt
package com.example.a1_2_watch.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.AnimeDetails
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.MovieDetails
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.ShowDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LikeButtonUtils(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    private val gson = Gson()

    suspend fun toggleLikeToItem(item: Any) = withContext(Dispatchers.IO) {
        when (item) {
            is Movie -> toggleLike(item, "liked_movies")
            is MovieDetails -> {
                val minimalItem = Movie(
                    id = item.id,
                    title = item.title,
                    overview = item.overview ?: "",
                    poster_path = item.poster_path,
                    vote_average = item.vote_average,
                    release_date = item.release_date,
                    isLiked = true
                )
                toggleLike(minimalItem, "liked_movies")
            }
            is Show -> toggleLike(item, "liked_shows")
            is ShowDetails -> {
                val minimalItem = Show(
                    id = item.id,
                    name = item.name,
                    overview = item.overview ?: "",
                    poster_path = item.poster_path,
                    vote_average = item.vote_average,
                    first_air_date = item.first_air_date,
                    isLiked = true
                )
                toggleLike(minimalItem, "liked_shows")
            }
            is Anime -> toggleLike(item, "liked_anime")
            is AnimeDetails -> {
                val animeId = item.data?.id?.toIntOrNull()
                val attributes = item.data?.attributes
                if (animeId != null && attributes != null) {
                    val minimalItem = Anime(id = animeId, attributes = attributes, isLiked = true)
                    toggleLike(minimalItem, "liked_anime")
                }
            }
        }
    }


    private fun toggleLike(item: Any, key: String) {
        // Retrieve the existing liked items as a list
        val likedItemsJson = sharedPreferences.getString(key, null)
        val likedItems = if (likedItemsJson != null) {
            gson.fromJson<MutableList<String>>(likedItemsJson, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            mutableListOf()
        }

        // Deserialize existing items
        val likedItemsList = likedItems.mapNotNull { json ->
            when (key) {
                "liked_movies" -> gson.fromJson(json, Movie::class.java)
                "liked_shows" -> gson.fromJson(json, Show::class.java)
                "liked_anime" -> gson.fromJson(json, Anime::class.java)
                else -> null
            }
        }.toMutableList()

        // Check if the item is already liked
        val itemId = when (item) {
            is Movie -> item.id
            is Show -> item.id
            is Anime -> item.id
            else -> return
        }

        val existingItemIndex = likedItemsList.indexOfFirst { likedItem ->
            when (likedItem) {
                is Movie -> likedItem.id == itemId
                is Show -> likedItem.id == itemId
                is Anime -> likedItem.id == itemId
                else -> false
            }
        }

        if (existingItemIndex != -1) {
            // Item is already liked, remove it
            likedItemsList.removeAt(existingItemIndex)
        } else {
            // Item is not liked, add it to the beginning
            likedItemsList.add(0, item)
        }

        // Serialize the updated liked items back to JSON strings
        val updatedJsonList = likedItemsList.map { gson.toJson(it) }

        // Save back to SharedPreferences
        val updatedJson = gson.toJson(updatedJsonList)
        sharedPreferences.edit().putString(key, updatedJson).apply()
    }



    suspend fun getLikedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val likedItemsJson = sharedPreferences.getString("liked_movies", null)
        val likedItems = if (likedItemsJson != null) {
            gson.fromJson<MutableList<String>>(likedItemsJson, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            mutableListOf()
        }

        likedItems.mapNotNull { json ->
            val movie = gson.fromJson(json, Movie::class.java)
            movie.isLiked = true
            movie
        }
    }


  suspend fun getLikedShows(): List<Show> = withContext(Dispatchers.IO) {
        val likedItemsJsonSet = sharedPreferences.getString("liked_shows", null)
        val likedItems = if (likedItemsJsonSet != null) {
          gson.fromJson<MutableList<String>>(likedItemsJsonSet, object : TypeToken<MutableList<String>>() {}.type)
      } else {
          mutableListOf()
      }
      likedItems.mapNotNull { json ->
          val show = gson.fromJson(json, Show::class.java)
          show.isLiked = true
          show
      }

    }

    suspend fun getLikedAnime(): List<Anime> = withContext(Dispatchers.IO) {
        val likedAnimeJsonSet = sharedPreferences.getString("liked_anime", null)
        val likedAnime = if (likedAnimeJsonSet != null) {
            gson.fromJson<MutableList<String>>(likedAnimeJsonSet, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            mutableListOf()
        }
        likedAnime.mapNotNull { json ->
            val anime = gson.fromJson(json, Anime::class.java)
            anime.isLiked = true
            anime
        }
    }


    /**
     * This function checks if a given media item is in the liked list or not.
     *
     * @param item The media item to check the like status for.
     * @return Boolean indicating whether the item is liked or not.
     */
    suspend fun isItemLiked(item: Any): Boolean = withContext(Dispatchers.IO) {
        when (item) {
            is Movie -> {
                val likedMovies = getLikedMovies()
                likedMovies.any { it.id == item.id }
            }
            is MovieDetails -> {
                val likedMovies = getLikedMovies()
                likedMovies.any { it.id == item.id }
            }
            is Show -> {
                val likedShows = getLikedShows()
                likedShows.any { it.id == item.id }
            }
            is ShowDetails -> {
                val likedShows = getLikedShows()
                likedShows.any { it.id == item.id }
            }
            is Anime -> {
                val likedAnime = getLikedAnime()
                likedAnime.any { it.id == item.id }
            }
            is AnimeDetails -> {
                val animeId = item.data?.id?.toIntOrNull()
                if (animeId != null) {
                    val likedAnime = getLikedAnime()
                    likedAnime.any { it.id == animeId }
                } else {
                    false
                }
            }
            else -> false
        }
    }

}


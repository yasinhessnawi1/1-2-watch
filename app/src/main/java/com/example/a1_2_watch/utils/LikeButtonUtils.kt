package com.example.a1_2_watch.utils

import android.content.Context
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * LikeButtonUtils class for managing the like status of media items (movies, shows, and anime).
 * This class stores liked items in shared preferences and provides functions for toggling and
 * checking the like status of items.
 *
 * @param context The context from which the shared preferences are accessed.
 */
class LikeButtonUtils(private val context: Context) {
    // Gson object for converting objects to/from JSON.
    val gson = Gson()
    // lazy-load shared preferences instance for storing liked items.
    val sharedPreferences by lazy {
        context.getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    /**
     * This function toggles like status of a given media item and adds the item to the liked list
     * if not already liked, or removes it if it is already liked.
     *
     * @param item The media item to toggle like status for.
     */
    fun toggleLikeToItem(item: Any) {
        // Editor to apply changes to SharedPreferences.
        val editor = sharedPreferences.edit()
        when (item) {
            is Movie -> {
                // Return the list of liked movies from the shared preferences.
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: MutableList<Movie> = gson.fromJson(
                        likedMoviesJson, object : TypeToken<MutableList<Movie>>() {}.type
                    )

                // Toggle like status based on if the item exists in the liked list or not.
                val isRemoved = likedMovies.removeIf { it.title == item.title }
                if (!isRemoved) {
                    item.isLiked = true
                    // Add the item if it was not existed in the liked list.
                    likedMovies.add(item)
                } else {
                    item.isLiked = false
                }

                // Save the updated liked movies list to SharedPreferences.
                editor.putString("liked_movies", gson.toJson(likedMovies))
                editor.apply()
            }

            is Show -> {
                // Return the list of liked TV shows from the shared preferences.
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: MutableList<Show> = gson.fromJson(
                        likedShowsJson, object : TypeToken<MutableList<Show>>() {}.type
                    )

                // Toggle like status based on if the item exists in the liked list or not.
                val isRemoved = likedShows.removeIf { it.name == item.name }
                if (!isRemoved) {
                    item.isLiked = true
                    // Add the item if it was not existed in the liked list.
                    likedShows.add(item)
                } else {
                    item.isLiked = false
                }

                // Save the updated liked TV shows list to SharedPreferences.
                editor.putString("liked_shows", gson.toJson(likedShows))
                editor.apply()
            }

            is Anime -> {
                // Return the list of liked anime from the shared preferences.
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: MutableList<Anime> = gson.fromJson(
                        likedAnimeJson, object : TypeToken<MutableList<Anime>>() {}.type
                    )

                // Toggle like status based on if the item exists in the liked list or not.
                val isRemoved = likedAnime.removeIf { it.attributes.canonicalTitle == item.attributes.canonicalTitle }
                if (!isRemoved) {
                    item.isLiked = true
                    // Add the item if it was not existed in the liked list.
                    likedAnime.add(item)
                } else {
                    item.isLiked = false
                }

                // Save the updated liked anime list to SharedPreferences.
                editor.putString("liked_anime", gson.toJson(likedAnime))
                editor.apply()
            }
        }
    }

    /**
     * This function checks if a given media item is in the liked list or not.
     *
     * @param item The media item to check the like status for.
     * @return Boolean indicating whether the item is liked or not.
     */
    fun isItemLiked(item: Any): Boolean {
        return when (item) {
            is Movie -> {
                // Return the list of liked movies from the shared preferences.
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type
                    )
                // Check if the item is already in the list of liked movies
                likedMovies.any { it.title == item.title }
            }

            is Show -> {
                // Return the list of liked TV shows from the shared preferences.
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> = gson.fromJson(
                        likedShowsJson, object : TypeToken<List<Show>>() {}.type
                    )
                // Check if the item is already in the list of liked TV shows.
                likedShows.any { it.name == item.name }
            }

            is Anime -> {
                // Return the list of liked anime from the shared preferences.
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> = gson.fromJson(
                        likedAnimeJson, object : TypeToken<List<Anime>>() {}.type
                    )
                // Check if the item is already in the list of liked anime.
                likedAnime.any { it.attributes.canonicalTitle == item.attributes.canonicalTitle }
            }
            // Return false if the item title is not recognized.
            else -> false
        }
    }
}
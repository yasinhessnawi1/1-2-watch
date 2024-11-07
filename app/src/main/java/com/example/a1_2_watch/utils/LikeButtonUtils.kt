package com.example.a1_2_watch.utils

import android.content.Context
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class LikeButtonUtils(private val context: Context) {
    val gson = Gson()
    val sharedPreferences by lazy {
        context.getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    fun toggleLikeToItem(item: Any) {
        val editor = sharedPreferences.edit()
        when (item) {
            is Movie -> {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: MutableList<Movie> =
                    gson.fromJson(
                        likedMoviesJson,
                        object : TypeToken<MutableList<Movie>>() {}.type
                    )

                val isRemoved = likedMovies.removeIf { it.title == item.title }
                if (!isRemoved) {
                    item.isLiked = true
                    likedMovies.add(item)
                } else {
                    item.isLiked = false
                }

                editor.putString("liked_movies", gson.toJson(likedMovies))
                editor.apply()
            }

            is Show -> {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: MutableList<Show> =
                    gson.fromJson(
                        likedShowsJson,
                        object : TypeToken<MutableList<Show>>() {}.type
                    )

                val isRemoved = likedShows.removeIf { it.name == item.name }
                if (!isRemoved) {
                    item.isLiked = true
                    likedShows.add(item)
                } else {
                    item.isLiked = false
                }

                editor.putString("liked_shows", gson.toJson(likedShows))
                editor.apply()
            }

            is Anime -> {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: MutableList<Anime> =
                    gson.fromJson(
                        likedAnimeJson,
                        object : TypeToken<MutableList<Anime>>() {}.type
                    )

                val isRemoved =
                    likedAnime.removeIf { it.attributes.canonicalTitle == item.attributes.canonicalTitle }
                if (!isRemoved) {
                    item.isLiked = true
                    likedAnime.add(item)
                } else {
                    item.isLiked = false
                }

                editor.putString("liked_anime", gson.toJson(likedAnime))
                editor.apply()
            }
        }
    }
}
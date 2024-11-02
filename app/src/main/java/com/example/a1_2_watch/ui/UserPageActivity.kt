package com.example.a1_2_watch.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.UserPageLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.a1_2_watch.R
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.utils.NavigationUtils

class UserPageActivity : AppCompatActivity() {

    private lateinit var binding: UserPageLayoutBinding
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserPageLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Go Back button
        binding.goBackButton.setOnClickListener {
            finish() // Finish activity when back button is pressed
        }

        // Setup RecyclerViews and Adapters
        setupAdapters()

        // Load liked items initially
        loadLikedItems()

        // Setup Bottom Navigation
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Refresh liked items every time the UserPageActivity is resumed
        loadLikedItems()
    }

    private fun setupAdapters() {
        val context = this

        moviesAdapter = MediaAdapter(
            context = context,
            onItemClick = { movie ->  NavigationUtils.navigateToDetails(this, movie.id, MediaType.MOVIES.name) },
            onSaveClick = {
                toggleLikeAndRefresh(it)
            }
        )
        showsAdapter = MediaAdapter(
            context = context,
            onItemClick = { show ->  NavigationUtils.navigateToDetails(this, show.id, MediaType.TV_SHOWS.name) },
            onSaveClick = {
                toggleLikeAndRefresh(it)
            }
        )
        animeAdapter = MediaAdapter(
            context = context,
            onItemClick = { anime ->  NavigationUtils.navigateToDetails(this, anime.id, MediaType.ANIME.name) },
            onSaveClick = {
                toggleLikeAndRefresh(it)
            }
        )

        binding.likedMoviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        binding.likedTvShowsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }
        binding.likedAnimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }

    private fun loadLikedItems() {
        val gson = Gson()

        lifecycleScope.launch {
            // Load liked movies
            val likedMovies = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                likedMovies.forEach { it.isLiked = true } // Set isLiked to true for all liked movies
                likedMovies
            }
            withContext(Dispatchers.Main) {
                if (likedMovies.isEmpty()) {
                    binding.emptyMoviesTextView.visibility = View.VISIBLE
                    binding.likedMoviesRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyMoviesTextView.visibility = View.GONE
                    binding.likedMoviesRecyclerView.visibility = View.VISIBLE
                    moviesAdapter.setMediaList(likedMovies)
                }
            }

            // Load liked TV shows
            val likedShows = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                likedShows.forEach { it.isLiked = true } // Set isLiked to true for all liked shows
                likedShows
            }
            withContext(Dispatchers.Main) {
                if (likedShows.isEmpty()) {
                    binding.emptyTvShowsTextView.visibility = View.VISIBLE
                    binding.likedTvShowsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyTvShowsTextView.visibility = View.GONE
                    binding.likedTvShowsRecyclerView.visibility = View.VISIBLE
                    showsAdapter.setMediaList(likedShows)
                }
            }

            // Load liked anime
            val likedAnime = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                likedAnime.forEach { it.isLiked = true } // Set isLiked to true for all liked anime
                likedAnime
            }
            withContext(Dispatchers.Main) {
                if (likedAnime.isEmpty()) {
                    binding.emptyAnimeTextView.visibility = View.VISIBLE
                    binding.likedAnimeRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyAnimeTextView.visibility = View.GONE
                    binding.likedAnimeRecyclerView.visibility = View.VISIBLE
                    animeAdapter.setMediaList(likedAnime)
                }
            }
        }
    }

    private fun toggleLikeAndRefresh(item: Any) {
        val gson = Gson()
        lifecycleScope.launch(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            when (item) {
                is Movie -> {
                    val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                    val likedMovies: MutableList<Movie> = gson.fromJson(likedMoviesJson, object : TypeToken<MutableList<Movie>>() {}.type)
                    likedMovies.removeIf { it.title == item.title }
                    editor.putString("liked_movies", gson.toJson(likedMovies))
                    editor.apply()
                }
                is Show -> {
                    val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                    val likedShows: MutableList<Show> = gson.fromJson(likedShowsJson, object : TypeToken<MutableList<Show>>() {}.type)
                    likedShows.removeIf { it.name == item.name }
                    editor.putString("liked_shows", gson.toJson(likedShows))
                    editor.apply()
                }
                is Anime -> {
                    val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                    val likedAnime: MutableList<Anime> = gson.fromJson(likedAnimeJson, object : TypeToken<MutableList<Anime>>() {}.type)
                    likedAnime.removeIf { it.attributes.canonicalTitle == item.attributes.canonicalTitle }
                    editor.putString("liked_anime", gson.toJson(likedAnime))
                    editor.apply()
                }
            }

            // Update the UI after toggling like
            withContext(Dispatchers.Main) {
                loadLikedItems()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.user
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    finish() // Go back to Home Activity
                    true
                }
                R.id.user -> true
                R.id.discover -> {
                    val intent = Intent(this, DiscoverActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}

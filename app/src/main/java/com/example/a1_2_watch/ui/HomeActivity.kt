package com.example.a1_2_watch.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.HomeLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.MediaHandler
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.models.MediaType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeLayoutBinding
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var tvShowsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private val mediaHandler = MediaHandler()
    private var currentPage = 1
    private var isLoading = false
    private val animeLimit = 20 // Limit for anime items per page
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        fetchAllMedia() // Fetch data for all categories on activity start
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.selectedItemId = R.id.home
        refreshLikedStatus()
    }

    private fun setupRecyclerViews() {
        // Set up Movies RecyclerView
        moviesAdapter = MediaAdapter(
            context = this,
            onItemClick = { movie ->
                // Handle movie item click
                NavigationUtils.navigateToDetails(this, movie.id, MediaType.MOVIES.name)
            },
            onSaveClick = { movie ->
                toggleLike(movie)
            }
        )
        binding.moviesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.moviesRecyclerView.adapter = moviesAdapter
        setupPaginationForRecyclerView(binding.moviesRecyclerView, MediaType.MOVIES)

        // Set up TV Shows RecyclerView
        tvShowsAdapter = MediaAdapter(
            context = this,
            onItemClick = { show ->
                // Handle show item click
                NavigationUtils.navigateToDetails(this, show.id, MediaType.TV_SHOWS.name)

            },
            onSaveClick = { show ->
                toggleLike(show)
            }
        )
        binding.tvShowsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tvShowsRecyclerView.adapter = tvShowsAdapter
        setupPaginationForRecyclerView(binding.tvShowsRecyclerView, MediaType.TV_SHOWS)

        // Set up Anime RecyclerView
        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { anime ->
                // Handle anime item click
                NavigationUtils.navigateToDetails(this, anime.id, MediaType.ANIME.name)
            },
            onSaveClick = { anime ->
                toggleLike(anime)
            }
        )
        binding.animeRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.animeRecyclerView.adapter = animeAdapter
        setupPaginationForRecyclerView(binding.animeRecyclerView, MediaType.ANIME)
    }

    private fun setupPaginationForRecyclerView(recyclerView: RecyclerView, mediaType: MediaType) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager =
                    recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition =
                    layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                    isLoading = true
                    currentPage++

                    // Fetch data based on media type
                    when (mediaType) {
                        MediaType.MOVIES -> {
                            fetchMovies()
                        }

                        MediaType.TV_SHOWS -> {
                            fetchTVShows()
                        }

                        MediaType.ANIME -> {
                            fetchAnime()
                        }
                    }
                }
            }
        })
    }

    private fun fetchAllMedia() {
        isLoading = true
        fetchMovies()
        fetchTVShows()
        fetchAnime()
    }

    private fun fetchMovies() {
        lifecycleScope.launch {
            val gson = Gson()
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                likedMovies.map { it.title }
            }

            val fetchedMovies = mediaHandler.fetchPopularMovies(currentPage)
            val updatedMovies = fetchedMovies.map { movie ->
                movie.isLiked = likedMoviesTitles.contains(movie.title)
                movie
            }
            withContext(Dispatchers.Main) {
                moviesAdapter.addMediaItems(updatedMovies)
                onLoadingComplete()
            }
        }
    }

    private fun fetchTVShows() {
        lifecycleScope.launch {
            val gson = Gson()
            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                likedShows.map { it.name }
            }

            val fetchedShows = mediaHandler.fetchPopularTVShows(currentPage)
            val updatedShows = fetchedShows.map { show ->
                show.isLiked = likedShowsNames.contains(show.name)
                show
            }
            withContext(Dispatchers.Main) {
                tvShowsAdapter.addMediaItems(updatedShows)
                onLoadingComplete()
            }
        }
    }

    private fun fetchAnime() {
        lifecycleScope.launch {
            val gson = Gson()
            val likedAnimeTitles = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                likedAnime.map { it.attributes.canonicalTitle }
            }

            val fetchedAnime = mediaHandler.fetchPopularAnime(currentPage, animeLimit)
            val updatedAnime = fetchedAnime.map { anime ->
                anime.isLiked = likedAnimeTitles.contains(anime.attributes.canonicalTitle)
                anime
            }
            withContext(Dispatchers.Main) {
                animeAdapter.addMediaItems(updatedAnime)
                onLoadingComplete()
            }
        }
    }

    private fun onLoadingComplete() {
        isLoading = false
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.home
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true
                R.id.user -> {
                    NavigationUtils.navigateToUser(this)
                    true
                }

                R.id.discover -> {
                    NavigationUtils.navigateToDiscover(this)
                    true
                }

                else -> false
            }
        }
    }


    private fun toggleLike(item: Any) {
        val gson = Gson()
        lifecycleScope.launch(Dispatchers.IO) {
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

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                when (item) {
                    is Movie -> moviesAdapter.updateLikeStatus(item)
                    is Show -> tvShowsAdapter.updateLikeStatus(item)
                    is Anime -> animeAdapter.updateLikeStatus(item)
                }
            }
        }
    }


    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            val gson = Gson()

            // Refresh movies
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                likedMovies.mapNotNull { it.title }
            }
            moviesAdapter.refreshLikedStatus(likedMoviesTitles)

            // Refresh TV shows
            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                likedShows.mapNotNull { it.name }
            }
            tvShowsAdapter.refreshLikedStatus(likedShowsNames)

            // Refresh anime
            val likedAnimeTitles = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                likedAnime.map { it.attributes.canonicalTitle }
            }
            animeAdapter.refreshLikedStatus(likedAnimeTitles)
        }
    }
}


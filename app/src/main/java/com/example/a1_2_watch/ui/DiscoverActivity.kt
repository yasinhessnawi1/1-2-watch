package com.example.a1_2_watch.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.DiscoverAdapter
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.DiscoverLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.repository.DiscoverRepository
import com.example.a1_2_watch.repository.MediaRepository
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscoverActivity : AppCompatActivity() {
    private lateinit var binding: DiscoverLayoutBinding
    private val searchRepository = DiscoverRepository()
    private val mediaRepository = MediaRepository()
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private val discoverAdapter = DiscoverAdapter(onItemClick = { item ->
        handleItemClick(item) // Handle item click based on type
    })
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DiscoverLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goBackButton.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupSearchView()
        setupBottomNavigation()
        loadRelatedItems()
    }

    override fun onResume() {
        super.onResume()
        refreshLikedStatus()
    }

    private fun setupRecyclerView() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity)
            adapter = discoverAdapter
            visibility = View.GONE
        }

        moviesAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            fetchDetailsFromAPI = true
        )
        binding.moviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }

        showsAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            fetchDetailsFromAPI = true
        )
        binding.tvShowsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }

        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            fetchDetailsFromAPI = true
        )
        binding.animeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val trimmedQuery = newText?.trim() ?: ""
                if (trimmedQuery.isEmpty()) {
                    clearSearchResults()
                } else {
                    performSearch(trimmedQuery)
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            clearSearchResults()
            return
        }

        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        binding.searchResultsRecyclerView.bringToFront()
        toggleRelatedListsVisibility(false)

        val searchResults = mutableListOf<Any>()

        searchRepository.searchMovies(Constants.API_KEY, trimmedQuery) { movieResponse ->
            movieResponse?.results?.let { movies ->
                searchResults.addAll(movies)
                updateSearchResults(searchResults)
            }
        }

        searchRepository.searchTVShows(Constants.API_KEY, trimmedQuery) { showResponse ->
            showResponse?.results?.let { shows ->
                searchResults.addAll(shows)
                updateSearchResults(searchResults)
            }
        }

        searchRepository.searchAnime(trimmedQuery) { animeResponse ->
            animeResponse?.data?.let { animes ->
                searchResults.addAll(animes)
                updateSearchResults(searchResults)
            }
        }
    }

    private fun updateSearchResults(results: List<Any>) {
        discoverAdapter.updateItems(results)
    }

    private fun clearSearchResults() {
        discoverAdapter.updateItems(emptyList())
        binding.searchResultsRecyclerView.visibility = View.GONE
        toggleRelatedListsVisibility(true)
    }

    private fun handleItemClick(item: Any) {
        when (item) {
            is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
            is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
            is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.discover
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    NavigationUtils.navigateToHome(this)
                    true
                }
                R.id.user -> {
                    NavigationUtils.navigateToUser(this)
                    true
                }
                R.id.discover -> true
                else -> false
            }
        }
    }

    private fun toggleRelatedListsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.moviesTitleLayout.visibility = visibility
        binding.moviesRecyclerView.visibility = visibility
        binding.tvShowsTitleLayout.visibility = visibility
        binding.tvShowsRecyclerView.visibility = visibility
        binding.animeTitleLayout.visibility = visibility
        binding.animeRecyclerView.visibility = visibility
        binding.emptyMoviesTextView.visibility = visibility
        binding.emptyTvShowsTextView.visibility = visibility
        binding.emptyAnimeTextView.visibility = visibility
    }

    private fun loadRelatedItems() {
        lifecycleScope.launch {
            val likedMovies = getLikedMovies()
            val likedTVShows = getLikedTVShows()

            val relatedMovies = fetchRelatedMovies(likedMovies)
            val relatedTVShows = fetchRelatedTVShows(likedTVShows)
            val relatedAnime = fetchRelatedAnime()

            displayRelatedItems(relatedMovies, relatedTVShows, relatedAnime)
        }
    }

    private suspend fun getLikedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
        Gson().fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
    }

    private suspend fun getLikedTVShows(): List<Show> = withContext(Dispatchers.IO) {
        val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
        Gson().fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
    }

    private suspend fun fetchRelatedMovies(likedMovies: List<Movie>): List<Movie> {
        val relatedMovies = mutableListOf<Movie>()
        for (movie in likedMovies) {
            val related = mediaRepository.fetchRelatedMovies(movie.id)
            relatedMovies.addAll(related.take(10))
        }
        return relatedMovies
    }

    private suspend fun fetchRelatedTVShows(likedTVShows: List<Show>): List<Show> {
        val relatedShows = mutableListOf<Show>()
        for (show in likedTVShows) {
            val related = mediaRepository.fetchRelatedTVShows(show.id)
            relatedShows.addAll(related.take(10))
        }
        return relatedShows
    }

    private suspend fun fetchRelatedAnime(): List<Anime> {
        val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
        val likedAnime: List<Anime> = Gson().fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
        val animeTypes = likedAnime.map { it.attributes.subtype }.distinct()

        val relatedAnime = mutableListOf<Anime>()
        for (type in animeTypes) {
            val related = mediaRepository.fetchAnimeByType(type)
            relatedAnime.addAll(related.take(10))
        }
        return relatedAnime
    }

    private fun displayRelatedItems(movies: List<Movie>, shows: List<Show>, anime: List<Anime>) {
        moviesAdapter.setMediaList(movies)
        showsAdapter.setMediaList(shows)
        animeAdapter.setMediaList(anime)

        binding.emptyMoviesTextView.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyTvShowsTextView.visibility = if (shows.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyAnimeTextView.visibility = if (anime.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun toggleLike(item: Any) {
        lifecycleScope.launch(Dispatchers.IO) {
            LikeButtonUtils(this@DiscoverActivity).toggleLikeToItem(item)
            withContext(Dispatchers.Main) {
                when (item) {
                    is Movie -> moviesAdapter.updateLikeStatus(item)
                    is Show -> showsAdapter.updateLikeStatus(item)
                    is Anime -> animeAdapter.updateLikeStatus(item)
                }
            }
        }
    }

    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            val gson = Gson()

            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                likedMovies.mapNotNull { it.title }
            }
            moviesAdapter.refreshLikedStatus(likedMoviesTitles)

            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                likedShows.mapNotNull { it.name }
            }
            showsAdapter.refreshLikedStatus(likedShowsNames)

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

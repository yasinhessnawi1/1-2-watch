package com.example.a1_2_watch.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.DiscoverAdapter
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.DiscoverLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.workers.FetchRelatedMediaWorker
import com.example.a1_2_watch.workers.SearchMediaWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class DiscoverActivity : AppCompatActivity() {
    private lateinit var binding: DiscoverLayoutBinding
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private lateinit var discoverAdapter: DiscoverAdapter
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }
    private val likeButtonUtils = LikeButtonUtils(this)
    private val gson = Gson()

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
        // Using your existing DiscoverAdapter which takes only onItemClick
        discoverAdapter = DiscoverAdapter(
            onItemClick = { item -> handleItemClick(item) }
        )
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity)
            adapter = discoverAdapter
            visibility = View.GONE
        }

        moviesAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            onExpandClick = { item -> expandItem(item) }
        )
        binding.moviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }

        showsAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            onExpandClick = { item -> expandItem(item) }
        )
        binding.tvShowsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }

        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
            onExpandClick = { item -> expandItem(item) }
        )
        binding.animeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }

    private fun handleItemClick(item: Any) {
        when (item) {
            is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
            is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
            is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
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

        val inputData = workDataOf(
            SearchMediaWorker.KEY_QUERY to trimmedQuery
        )

        val searchWork = OneTimeWorkRequestBuilder<SearchMediaWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(searchWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(searchWork.id)
            .observe(this) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(SearchMediaWorker.KEY_RESULT)
                        handleSearchResults(resultJson)
                    } else {
                        updateSearchResults(emptyList())
                    }
                }
            }
    }

    private fun handleSearchResults(resultJson: String?) {
        if (resultJson == null) {
            updateSearchResults(emptyList())
            return
        }
        val type = object : TypeToken<List<Any>>() {}.type
        val searchResults: List<Any> = gson.fromJson(resultJson, type)
        updateSearchResults(searchResults)
    }

    private fun updateSearchResults(results: List<Any>) {
        discoverAdapter.updateItems(results)
    }

    private fun clearSearchResults() {
        discoverAdapter.updateItems(emptyList())
        binding.searchResultsRecyclerView.visibility = View.GONE
        toggleRelatedListsVisibility(true)
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
        val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]") ?: "[]"
        gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
    }

    private suspend fun getLikedTVShows(): List<Show> = withContext(Dispatchers.IO) {
        val likedShowsJson = sharedPreferences.getString("liked_shows", "[]") ?: "[]"
        gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
    }

    private suspend fun fetchRelatedMovies(likedMovies: List<Movie>): List<Movie> {
        val relatedMovies = mutableListOf<Movie>()
        for (movie in likedMovies) {
            val inputData = workDataOf(
                FetchRelatedMediaWorker.KEY_MEDIA_TYPE to MediaType.MOVIES.name,
                FetchRelatedMediaWorker.KEY_MEDIA_ID to movie.id
            )

            val fetchRelatedWork = OneTimeWorkRequestBuilder<FetchRelatedMediaWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this).enqueue(fetchRelatedWork).await()

            val workInfo = WorkManager.getInstance(this).getWorkInfoById(fetchRelatedWork.id).await()

            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val resultJson = workInfo.outputData.getString(FetchRelatedMediaWorker.KEY_RESULT)
                if (resultJson != null) {
                    val type = object : TypeToken<List<Movie>>() {}.type
                    val related: List<Movie> = gson.fromJson(resultJson, type)
                    related.forEach { it.isLiked = likeButtonUtils.isItemLiked(it) }
                    relatedMovies.addAll(related.take(10))
                }
            }
        }
        return relatedMovies
    }

    private suspend fun fetchRelatedTVShows(likedTVShows: List<Show>): List<Show> {
        val relatedTVShows = mutableListOf<Show>()
        for (show in likedTVShows) {
            val inputData = workDataOf(
                FetchRelatedMediaWorker.KEY_MEDIA_TYPE to MediaType.TV_SHOWS.name,
                FetchRelatedMediaWorker.KEY_MEDIA_ID to show.id
            )

            val fetchRelatedWork = OneTimeWorkRequestBuilder<FetchRelatedMediaWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this).enqueue(fetchRelatedWork).await()

            val workInfo = WorkManager.getInstance(this).getWorkInfoById(fetchRelatedWork.id).await()

            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val resultJson = workInfo.outputData.getString(FetchRelatedMediaWorker.KEY_RESULT)
                if (resultJson != null) {
                    val type = object : TypeToken<List<Show>>() {}.type
                    val related: List<Show> = gson.fromJson(resultJson, type)
                    related.forEach { it.isLiked = likeButtonUtils.isItemLiked(it) }
                    relatedTVShows.addAll(related.take(10))
                }
            }
        }
        return relatedTVShows
    }

    private suspend fun fetchRelatedAnime(): List<Anime> {
        // Implement fetching related anime if needed
        return emptyList()
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
            likeButtonUtils.toggleLikeToItem(item)

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
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]") ?: "[]"
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                likedMovies.mapNotNull { it.title }
            }
            moviesAdapter.refreshLikedStatus(likedMoviesTitles)

            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]") ?: "[]"
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                likedShows.mapNotNull { it.name }
            }
            showsAdapter.refreshLikedStatus(likedShowsNames)

            val likedAnimeTitles = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]") ?: "[]"
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                likedAnime.map { it.attributes.canonicalTitle }
            }
            animeAdapter.refreshLikedStatus(likedAnimeTitles)
        }
    }

    private fun expandItem(item: Any) {
        when (item) {
            is Movie -> moviesAdapter.toggleItemExpansion(item)
            is Show -> showsAdapter.toggleItemExpansion(item)
            is Anime -> animeAdapter.toggleItemExpansion(item)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.discover
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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
}
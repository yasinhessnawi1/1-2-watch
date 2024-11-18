// DiscoverActivity.kt
package com.example.a1_2_watch.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.DiscoverAdapter
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.DiscoverLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.workers.FetchRelatedItemsWorker
import com.example.a1_2_watch.workers.FetchSearchResultsWorker
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscoverActivity : AppCompatActivity() {
    private lateinit var binding: DiscoverLayoutBinding

    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private val discoverViewModel: DiscoverViewModel by viewModels()

    private val discoverAdapter = DiscoverAdapter(onItemClick = { item ->
        handleItemClick(item)
    })
    private lateinit var likeButtonUtils: LikeButtonUtils


    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DiscoverLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.goBackButton.setOnClickListener {
            finish()
        }
        likeButtonUtils = LikeButtonUtils(this)
        setupRecyclerView()
        setupSearchView()
        setupBottomNavigation()
        observeViewModel()
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
        )
        binding.moviesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        showsAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
        )
        binding.tvShowsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }
        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { item -> handleItemClick(item) },
            onSaveClick = { item -> toggleLike(item) },
        )
        binding.animeRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }
    private fun observeViewModel() {
        discoverViewModel.relatedMovies.observe(this, Observer { movies ->
            moviesAdapter.setMediaList(movies)
            binding.emptyMoviesTextView.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
        })

        discoverViewModel.relatedShows.observe(this, Observer { shows ->
            showsAdapter.setMediaList(shows)
            binding.emptyTvShowsTextView.visibility = if (shows.isEmpty()) View.VISIBLE else View.GONE
        })

        discoverViewModel.relatedAnime.observe(this, Observer { anime ->
            animeAdapter.setMediaList(anime)
            binding.emptyAnimeTextView.visibility = if (anime.isEmpty()) View.VISIBLE else View.GONE
        })
    }



    private fun handleItemClick(item: Any) {
        when (item) {
            is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
            is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
            is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
        }
    }

    private fun setupSearchView() {
        binding.searchViewTextField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

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

        // Customize the SearchView appearance
        val searchIcon = binding.searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setImageResource(R.drawable.search)
        searchIcon.adjustViewBounds = true// Make sure you have this drawable
        searchIcon.visibility = View.VISIBLE
        binding.searchViewTextField.queryHint = "Search any media..."
        var searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.nav_item_unselected))
        searchEditText.setPadding(0, searchEditText.paddingTop, searchEditText.paddingRight, searchEditText.paddingBottom)

        // Remove the underline
        val searchPlate = binding.searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate.setBackgroundColor(Color.TRANSPARENT)
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
            FetchSearchResultsWorker.KEY_QUERY to trimmedQuery
        )
        val searchWorkRequest = OneTimeWorkRequestBuilder<FetchSearchResultsWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(searchWorkRequest)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(searchWorkRequest.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchSearchResultsWorker.KEY_RESULT)
                        val type = object : TypeToken<List<Any>>() {}.type
                        val  parsedSearchResults = parseSearchResults(resultJson ?: "[]")
                        updateSearchResults(parsedSearchResults)
                    } else {
                        // Handle failure if needed
                    }
                }
            })
    }



    private fun parseSearchResults(resultJson: String): List<Any> {
        // Parse the JSON string into a JsonElement
        val jsonElement: JsonElement = gson.fromJson(resultJson, JsonElement::class.java)
        // Get the JsonArray from the JsonElement
        val jsonArray = jsonElement.asJsonArray
        val searchResults = mutableListOf<Any>()

        for (jsonElement in jsonArray) {
            val jsonObject = jsonElement.asJsonObject

            val item: Any? = when {
                jsonObject.has("title") -> {
                    // This is a Movie
                    gson.fromJson(jsonObject, Movie::class.java)
                }
                jsonObject.has("name") -> {
                    // This is a Show
                    gson.fromJson(jsonObject, Show::class.java)
                }
                jsonObject.has("attributes") -> {
                    // This is an Anime
                    gson.fromJson(jsonObject, Anime::class.java)
                }
                else -> {
                    // Unknown type, handle if necessary
                    null
                }
            }

            if (item != null) {
                searchResults.add(item)
            }
        }
        return searchResults
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
        binding.emptyMoviesTextView.visibility = if (moviesAdapter.itemCount == 0) View.VISIBLE else View.GONE
        binding.emptyTvShowsTextView.visibility = if (showsAdapter.itemCount == 0) View.VISIBLE else View.GONE
        binding.emptyAnimeTextView.visibility = if (animeAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }


    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            val likedMovies = likeButtonUtils.getLikedMovies()
            val likedShows = likeButtonUtils.getLikedShows()
            val likedAnime = likeButtonUtils.getLikedAnime()

            withContext(Dispatchers.Main) {
                moviesAdapter.refreshLikedStatus(likedMovies.map { it.id })
                showsAdapter.refreshLikedStatus(likedShows.map { it.id })
                animeAdapter.refreshLikedStatus(likedAnime.map { it.id })
            }
        }
    }


    private fun toggleLike(item: Any) {
        lifecycleScope.launch {
            // Toggle like status
            likeButtonUtils.toggleLikeToItem(item)

            // Update item's liked status
            when (item) {
                is Movie -> {
                    item.isLiked = !item.isLiked
                    moviesAdapter.updateLikeStatus(item)
                }
                is Show -> {
                    item.isLiked = !item.isLiked
                    showsAdapter.updateLikeStatus(item)
                }
                is Anime -> {
                    item.isLiked = !item.isLiked
                    animeAdapter.updateLikeStatus(item)
                }
            }
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
}

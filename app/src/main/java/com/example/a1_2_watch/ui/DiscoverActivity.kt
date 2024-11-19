package com.example.a1_2_watch.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import com.example.a1_2_watch.workers.FetchSearchResultsWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for exploring media content, performing searches, and displaying recommendations.
 * Works with minimized items only for search results while maintaining full models for other views.
 */
class DiscoverActivity : AppCompatActivity() {

    // View binding for the discover layout.
    private lateinit var binding: DiscoverLayoutBinding

    // Adapters for media content.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>

    // Adapter for displaying search results with minimized items.
    private val discoverAdapter = DiscoverAdapter(onItemClick = { item ->
        handleSearchItemClick(item as MinimizedItem)
    })

    // ViewModel for observing related media content.
    private val discoverViewModel: DiscoverViewModel by viewModels()

    // Utility for handling like button interactions.
    private lateinit var likeButtonUtils: LikeButtonUtils

    // Gson instance for JSON parsing.
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DiscoverActivity", "onCreate: Initializing activity")
        binding = DiscoverLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setupUI()
            setupRecyclerView()
            setupSearchView()
            setupBottomNavigation()
            observeViewModel()
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in onCreate: ${e.message}", e)
        }
    }

    /**
     * Called when the activity resumes. Refreshes the like status for displayed items.
     */
    override fun onResume() {
        super.onResume()
        try {
            Log.d("DiscoverActivity", "onResume: Refreshing liked status")
            refreshLikedStatus()
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in onResume: ${e.message}", e)
        }
    }

    /**
     * Initializes the UI components, including the like button utility.
     */
    private fun setupUI() {
        try {
            Log.d("DiscoverActivity", "Setting up UI components")
            likeButtonUtils = LikeButtonUtils(this)
            binding.goBackButton.setOnClickListener { finish() }
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in setupUI: ${e.message}", e)
        }
    }

    /**
     * Sets up the RecyclerViews for movies, TV shows, anime, and search results.
     */
    private fun setupRecyclerView() {
        try {
            Log.d("DiscoverActivity", "Setting up RecyclerViews")
            // Search results RecyclerView.
            binding.searchResultsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@DiscoverActivity)
                adapter = discoverAdapter
                visibility = View.GONE
            }

            // Movies RecyclerView.
            moviesAdapter = createMediaAdapter<Movie>()
            binding.moviesRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = moviesAdapter
            }

            // TV shows RecyclerView.
            showsAdapter = createMediaAdapter<Show>()
            binding.tvShowsRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = showsAdapter
            }

            // Anime RecyclerView.
            animeAdapter = createMediaAdapter<Anime>()
            binding.animeRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = animeAdapter
            }
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in setupRecyclerView: ${e.message}", e)
        }
    }

    /**
     * Creates a MediaAdapter instance for a specific type.
     */
    private fun <T> createMediaAdapter(): MediaAdapter<T> {
        return MediaAdapter(
            context = this,
            onItemClick = { item -> handleMediaItemClick(item as Any) },
            onSaveClick = { item -> toggleLike(item as Any) }
        )
    }


    /**
     * Observes the ViewModel to update the media RecyclerViews with related content.
     */
    private fun observeViewModel() {
        try {
            Log.d("DiscoverActivity", "Observing ViewModel for data changes")
            discoverViewModel.relatedMovies.observe(this) { movies ->
                moviesAdapter.setMediaList(movies)
                binding.emptyMoviesTextView.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
            }

            discoverViewModel.relatedShows.observe(this) { shows ->
                showsAdapter.setMediaList(shows)
                binding.emptyTvShowsTextView.visibility = if (shows.isEmpty()) View.VISIBLE else View.GONE
            }

            discoverViewModel.relatedAnime.observe(this) { anime ->
                animeAdapter.setMediaList(anime)
                binding.emptyAnimeTextView.visibility = if (anime.isEmpty()) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in observeViewModel: ${e.message}", e)
        }
    }

    /**
     * Configures the SearchView for handling user input.
     */
    private fun setupSearchView() {
        try {
            Log.d("DiscoverActivity", "Setting up SearchView")
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

            // Customize SearchView appearance.
            val searchIcon = binding.searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
            searchIcon.setImageResource(R.drawable.search)
            searchIcon.visibility = View.VISIBLE
            binding.searchViewTextField.queryHint = "Search any media..."
            val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
            searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.nav_item_unselected))
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in setupSearchView: ${e.message}", e)
        }
    }

    /**
     * Configures the bottom navigation bar behavior.
     */
    private fun setupBottomNavigation() {
        try {
            Log.d("DiscoverActivity", "Setting up bottom navigation")
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
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error in setupBottomNavigation: ${e.message}", e)
        }
    }

    /**
     * Performs a search using the provided query and updates the UI with results.
     *
     * @param query The search query entered by the user.
     */
    private fun performSearch(query: String) {
        try {
            Log.d("DiscoverActivity", "Performing search with query: $query")
            val inputData = workDataOf(FetchSearchResultsWorker.KEY_QUERY to query.trim())
            val searchWorkRequest = OneTimeWorkRequestBuilder<FetchSearchResultsWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this).enqueue(searchWorkRequest)

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(searchWorkRequest.id)
                .observe(this) { workInfo ->
                    if (workInfo?.state?.isFinished == true) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            val resultJson = workInfo.outputData.getString(FetchSearchResultsWorker.KEY_RESULT)
                            val searchResults = parseSearchResults(resultJson ?: "[]")
                            updateSearchResults(searchResults)
                        } else {
                            Log.e("DiscoverActivity", "Search WorkManager task failed.")
                            clearSearchResults()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error performing search: ${e.message}", e)
        }
    }

    /**
     * Parses the search results JSON into a list of MinimizedItem objects.
     *
     * @param resultJson The JSON string containing the search results.
     * @return A list of parsed MinimizedItem objects.
     */
    private fun parseSearchResults(resultJson: String): List<MinimizedItem> {
        return try {
            Log.d("DiscoverActivity", "Parsing search results JSON")
            val type = object : TypeToken<List<MinimizedItem>>() {}.type
            gson.fromJson(resultJson, type)
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error parsing search results: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Updates the UI with the provided search results.
     *
     * @param results The list of search results to display.
     */
    private fun updateSearchResults(results: List<MinimizedItem>) {
        try {
            Log.d("DiscoverActivity", "Updating search results with ${results.size} items")
            discoverAdapter.updateItems(results)
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
            binding.searchResultsRecyclerView.bringToFront()
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error updating search results: ${e.message}", e)
        }
    }

    /**
     * Clears the search results and resets the UI.
     */
    private fun clearSearchResults() {
        try {
            Log.d("DiscoverActivity", "Clearing search results")
            discoverAdapter.updateItems(emptyList())
            binding.searchResultsRecyclerView.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error clearing search results: ${e.message}", e)
        }
    }

    /**
     * Handles click events for search items.
     *
     * @param item The clicked search result.
     */
    private fun handleSearchItemClick(item: MinimizedItem) {
        try {
            Log.d("DiscoverActivity", "Search item clicked: ${item.id} (${item.type})")
            NavigationUtils.navigateToDetails(this, item.id, item.type)
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error handling search item click: ${e.message}", e)
        }
    }

    /**
     * Handles click events for media items.
     *
     * @param item The clicked media item (Movie, Show, Anime).
     */
    private fun handleMediaItemClick(item: Any) {
        try {
            Log.d("DiscoverActivity", "Media item clicked: $item")
            when (item) {
                is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
                is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
                is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
            }
        } catch (e: Exception) {
            Log.e("DiscoverActivity", "Error handling media item click: ${e.message}", e)
        }
    }

    /**
     * Toggles the like status of a given media item.
     *
     * @param item The media item to toggle.
     */
    private fun toggleLike(item: Any) {
        lifecycleScope.launch {
            try {
                Log.d("DiscoverActivity", "Toggling like status for item: $item")
                likeButtonUtils.toggleLikeToItem(item)
                updateLikeStatus(item)
            } catch (e: Exception) {
                Log.e("DiscoverActivity", "Error toggling like status: ${e.message}", e)
            }
        }
    }

    /**
     * Updates the like status of a media item in its corresponding adapter.
     *
     * @param item The media item to update.
     */
    private fun updateLikeStatus(item: Any) {
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

    /**
     * Refreshes the like status for all media items.
     */
    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            try {
                Log.d("DiscoverActivity", "Refreshing liked status for all media items")
                val likedMovies = likeButtonUtils.getLikedMovies()
                val likedShows = likeButtonUtils.getLikedShows()
                val likedAnime = likeButtonUtils.getLikedAnime()

                withContext(Dispatchers.Main) {
                    moviesAdapter.refreshLikedStatus(likedMovies.map { it.id })
                    showsAdapter.refreshLikedStatus(likedShows.map { it.id })
                    animeAdapter.refreshLikedStatus(likedAnime.map { it.id })
                }
            } catch (e: Exception) {
                Log.e("DiscoverActivity", "Error refreshing liked status: ${e.message}", e)
            }
        }
    }
}

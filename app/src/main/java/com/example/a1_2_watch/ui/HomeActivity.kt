package com.example.a1_2_watch.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.HomeLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.workers.FetchPopularMediaWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HomeActivity is the main activity of the application. It displays lists of popular movies, TV shows,
 * and anime. It also handles pagination, like functionality, and navigation between different sections.
 */
class HomeActivity : AppCompatActivity() {

    // Binding object for accessing views in the layout.
    private lateinit var binding: HomeLayoutBinding

    // Adapters for managing and displaying lists of movies, TV shows, and anime.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var tvShowsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>

    // Utility for managing like functionality.
    private lateinit var likeButtonUtils: LikeButtonUtils

    // Current page number for pagination.
    private var currentPage = 1

    // AtomicBoolean to ensure thread-safe operations for loading state.
    private val isLoading = AtomicBoolean(false)

    // Limit for the number of anime items per page.
    private val animeLimit = 10

    // Gson instance for JSON parsing.
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("HomeActivity", "onCreate: Initializing HomeActivity")
            // Inflate the layout using view binding.
            binding = HomeLayoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize LikeButtonUtils.
            likeButtonUtils = LikeButtonUtils(this)

            // Set up RecyclerViews for movies, TV shows, and anime.
            setupRecyclerViews()

            // Fetch initial data for all media categories.
            fetchAllMedia()

            // Set up the bottom navigation bar.
            setupBottomNavigation()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error during onCreate: ${e.message}", e)
        }
    }

    /**
     * Called when the activity resumes. Refreshes the liked status of displayed media items.
     */
    override fun onResume() {
        super.onResume()
        try {
            Log.d("HomeActivity", "onResume: Refreshing liked statuses")
            // Set the bottom navigation bar to the Home screen.
            binding.bottomNavigationView.selectedItemId = R.id.home

            // Refresh the liked status of media items.
            refreshLikedStatus()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error during onResume: ${e.message}", e)
        }
    }

    /**
     * Sets up the RecyclerViews and their corresponding adapters for movies, TV shows, and anime.
     * Also configures pagination for each RecyclerView.
     */
    private fun setupRecyclerViews() {
        try {
            Log.d("HomeActivity", "Setting up RecyclerViews")

            // Initialize Movies RecyclerView and its adapter.
            moviesAdapter = createMediaAdapter(MediaType.MOVIES)
            binding.moviesRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = moviesAdapter
            }
            setupPagination(binding.moviesRecyclerView, MediaType.MOVIES)

            // Initialize TV Shows RecyclerView and its adapter.
            tvShowsAdapter = createMediaAdapter(MediaType.TV_SHOWS)
            binding.tvShowsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = tvShowsAdapter
            }
            setupPagination(binding.tvShowsRecyclerView, MediaType.TV_SHOWS)

            // Initialize Anime RecyclerView and its adapter.
            animeAdapter = createMediaAdapter(MediaType.ANIME)
            binding.animeRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = animeAdapter
            }
            setupPagination(binding.animeRecyclerView, MediaType.ANIME)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error setting up RecyclerViews: ${e.message}", e)
        }
    }

    /**
     * Creates and returns a MediaAdapter for a specified media type.
     *
     * @param mediaType The type of media (MOVIES, TV_SHOWS, ANIME).
     * @return A MediaAdapter configured for the specified media type.
     */
    private fun <T> createMediaAdapter(mediaType: MediaType): MediaAdapter<T> {
        return MediaAdapter(
            context = this,
            onItemClick = { item ->
                try {
                    val mediaId = when (item) {
                        is Movie -> item.id
                        is Show -> item.id
                        is Anime -> item.id
                        else -> throw IllegalArgumentException("Unknown media type")
                    }
                    NavigationUtils.navigateToDetails(this, mediaId, mediaType.name)
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error navigating to details: ${e.message}", e)
                }
            },
            onSaveClick = { item ->
                try {
                    toggleLike(item)
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error toggling like status: ${e.message}", e)
                }
            }
        )
    }

    /**
     * Configures pagination for a given RecyclerView based on the media type.
     * Loads additional items when the user scrolls to the end of the list.
     *
     * @param recyclerView The RecyclerView to set up pagination for.
     * @param mediaType The type of media (MOVIES, TV_SHOWS, ANIME) for which to fetch data.
     */
    private fun setupPagination(recyclerView: RecyclerView, mediaType: MediaType) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                try {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading.get() && lastVisibleItemPosition == totalItemCount - 1) {
                        Log.d("HomeActivity", "Pagination triggered for $mediaType at page $currentPage")
                        isLoading.set(true)
                        currentPage++

                        // Fetch more data based on media type.
                        when (mediaType) {
                            MediaType.MOVIES -> fetchMovies()
                            MediaType.TV_SHOWS -> fetchTVShows()
                            MediaType.ANIME -> fetchAnime()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error in pagination setup: ${e.message}", e)
                }
            }
        })
    }

    /**
     * Initiates fetching of media data for all categories (movies, TV shows, anime).
     * Sets the loading state to true to prevent multiple simultaneous fetches.
     */
    private fun fetchAllMedia() {
        try {
            Log.d("HomeActivity", "Fetching all media types")
            isLoading.set(true)
            fetchMovies()
            fetchTVShows()
            fetchAnime()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error fetching all media: ${e.message}", e)
        }
    }

    /**
     * Fetches popular movies and updates the adapter.
     */
    private fun fetchMovies() {
        try {
            fetchMedia(MediaType.MOVIES, currentPage) { resultJson ->
                handleMediaResult(resultJson, moviesAdapter, likeButtonUtils::getLikedMovies, MediaType.MOVIES)
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error fetching movies: ${e.message}", e)
        }
    }

    /**
     * Fetches popular TV shows and updates the adapter.
     */
    private fun fetchTVShows() {
        try {
            fetchMedia(MediaType.TV_SHOWS, currentPage) { resultJson ->
                handleMediaResult(resultJson, tvShowsAdapter, likeButtonUtils::getLikedShows, MediaType.TV_SHOWS)
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error fetching TV shows: ${e.message}", e)
        }
    }

    /**
     * Fetches popular anime and updates the adapter.
     */
    private fun fetchAnime() {
        try {
            fetchMedia(MediaType.ANIME, currentPage, animeLimit) { resultJson ->
                handleMediaResult(resultJson, animeAdapter, likeButtonUtils::getLikedAnime, MediaType.ANIME)
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error fetching anime: ${e.message}", e)
        }
    }

    /**
     * Fetches media data using WorkManager and updates the corresponding adapter.
     *
     * @param mediaType The type of media to fetch (MOVIES, TV_SHOWS, ANIME).
     * @param page The page number to fetch data for.
     * @param limit Optional limit for number of items per page.
     * @param handleResult Callback function to handle the fetched data result.
     */
    private fun fetchMedia(mediaType: MediaType, page: Int, limit: Int? = null, handleResult: (String?) -> Unit) {
        try {
            val inputDataBuilder = Data.Builder()
                .putString(FetchPopularMediaWorker.KEY_MEDIA_TYPE, mediaType.name)
                .putInt(FetchPopularMediaWorker.KEY_PAGE, page)

            limit?.let { inputDataBuilder.putInt(FetchPopularMediaWorker.KEY_RESULT, it) }

            val fetchMediaWork = OneTimeWorkRequestBuilder<FetchPopularMediaWorker>()
                .setInputData(inputDataBuilder.build())
                .build()

            WorkManager.getInstance(this).enqueue(fetchMediaWork)

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(fetchMediaWork.id)
                .observe(this) { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            val resultJson = workInfo.outputData.getString(FetchPopularMediaWorker.KEY_RESULT)
                            handleResult(resultJson)
                        } else {
                            Log.e("HomeActivity", "FetchMediaWorker failed for $mediaType")
                        }
                        onLoadingComplete()
                    }
                }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error fetching media for $mediaType: ${e.message}", e)
        }
    }

    /**
     * Handles the result from FetchPopularMediaWorker and updates the adapter.
     *
     * @param resultJson The JSON string containing the fetched media.
     * @param adapter The adapter for the media type.
     * @param getLikedItems Function to retrieve liked items of the media type.
     */
    private fun <T> handleMediaResult(
        resultJson: String?,
        adapter: MediaAdapter<T>,
        getLikedItems: suspend () -> List<T>,
        mediaType: MediaType
    ) {
        if (resultJson.isNullOrEmpty()) {
            Log.e("HomeActivity", "Received empty result for $mediaType")
            return
        }

        lifecycleScope.launch {
            try {
                val likedItems = getLikedItems()

                // Parse the JSON to the correct type
                val fetchedItems: List<T> = when (mediaType) {
                    MediaType.MOVIES -> gson.fromJson(resultJson, object : TypeToken<List<Movie>>() {}.type) as List<T>
                    MediaType.TV_SHOWS -> gson.fromJson(resultJson, object : TypeToken<List<Show>>() {}.type) as List<T>
                    MediaType.ANIME -> gson.fromJson(resultJson, object : TypeToken<List<Anime>>() {}.type) as List<T>
                }

                // Update each item's liked status based on its type.
                val updatedItems = fetchedItems.map { item ->
                    when (item) {
                        is Movie -> {
                            item.isLiked = likedItems.filterIsInstance<Movie>().any { it.id == item.id }
                            item
                        }
                        is Show -> {
                            item.isLiked = likedItems.filterIsInstance<Show>().any { it.id == item.id }
                            item
                        }
                        is Anime -> {
                            item.isLiked = likedItems.filterIsInstance<Anime>().any { it.id == item.id }
                            item
                        }
                        else -> item
                    }
                }

                withContext(Dispatchers.Main) {
                    adapter.addMediaItems(updatedItems)
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error handling media result for $mediaType: ${e.message}", e)
            }
        }
    }

    /**
     * Marks the loading process as complete by setting the isLoading flag to false.
     */
    private fun onLoadingComplete() {
        Log.d("HomeActivity", "Loading process completed.")
        isLoading.set(false)
    }

    /**
     * Toggles the liked status of a given media item and updates the UI accordingly.
     *
     * @param item The media item to toggle liked status for.
     */
    private fun <T> toggleLike(item: T) {
        lifecycleScope.launch {
            try {
                likeButtonUtils.toggleLikeToItem(item as Any)
                when (item) {
                    is Movie -> item.isLiked = !item.isLiked
                    is Show -> item.isLiked = !item.isLiked
                    is Anime -> item.isLiked = !item.isLiked
                }

                withContext(Dispatchers.Main) {
                    when (item) {
                        is Movie -> moviesAdapter.updateLikeStatus(item)
                        is Show -> tvShowsAdapter.updateLikeStatus(item)
                        is Anime -> animeAdapter.updateLikeStatus(item)
                    }
                    Log.d("HomeActivity", "Toggled like status for item: $item")
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error toggling like status: ${e.message}", e)
            }
        }
    }

    /**
     * Refreshes the liked status of all media items by checking the persisted liked items.
     */
    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            try {
                val likedMovies = likeButtonUtils.getLikedMovies()
                val likedShows = likeButtonUtils.getLikedShows()
                val likedAnime = likeButtonUtils.getLikedAnime()

                withContext(Dispatchers.Main) {
                    moviesAdapter.refreshLikedStatus(likedMovies.map { it.id })
                    tvShowsAdapter.refreshLikedStatus(likedShows.map { it.id })
                    animeAdapter.refreshLikedStatus(likedAnime.map { it.id })
                    Log.d("HomeActivity", "Successfully refreshed liked statuses.")
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error refreshing liked statuses: ${e.message}", e)
            }
        }
    }

    /**
     * Sets up the bottom navigation bar and configures navigation between different activities.
     */
    private fun setupBottomNavigation() {
        try {
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
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error setting up bottom navigation: ${e.message}", e)
        }
    }
}


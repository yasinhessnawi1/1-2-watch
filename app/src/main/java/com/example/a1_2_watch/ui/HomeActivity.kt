package com.example.a1_2_watch.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
import com.example.a1_2_watch.workers.FetchMediaWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HomeActivity is the main activity for this application. It displays lists of popular movies, TV shows,
 * and anime. It also handles pagination of popular media, like functionality, expand button functionality,
 * and navigation between all activities.
 */
class HomeActivity : AppCompatActivity() {
    // Binding object for accessing views in the layout.
    private lateinit var binding: HomeLayoutBinding
    // Media Adapter for managing and displaying a list of movies items in the RecyclerView.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    // Media Adapter for managing and displaying a list of TV show items in the RecyclerView.
    private lateinit var tvShowsAdapter: MediaAdapter<Show>
    // Media Adapter for managing and displaying a list of anime items in the RecyclerView.
    private lateinit var animeAdapter: MediaAdapter<Anime>
    // LikeButtonUtils for managing like functionality.
    private lateinit var likeButtonUtils: LikeButtonUtils
    // Variable used to track the current page for pagination data loading.
    private var currentPage = 1
    // Boolean indicating whether data is being loaded or not.
    private val isLoading = AtomicBoolean(false)
    // Limit for anime items per page
    private val animeLimit = 10
    // Gson instance for JSON parsing
    private val gson = Gson()

    /**
     * This function is called when the activity is created, and initializes the activity layout,
     * RecyclerViews, and the bottom navigation bar.
     *
     * @param savedInstanceState Used to restore the activity's previously saved state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create binding with the Home layout view
        binding = HomeLayoutBinding.inflate(layoutInflater)
        // Sets the content view to the Home layout.
        setContentView(binding.root)
        // Sets up the RecyclerViews for displaying lists of movies, shows, and anime.
        setupRecyclerViews()

        likeButtonUtils = LikeButtonUtils(this)
        // Fetch data for all categories on activity start
        fetchAllMedia()
        // Sets up the bottom navigation bar for navigation between activities.
        setupBottomNavigation()
    }

    /**
     * This function called when the activity is resumed, and sets the selected navigation item
     * and refreshes the liked status of displayed media items.
     */
    override fun onResume() {
        super.onResume()
        // Sets the bottom navigation bar to the Home screen
        binding.bottomNavigationView.selectedItemId = R.id.home
        // Updates the liked status of displayed media items
        refreshLikedStatus()
    }

    /**
     * This function sets up RecyclerViews and adapters for movies, TV shows, and anime lists.
     * It also sets up click listeners for navigation, like functionality, and expand button functionality.
     */
    private fun setupRecyclerViews() {
        // Set up Movies RecyclerView and its adapter.
        moviesAdapter = MediaAdapter(
            context = this,
            onItemClick = { movie ->
                // Handle movie item click and navigate to the details page of selected movie.
                NavigationUtils.navigateToDetails(this, movie.id, MediaType.MOVIES.name)
            },
            onSaveClick = { movie ->
                // Toggles the liked status of the selected movie.
                toggleLike(movie)
            },
        )
        // Sets the layout manager for displaying items horizontally in the movies RecyclerView.
        binding.moviesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Sets the adapter for the movies RecyclerView.
        binding.moviesRecyclerView.adapter = moviesAdapter
        // Enables pagination for the movies RecyclerView.
        setupPaginationForRecyclerView(binding.moviesRecyclerView, MediaType.MOVIES)

        // Set up TV Shows RecyclerView and its adapter.
        tvShowsAdapter = MediaAdapter(
            context = this,
            onItemClick = { show ->
                // Handle TV show item click and navigate to the details page of selected show.
                NavigationUtils.navigateToDetails(this, show.id, MediaType.TV_SHOWS.name)
            },
            onSaveClick = { show ->
                // Toggles the liked status of the selected TV show.
                toggleLike(show)
            }
        )
        // Sets the layout manager for displaying items horizontally in the TV shows RecyclerView.
        binding.tvShowsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Sets the adapter for the TV shows RecyclerView.
        binding.tvShowsRecyclerView.adapter = tvShowsAdapter
        // Enables pagination for the TV shows RecyclerView.
        setupPaginationForRecyclerView(binding.tvShowsRecyclerView, MediaType.TV_SHOWS)

        // Set up Anime RecyclerView and its adapter.
        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { anime ->
                // Handle anime item click and navigate to the details page of selected anime.
                NavigationUtils.navigateToDetails(this, anime.id, MediaType.ANIME.name)
            },
            onSaveClick = { anime ->
                // Toggles the liked status of the selected anime.
                toggleLike(anime)
            }
        )
        // Sets the layout manager for displaying items horizontally in the anime RecyclerView.
        binding.animeRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Sets the adapter for the anime RecyclerView.
        binding.animeRecyclerView.adapter = animeAdapter
        // Enables pagination for the anime RecyclerView.
        setupPaginationForRecyclerView(binding.animeRecyclerView, MediaType.ANIME)
    }

    /**
     * This function sets up pagination for a given RecyclerView based on media type, and loads
     * additional items when the user scrolls to the end of the list.
     *
     * @param recyclerView The RecyclerView to set up pagination for.
     * @param mediaType The type of media (movies, shows, or anime) for pagination.
     */
    private fun setupPaginationForRecyclerView(recyclerView: RecyclerView, mediaType: MediaType) {
        // Adds the scroll listener to the RecyclerView to detect when the user reaches the end of the list.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /**
             * This function is called when the RecyclerView has been scrolled.
             *
             * @param recyclerView The RecyclerView that has been scrolled.
             * @param dx The amount of horizontal scrolling.
             * @param dy The amount of vertical scrolling.
             */
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Gets the LinearLayoutManager associated with the RecyclerView.
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                // Gets the total number of items in the RecyclerView.
                val totalItemCount = layoutManager.itemCount
                // Finds the position of the last visible item in the RecyclerView.
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                // Checks if data is not loading and if the user scrolled to the last item.
                if (!isLoading.get() && lastVisibleItemPosition == totalItemCount - 1) {
                    // Sets the loading state to true to prevent duplicate data requests.
                    isLoading.set(true)
                    // Increments the current page counter for pagination.
                    currentPage++

                    // Fetch more data based on media type
                    when (mediaType) {
                        // Loads more movies if mediaType is MOVIES.
                        MediaType.MOVIES -> {
                            fetchMovies()
                        }
                        // Loads more TV shows if mediaType is TV_SHOWS.
                        MediaType.TV_SHOWS -> {
                            fetchTVShows()
                        }
                        // Loads more anime if mediaType is ANIME.
                        MediaType.ANIME -> {
                            fetchAnime()
                        }
                    }
                }
            }
        })
    }

    /**
     * This function sets the loading flag to true to prevent duplicate requests, and initiates data
     * fetching for all media types (movies, TV shows, anime).
     */
    private fun fetchAllMedia() {
        isLoading.set(true)
        fetchMovies()
        fetchTVShows()
        fetchAnime()
    }

    /**
     * This function fetches popular movies using WorkManager and updates the adapter with liked
     * status.
     */
    private fun fetchMovies() {
        val inputData = workDataOf(
            FetchMediaWorker.KEY_MEDIA_TYPE to MediaType.MOVIES.name,
            FetchMediaWorker.KEY_PAGE to currentPage
        )

        val fetchMediaWork = OneTimeWorkRequestBuilder<FetchMediaWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(fetchMediaWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchMediaWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchMediaWorker.KEY_RESULT)
                        handleMoviesResult(resultJson)
                    }
                    onLoadingComplete()
                }
            })
    }

    /**
     * This function handles the result from FetchMediaWorker for movies and updates the UI.
     *
     * @param resultJson The JSON string containing the fetched movies.
     */
    private fun handleMoviesResult(resultJson: String?) {
        if (resultJson == null) return
        val type = object : TypeToken<List<Movie>>() {}.type
        val fetchedMovies: List<Movie> = gson.fromJson(resultJson, type)
        lifecycleScope.launch {
            val likedMovies = likeButtonUtils.getLikedMovies()
            // Update each movie's liked status
            val updatedMovies = fetchedMovies.map { movie ->
                movie.isLiked = likedMovies.any { likedMovie -> likedMovie.id == movie.id }
                movie
            }
            withContext(Dispatchers.Main) {
                moviesAdapter.addMediaItems(updatedMovies)
            }
        }
    }


    /**
     * This function fetches popular TV shows using WorkManager and updates the adapter with liked
     * status.
     */
    private fun fetchTVShows() {
        val inputData = workDataOf(
            FetchMediaWorker.KEY_MEDIA_TYPE to MediaType.TV_SHOWS.name,
            FetchMediaWorker.KEY_PAGE to currentPage
        )

        val fetchMediaWork = OneTimeWorkRequestBuilder<FetchMediaWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(fetchMediaWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchMediaWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchMediaWorker.KEY_RESULT)
                        handleTVShowsResult(resultJson)
                    }
                    onLoadingComplete()
                }
            })
    }

    /**
     * This function handles the result from FetchMediaWorker for TV shows and updates the UI.
     *
     * @param resultJson The JSON string containing the fetched TV shows.
     */
    private fun handleTVShowsResult(resultJson: String?) {
        if (resultJson == null) return
        val type = object : TypeToken<List<Show>>() {}.type
        val fetchedShows: List<Show> = gson.fromJson(resultJson, type)
        lifecycleScope.launch {
            val likedShows = likeButtonUtils.getLikedShows()
            val updatedShows = fetchedShows.map { show ->
                show.isLiked = likedShows.any { likedShow -> likedShow.id == show.id }
                show
            }
            withContext(Dispatchers.Main) {
                tvShowsAdapter.addMediaItems(updatedShows)
            }
        }
    }


    /**
     * This function fetches popular anime using WorkManager and updates the adapter with liked
     * status.
     */
    private fun fetchAnime() {
        val inputData = workDataOf(
            FetchMediaWorker.KEY_MEDIA_TYPE to MediaType.ANIME.name,
            FetchMediaWorker.KEY_PAGE to currentPage,
            FetchMediaWorker.KEY_RESULT to animeLimit
        )

        val fetchMediaWork = OneTimeWorkRequestBuilder<FetchMediaWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(fetchMediaWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchMediaWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchMediaWorker.KEY_RESULT)
                        handleAnimeResult(resultJson)
                    }
                    onLoadingComplete()
                }
            })
    }

    /**
     * This function handles the result from FetchMediaWorker for anime and updates the UI.
     *
     * @param resultJson The JSON string containing the fetched anime.
     */
    private fun handleAnimeResult(resultJson: String?) {
        if (resultJson == null) return
        val type = object : TypeToken<List<Anime>>() {}.type
        val fetchedAnime: List<Anime> = gson.fromJson(resultJson, type)
        lifecycleScope.launch {
            val likedAnime = likeButtonUtils.getLikedAnime()
            val updatedAnime = fetchedAnime.map { anime ->
                anime.isLiked = likedAnime.any { likedItem -> likedItem.id == anime.id }
                anime
            }
            withContext(Dispatchers.Main) {
                animeAdapter.addMediaItems(updatedAnime)
            }
        }
    }




    /**
     * This function marks the loading process as complete by setting the isLoading flag to false.
     */
    private fun onLoadingComplete() {
        isLoading.set(false)
    }

    /**
     * This function sets up the bottom navigation bar, configures each tab with its navigation behavior.
     */
    private fun setupBottomNavigation() {
        // Sets the default selected item to the Home tab.
        binding.bottomNavigationView.selectedItemId = R.id.home
        // Configures the bottom navigation bar's item selection listener.
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Keeps the user on home screen if home is selected.
                R.id.home -> true
                R.id.user -> {
                    // Navigates to the user tab if the user is selected.
                    NavigationUtils.navigateToUser(this)
                    true
                }
                R.id.discover -> {
                    // Navigates to the discover tab if the discover is selected.
                    NavigationUtils.navigateToDiscover(this)
                    true
                }
                // No navigation for any other cases.
                else -> false
            }
        }
    }

    /**
     * This function toggles the liked status of an item and updates the UI accordingly.
     *
     * @param item The media item to toggle liked status for.
     */
    private fun toggleLike(item: Any) {
        lifecycleScope.launch {
            // Toggle like status of the item
            likeButtonUtils.toggleLikeToItem(item)

            // Update the isLiked status of the item
            when (item) {
                is Movie -> item.isLiked = !item.isLiked
                is Show -> item.isLiked = !item.isLiked
                is Anime -> item.isLiked = !item.isLiked
            }

            // Update the UI after toggling on the main thread
            withContext(Dispatchers.Main) {
                when (item) {
                    is Movie -> moviesAdapter.updateLikeStatus(item)
                    is Show -> tvShowsAdapter.updateLikeStatus(item)
                    is Anime -> animeAdapter.updateLikeStatus(item)
                }
            }
        }
    }



    /**
     * This function refreshes the liked status of media items in each adapter by checking which items
     * are marked as liked in the SharedPreferences.
     */
    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            val likedMovies = likeButtonUtils.getLikedMovies()
            withContext(Dispatchers.Main) {
                moviesAdapter.refreshLikedStatus(likedMovies.map { it.id })
            }
        }
    }



}
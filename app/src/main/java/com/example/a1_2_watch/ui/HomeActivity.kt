package com.example.a1_2_watch.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.HomeLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.MediaRepository
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HomeActivity The main activity for this application, it displays lists of popular movies, TV shows
 * and anime. It also handles pagination of popular media, like functionality, and navigation between
 * all activities.
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
    private val likeButtonUtils = LikeButtonUtils(this)
    // MediaRepository instance for fetching media items form APIs.
    private val mediaRepository = MediaRepository()
    // Variable used to track the current page for pagination data loading.
    private var currentPage = 1
    // Boolean indicating whether data is being loaded or not.
    private var isLoading = false
    // Limit for anime items per page
    private val animeLimit = 20
    // SharedPreferences instance for storing liked items, and initialized lazily for efficiency.
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }

    /**
     * This functions is called when the activity is created, and initialized the activity layout,
     * RecyclingView, and the bottom navigation bar.
     *
     * @param savedInstanceState Used to restore the activity's previously saved state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create binding with the Home layout view
        binding = HomeLayoutBinding.inflate(layoutInflater)
        // Sets the content view to the Home layout.
        setContentView(binding.root)
        // Sets up the RecyclerView for displaying lists of movies, shows, and anime.
        setupRecyclerViews()
        // Fetch data for all categories on activity start
        fetchAllMedia()
        // Sets up the bottom navigation bar for navigation between Activities.
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
     * This function sets up RecyclerView and adapters for movies, TV shows, and anime lists.
     * It also sets up click listeners for navigation and like functionality.
     */
    private fun setupRecyclerViews() {
        // Set up Movies RecyclerView and his adapter.
        moviesAdapter = MediaAdapter(
            context = this,
            onItemClick = { movie ->
                // Handle movie item click and navigates to the details page of selected movie.
                NavigationUtils.navigateToDetails(this, movie.id, MediaType.MOVIES.name)
            },
            onSaveClick = { movie ->
                // Toggles the liked status of the selected movie.
                toggleLike(movie)
            }
        )
        // Sets the layout manager for displaying items horizontally in the movies RecyclerView.
        binding.moviesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Sets the adapter for the movies RecyclerView.
        binding.moviesRecyclerView.adapter = moviesAdapter
        // Enables pagination for the movies RecyclerView.
        setupPaginationForRecyclerView(binding.moviesRecyclerView, MediaType.MOVIES)

        // Set up TV Shows RecyclerView and his adapter.
        tvShowsAdapter = MediaAdapter(
            context = this,
            onItemClick = { show ->
                // Handle TV show item click and navigates to the details page of selected show.
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

        // Set up Anime RecyclerView and his adapter.
        animeAdapter = MediaAdapter(
            context = this,
            onItemClick = { anime ->
                // Handle anime item click and navigates to the details page of selected anime.
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
     * additional items when the user scrolls to the end of list.
     *
     * @param recyclerView The RecyclerView to set up pagination for.
     * @param mediaType The type of media (movies, shows, or anime) for pagination.
     */
    private fun setupPaginationForRecyclerView(recyclerView: RecyclerView, mediaType: MediaType) {
        // Adds the scroll listener to the RecyclerView to detect when the user reaches the end of list.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            /**
             * This function called when the RecyclerView has been scrolled.
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
                // Checks if the data is not loading and if the user scrolls to the last item.
                if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                    // Sets the loading state to true to prevent duplicate data requests.
                    isLoading = true
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
        isLoading = true
        fetchMovies()
        fetchTVShows()
        fetchAnime()
    }

    /**
     * This function fetches popular movies from the repository and updates the adapter with liked
     * status.
     */
    private fun fetchMovies() {
        // Launches a coroutine tied to the activity’s lifecycle
        lifecycleScope.launch {
            val gson = Gson()
            // Gets the liked movies titles from the SharedPreferences on the IO thread.
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> = gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                // Gets the titles from liked movies list.
                likedMovies.map { it.title }
            }
            // Get the popular movies list based on current page number.
            val fetchedMovies = mediaRepository.fetchPopularMovies(currentPage)
            // Updates each movie's liked status if its title matches any LikedMoviesTitles.
            val updatedMovies = fetchedMovies.map { movie ->
                movie.isLiked = likedMoviesTitles.contains(movie.title)
                movie
            }
            // Adds the updated movies to the adapter on the main thread and marks loading as complete.
            withContext(Dispatchers.Main) {
                moviesAdapter.addMediaItems(updatedMovies)
                onLoadingComplete()
            }
        }
    }

    /**
     * This function fetches popular TV shows from the repository and updates the adapter with liked
     * status.
     */
    private fun fetchTVShows() {
        // Launches a coroutine tied to the activity’s lifecycle
        lifecycleScope.launch {
            val gson = Gson()
            // Gets the liked TV show names from the SharedPreferences on the IO thread.
            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> = gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                // Gets the names from liked TV shows list.
                likedShows.map { it.name }
            }
            // Get the popular TV shows list based on current page number.
            val fetchedShows = mediaRepository.fetchPopularTVShows(currentPage)
            // Updates each show's liked status if its name matches any LikedTVShowNames.
            val updatedShows = fetchedShows.map { show ->
                show.isLiked = likedShowsNames.contains(show.name)
                show
            }
            // Adds the updated TV Shows to the adapter on the main thread and marks loading as complete.
            withContext(Dispatchers.Main) {
                tvShowsAdapter.addMediaItems(updatedShows)
                onLoadingComplete()
            }
        }
    }

    /**
     * This function fetches popular anime from the repository and updates the adapter with liked
     * status.
     */
    private fun fetchAnime() {
        // Launches a coroutine tied to the activity’s lifecycle
        lifecycleScope.launch {
            val gson = Gson()
            // Gets the liked anime titles from the SharedPreferences on the IO thread.
            val likedAnimeTitles = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> = gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                // Gets the titles from liked anime list.
                likedAnime.map { it.attributes.canonicalTitle }
            }
            // Get the popular anime list based on current page number.
            val fetchedAnime = mediaRepository.fetchPopularAnime(currentPage, animeLimit)
            // Updates each anime's liked status if its title matches any LikedAnimeTitle.
            val updatedAnime = fetchedAnime.map { anime ->
                anime.isLiked = likedAnimeTitles.contains(anime.attributes.canonicalTitle)
                anime
            }
            // Adds the updated anime to the adapter on the main thread and marks loading as complete.
            withContext(Dispatchers.Main) {
                animeAdapter.addMediaItems(updatedAnime)
                onLoadingComplete()
            }
        }
    }

    /**
     * This function marks loading process as complete by setting the isLoading flag to false.
     */
    private fun onLoadingComplete() {
        isLoading = false
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
                    // Navigates to the discover tab if the discover is selected
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
        lifecycleScope.launch(Dispatchers.IO) {
            // Toggles the like status of the item in the shared preferences.
            likeButtonUtils.toggleLikeToItem(item)
            // Updates the UI on the main thread after toggling.
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
     * are marked as liked in the sharedPreferences.
     */
    private fun refreshLikedStatus() {
        lifecycleScope.launch {
            val gson = Gson()
            // Gets liked movies title from the sharedPreferences on the IO thread.
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                // Extract and return a list of movie titles from the liked movies list, filtering out null titles.
                likedMovies.mapNotNull { it.title }
            }
            // Updates the movies adapter with the list of liked movie titles to refresh UI.
            moviesAdapter.refreshLikedStatus(likedMoviesTitles)

            // Gets liked Tv show names from the sharedPreferences on the IO thread.
            val likedShowsNames = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                // Extract and return a list of TV show names from the liked TV show list, filtering out null names.
                likedShows.mapNotNull { it.name }
            }
            // Updates the TV shows adapter with the list of liked TV show names to refresh UI.
            tvShowsAdapter.refreshLikedStatus(likedShowsNames)

            // Gets liked anime title from the sharedPreferences on the IO thread.
            val likedAnimeTitles = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                // Extract and return a list of anime titles from the liked anime list.
                likedAnime.map { it.attributes.canonicalTitle }
            }
            // Updates the anime adapter with the list of liked anime titles to refresh UI.
            animeAdapter.refreshLikedStatus(likedAnimeTitles)
        }
    }
}
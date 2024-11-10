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

/**
 * DiscoverActivity displays and manages a discover page that includes many functionality like
 * searching for any media item (movies, tv shows, and anime), displaying related media items based
 * on user's liked media, and allowing users to like or unlike items.
 */
class DiscoverActivity : AppCompatActivity() {
    // Create Binding object for accessing UI elements in the discover layout.
    private lateinit var binding: DiscoverLayoutBinding
    // DiscoverRepository for handling search queries.
    private val searchRepository = DiscoverRepository()
    // MediaRepository for handling media related retrieval.
    private val mediaRepository = MediaRepository()
    // MediaAdapter for displaying movies in a horizontal RecycleView.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    // MediaAdapter for displaying TV Shows in a horizontal RecycleView.
    private lateinit var showsAdapter: MediaAdapter<Show>
    // MediaAdapter for displaying anime in a horizontal RecycleView.
    private lateinit var animeAdapter: MediaAdapter<Anime>
    // DiscoverAdapter for displaying search results in a vertical RecycleView.
    private val discoverAdapter = DiscoverAdapter(onItemClick = { item ->
        // Manage different item types.
        handleItemClick(item)
    })
    // sharedPreferences instance for storing liked items from persistent storage.
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }
    // Creates likeButtonUtils for managing liked items.
    private val likeButtonUtils = LikeButtonUtils(this)

    /**
     * This function initializes the activity, sets up view bindings, RecyclerView, adapters, loads
     * liked items, and configures bottom navigation.
     *
     * @param savedInstanceState Used to restore the activity's previously saved state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create binding with the Discover layout view
        binding = DiscoverLayoutBinding.inflate(layoutInflater)
        // Sets the content view to the Discover layout.
        setContentView(binding.root)
        // Set up the Go Back button and finish activity when back button is pressed
        binding.goBackButton.setOnClickListener {
            finish()
        }
        // Setup RecyclerViews and Adapters for displaying and media items and search results.
        setupRecyclerView()
        // Sets up the SearchView for handling search queries.
        setupSearchView()
        // Setup Bottom Navigation
        setupBottomNavigation()
        // Loads and displays related items based on the user's liked items.
        loadRelatedItems()
    }

    /**
     * This function called when the activity is resumed, and refreshes the liked status
     * of displayed media items.
     */
    override fun onResume() {
        super.onResume()
        // Updates the liked status of displayed media items
        refreshLikedStatus()
    }

    /**
     * setupRecyclerView sets up the RecyclerView to display different media types (movies, TV shows, or anime)
     * and search results. This function sets layout manager and adapter for each list.
     */
    private fun setupRecyclerView() {
        // Sets up the search results RecyclerView with vertical layout.
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity)
            // Assign the discoverAdapter to handle search results.
            adapter = discoverAdapter
            // Now hides the search results RecyclerView.
            visibility = View.GONE
        }
        // Sets up adapter for movies with click listeners
        moviesAdapter = MediaAdapter(
            context = this,
            // Handle movie item click and navigates to the details page of selected movie.
            onItemClick = { item -> handleItemClick(item) },
            // Toggles the liked status of the selected movie and refresh the UI.
            onSaveClick = { item -> toggleLike(item) },
            // True to allow API fetching for additional details.
            fetchDetailsFromAPI = true
        )
        // Sets the layout manager for displaying items horizontally in the liked movies RecyclerView.
        binding.moviesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        // Sets up adapter for TV Shows with click listeners
        showsAdapter = MediaAdapter(
            context = this,
            // Handle TV Show item click and navigates to the details page of selected TV Show.
            onItemClick = { item -> handleItemClick(item) },
            // Toggles the liked status of the selected TV Show and refresh the UI.
            onSaveClick = { item -> toggleLike(item) },
            // True to allow API fetching for additional details.
            fetchDetailsFromAPI = true
        )
        // Sets the layout manager for displaying items horizontally in the liked TV Shows RecyclerView.
        binding.tvShowsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }
        // Sets up adapter for anime with click listeners
        animeAdapter = MediaAdapter(
            context = this,
            // Handle anime item click and navigates to the details page of selected anime.
            onItemClick = { item -> handleItemClick(item) },
            // Toggles the liked status of the selected anime and refresh the UI.
            onSaveClick = { item -> toggleLike(item) },
            // True to allow API fetching for additional details.
            fetchDetailsFromAPI = true
        )
        // Sets the layout manager for displaying items horizontally in the liked anime RecyclerView.
        binding.animeRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@DiscoverActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }

    /**
     * This function handles the click events for the items in the related media list or on the
     * search results, this allow us to navigate to the details screen based on the media type.
     *
     * @param item The item clicked by the user (movie, TV show, or anime).
     */
    private fun handleItemClick(item: Any) {
        when (item) {
            // Navigate to the movie details screen
            is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
            // Navigate to the TV Show details screen
            is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
            // Navigate to the anime details screen
            is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
        }
    }

    /**
     * setupSearchView sets up the SearchView to listen for user input and preform searches.
     * This function manages the display and clearing of the search results.
     */
    private fun setupSearchView() {
        // Sets up the SearchView with query text listener.
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
             * This function called when the user inputs a search query.
             *
             * @param query The query text entered by the user.
             * @return Boolean Flag indicating whether the query submission was handled.
             */
            override fun onQueryTextSubmit(query: String?): Boolean {
                // If the query not null then make the search.
                query?.let { performSearch(it) }
                // Indicate that the query was handled.
                return true
            }

            /**
             * This function called when the text in the SearchView is changed, including when the user
             * types or deletes the character. If the query is empty this function clears the search results.
             *
             * @param newText The current search text in the SearchView input field.
             * @return Boolean Indicating whether the text change was handled
             */
            override fun onQueryTextChange(newText: String?): Boolean {
                // Trim whitespace from the query input.
                val trimmedQuery = newText?.trim() ?: ""
                if (trimmedQuery.isEmpty()) {
                    // Clears search results if the trimmed query is empty.
                    clearSearchResults()
                } else {
                    // Makes a search with the trimmed query
                    performSearch(trimmedQuery)
                }
                // Indicates that the text change was handled
                return true
            }
        })
    }

    /**
     * This function performs a search operation with the provided query, fetches the search results
     * and updating the search results view.
     *
     * @param query The search query entered by the user.
     */
    private fun performSearch(query: String) {
        // Remove any whitespace from the query.
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            // if the query is empty then clear the search results.
            clearSearchResults()
            // Ends the function execution.
            return
        }
        // Makes the search results view visible
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        // Brings the search results to the front of the view.
        binding.searchResultsRecyclerView.bringToFront()
        // Hides the related media list view.
        toggleRelatedListsVisibility(false)
        // An empty list to store the search results
        val searchResults = mutableListOf<Any>()
        // Makes a search for movies with the query and add the results to search Results.
        searchRepository.searchMovies(Constants.API_KEY, trimmedQuery) { movieResponse ->
            // Checks if the response contains results
            movieResponse?.results?.let { movies ->
                // Adds all found movies to search Results.
                searchResults.addAll(movies)
                // Updates the UI with the current search results.
                updateSearchResults(searchResults)
            }
        }
        // Makes a search for TV Shows with the query and add the results to search Results.
        searchRepository.searchTVShows(Constants.API_KEY, trimmedQuery) { showResponse ->
            // Checks if the response contains results
            showResponse?.results?.let { shows ->
                // Adds all found TV Shows to search Results.
                searchResults.addAll(shows)
                // Updates the UI with the current search results.
                updateSearchResults(searchResults)
            }
        }
        // Makes a search for TV Shows with the query and add the results to search Results.
        searchRepository.searchAnime(trimmedQuery) { animeResponse ->
            // Checks if the response contains results
            animeResponse?.data?.let { animes ->
                // Adds all found TV Shows to search Results.
                searchResults.addAll(animes)
                // Updates the UI with the current search results.
                updateSearchResults(searchResults)
            }
        }
    }

    /**
     * This function updates the search results display in the RecyclerView.
     *
     * @param results A list of media items found in the search.
     */
    private fun updateSearchResults(results: List<Any>) {
        // Updates the item in the discoverAdapter with the provided search results.
        discoverAdapter.updateItems(results)
    }

    /**
     * This function clears the search results from the view, hides the search results RecyclerView
     * and re-displaying the related media items list.
     */
    private fun clearSearchResults() {
        // Clear the discoverAdapter with an empty list.
        discoverAdapter.updateItems(emptyList())
        // Hides the search results RecyclerView from the UI.
        binding.searchResultsRecyclerView.visibility = View.GONE
        // Shows the related media items list again.
        toggleRelatedListsVisibility(true)
    }

    /**
     * This function toggles the visibility of the related media items based on provider boolean value.
     *
     * @param isVisible Boolean value indicating if the related lists should be visible or not.
     */
    private fun toggleRelatedListsVisibility(isVisible: Boolean) {
        // Makes the visibility to VISIBLE if isVisible true.
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        // Applies the visibility setting to each of these UI components.
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

    /**
     * loadRelatedItems loads the related items for the user's liked media by retrieving liked movies,
     * TV Show, and anime. This function coroutines to fetch related items asynchronously and displays
     * them in the UI.
     */
    private fun loadRelatedItems() {
        // Lifecycle scope to handle background operations.
        lifecycleScope.launch {
            // Gets a list of liked movies, TV Show, and anime from shared preferences.
            val likedMovies = getLikedMovies()
            val likedTVShows = getLikedTVShows()
            // Fetches the related movies, TV shows, and anime based on the liked items.
            val relatedMovies = fetchRelatedMovies(likedMovies)
            val relatedTVShows = fetchRelatedTVShows(likedTVShows)
            val relatedAnime = fetchRelatedAnime()
            // Displays the fetched related items in the UI.
            displayRelatedItems(relatedMovies, relatedTVShows, relatedAnime)
        }
    }

    /**
     * fetches the list of movies liked by the user from the shared preferences.
     *
     * @return A List of movie objects representing the user's favorite movies.
     */
    private suspend fun getLikedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        // Gets the list of liked movies from the shared preferences
        val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
        // Parses the JSON string to a list of movie (List<Movie>) using Gson.
        Gson().fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
    }

    /**
     * fetches the list of TV Shows liked by the user from the shared preferences.
     *
     * @return A List of TV Show objects representing the user's favorite TV Shows.
     */
    private suspend fun getLikedTVShows(): List<Show> = withContext(Dispatchers.IO) {
        // Gets the list of liked TV Shows from the shared preferences
        val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
        // Parses the JSON string to a list of TV Show (List<Show>) using Gson.
        Gson().fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
    }

    /**
     * This function fetches the list of movies related to the user's liked movies by iterating
     * through the liked movies and fetching related movies for each movie.
     *
     * @param likedMovies A list of movies objects liked by the user.
     * @return A list of related movies objects, up to 10 for each movie.
     */
    private suspend fun fetchRelatedMovies(likedMovies: List<Movie>): List<Movie> {
        // Create an empty list of store related movies.
        val relatedMovies = mutableListOf<Movie>()
        // Iterate through each liked movie and fetch related movies.
        for (movie in likedMovies) {
            // Gets the related movies for the given movie ID.
            val related = mediaRepository.fetchRelatedMovies(movie.id).map { relatedMovie ->
                // Set the liked status using isItemLiked
                relatedMovie.isLiked = likeButtonUtils.isItemLiked(relatedMovie)
                relatedMovie
            }
            // Adds up to 10 related movies to the list.
            relatedMovies.addAll(related.take(10))
        }
        // Returns the list of related movies.
        return relatedMovies
    }

    /**
     * This function fetches the list of TV Shows related to the user's liked TV Shows by iterating
     * through the liked TV Shows and fetching related TV shows for each movie.
     *
     * @param likedTVShows A list of TV Shows objects liked by the user.
     * @return A list of related TV Shows objects, up to 10 for each TV Show.
     */
    private suspend fun fetchRelatedTVShows(likedTVShows: List<Show>): List<Show> {
        // Create an empty list of store related TV Shows.
        val relatedShows = mutableListOf<Show>()
        // Iterate through each liked TV Show and fetch related TV Shows.
        for (show in likedTVShows) {
            // Gets the related TV Shows for the given TV Show ID.
            val related = mediaRepository.fetchRelatedTVShows(show.id).map { relatedShow ->
                // Set the liked status using isItemLiked
                relatedShow.isLiked = likeButtonUtils.isItemLiked(relatedShow)
                relatedShow
            }
            // Adds up to 10 related TV Shows to the list.
            relatedShows.addAll(related.take(10))
        }
        // Returns the list of related TV Shows.
        return relatedShows
    }

    /**
     * This function fetches related anime based on the user's liked anime by identifying
     * distinct subtypes of anime and fetching related anime based on each subtype.
     *
     * @return A list of related anime objects, up to 10 for each subtype.
     */
    private suspend fun fetchRelatedAnime(): List<Anime> {
        // Gets the liked anime JSON string from the sharedPreferences, and empty list if null.
        val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
        val likedAnime: List<Anime> =
            Gson().fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
        // Extract distinct anime subtypes from liked anime to use them for fetching related anime.
        val animeTypes = likedAnime.map { it.attributes.subtype }.distinct()
        // Create an empty list to store the related anime objects.
        val relatedAnime = mutableListOf<Anime>()
        // Iterate through each distinct anime subtype to fetch the related anime.
        for (type in animeTypes) {
            // Gets the related anime based on subtype.
            val related = mediaRepository.fetchAnimeByType(type).map { relatedAnimeItem ->
                // Set the liked status using isItemLiked
                relatedAnimeItem.isLiked = likeButtonUtils.isItemLiked(relatedAnimeItem)
                relatedAnimeItem
            }
            // Adds up to 10 related anime to the list.
            relatedAnime.addAll(related.take(10))
        }
        // Returns the complete list of related anime.
        return relatedAnime
    }

    /**
     * This function displays related media items by updating the adapter for movies, TV Shows,
     * and anime. Also toggles visibility of empty text views if no items are found.
     *
     * @param movies A list of movie objects to display.
     * @param shows A list of TV Show objects to display.
     * @param anime A list of anime objects to display.
     */
    private fun displayRelatedItems(movies: List<Movie>, shows: List<Show>, anime: List<Anime>) {
        // Updates the movies, shows, and anime adapter with related list.
        moviesAdapter.setMediaList(movies)
        showsAdapter.setMediaList(shows)
        animeAdapter.setMediaList(anime)
        // Sets the visibility of empty text views if no items are found
        binding.emptyMoviesTextView.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyTvShowsTextView.visibility = if (shows.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyAnimeTextView.visibility = if (anime.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * This function toggles the liked status of an item and updates the UI accordingly.
     *
     * @param item The media item to toggle liked status for.
     */
    private fun toggleLike(item: Any) {
        // Lifecycle scope to handle background operations.
        lifecycleScope.launch(Dispatchers.IO) {
            // Toggles the like status of the item in the shared preferences.
            LikeButtonUtils(this@DiscoverActivity).toggleLikeToItem(item)

            // Updates the UI on the main thread after toggling.
            withContext(Dispatchers.Main) {
                when (item) {
                    // Refreshes movie, show, and anime like status.
                    is Movie -> moviesAdapter.updateLikeStatus(item)
                    is Show -> showsAdapter.updateLikeStatus(item)
                    is Anime -> animeAdapter.updateLikeStatus(item)
                }
            }
        }
    }

    /**
     * This function refreshes the liked status for all media items by loading the liked titles from
     * shared preferences and updating the adapters.
     */
    private fun refreshLikedStatus() {
        // Lifecycle scope to handle background operations.
        lifecycleScope.launch {
            val gson = Gson()
            // Gets liked movie titles from shared preferences.
            val likedMoviesTitles = withContext(Dispatchers.IO) {
                // Gets JSON string of liked movies.
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                // Extract non null titles from the list of liked movies
                likedMovies.mapNotNull { it.title }
            }
            // Updates the movies adapter with the list of liked movie titles.
            moviesAdapter.refreshLikedStatus(likedMoviesTitles)

            // Gets liked TV Show names from shared preferences.
            val likedShowsNames = withContext(Dispatchers.IO) {
                // Gets JSON string of liked TV Shows.
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                // Extract non null names from the list of liked TV Shows.
                likedShows.mapNotNull { it.name }
            }
            // Updates the TV Shows adapter with the list of liked TV Show names.
            showsAdapter.refreshLikedStatus(likedShowsNames)

            // Gets liked anime titles from shared preferences.
            val likedAnimeTitles = withContext(Dispatchers.IO) {
                // Gets JSON string of liked anime.
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                // Extract non null titles from the list of liked anime
                likedAnime.map { it.attributes.canonicalTitle }
            }
            // Updates the anime adapter with the list of liked anime titles.
            animeAdapter.refreshLikedStatus(likedAnimeTitles)
        }
    }

    /**
     * This function sets up the bottom navigation bar, configures each tab with its navigation behavior.
     */
    private fun setupBottomNavigation() {
        // Sets the default selected item to the Discover tab.
        binding.bottomNavigationView.selectedItemId = R.id.discover
        // Configures the bottom navigation bar's item selection listener.
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Navigates to the Home tab if the Home is selected.
                    NavigationUtils.navigateToHome(this)
                    true
                }
                R.id.user -> {
                    // Navigates to the User tab if the User is selected.
                    NavigationUtils.navigateToUser(this)
                    true
                }
                // Keeps the user on Discover screen if discover icon is selected.
                R.id.discover -> true
                // No navigation for any other cases.
                else -> false
            }
        }
    }
}
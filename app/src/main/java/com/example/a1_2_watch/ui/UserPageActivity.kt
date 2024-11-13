package com.example.a1_2_watch.ui

import android.content.Context
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
import com.example.a1_2_watch.utils.LikeButtonUtils

/**
 * UserPageActivity displays a list of media items that the user has marked as liked.
 * This activity provides functionality to view and manage liked media items.
 */
class UserPageActivity : AppCompatActivity() {
    // Binds the user page layout for accessing UI components.
    private lateinit var binding: UserPageLayoutBinding
    // Creates adapter for displaying a list of liked movies.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    // Creates adapter for displaying a list of liked TV Shows.
    private lateinit var showsAdapter: MediaAdapter<Show>
    // Creates adapter for displaying a list of liked anime.
    private lateinit var animeAdapter: MediaAdapter<Anime>
    // SharedPreferences instance for storing and retrieving liked items from persistent storage.
    private val sharedPreferences by lazy {
        getSharedPreferences("liked_items", Context.MODE_PRIVATE)
    }
    // LikeButtonUtils for managing like functionality.
    private val likeButtonUtils = LikeButtonUtils(this)

    /**
     * This function initializes the activity, sets up view bindings, RecyclerViews, adapters, loads
     * liked items, and configures bottom navigation.
     *
     * @param savedInstanceState Used to restore the activity's previously saved state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create binding with the User layout view
        binding = UserPageLayoutBinding.inflate(layoutInflater)
        // Sets the content view to the User layout.
        setContentView(binding.root)
        // Set up the Go Back button and finish activity when back button is pressed
        binding.goBackButton.setOnClickListener {
            finish()
        }
        // Setup RecyclerViews and Adapters for displaying and managing liked items.
        setupAdapters()
        // Load the user's liked items.
        loadLikedItems()
        // Setup Bottom Navigation
        setupBottomNavigation()
    }

    /**
     * This function called when the activity is resumed, and reloads liked items every time the
     * activity is resumed to ensure it shows the latest data.
     */
    override fun onResume() {
        super.onResume()
        // Reload liked items to update any changes made on other screens.
        loadLikedItems()
    }

    /**
     * This function sets up adapters for each media type and sets up RecyclerViews for horizontal
     * scrolling.
     */
    private fun setupAdapters() {
        // Variable for holding the context.
        val context = this
        // Sets up adapter for movies with click listeners
        moviesAdapter = MediaAdapter(
            context = context,
            onItemClick = { movie ->
                NavigationUtils.navigateToDetails(this, movie.id, MediaType.MOVIES.name)
            },
            onSaveClick = { toggleLikeAndRefresh(it) },
            onExpandClick = { expandItem(it) }
        )
        // Sets up adapter for TV Shows with click listeners
        showsAdapter = MediaAdapter(
            context = context,
            onItemClick = { show ->
                NavigationUtils.navigateToDetails(this, show.id, MediaType.TV_SHOWS.name)
            },
            onSaveClick = { toggleLikeAndRefresh(it) },
            onExpandClick = { expandItem(it) }
        )
        // Sets up adapter for anime with click listeners
        animeAdapter = MediaAdapter(
            context = context,
            onItemClick = { anime ->
                NavigationUtils.navigateToDetails(this, anime.id, MediaType.ANIME.name)
            },
            onSaveClick = { toggleLikeAndRefresh(it) },
            onExpandClick = { expandItem(it) }
        )
        // Sets the layout manager for displaying items horizontally in the liked movies RecyclerView.
        binding.likedMoviesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = moviesAdapter
        }
        // Sets the layout manager for displaying items horizontally in the liked TV Shows RecyclerView.
        binding.likedTvShowsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = showsAdapter
        }
        // Sets the layout manager for displaying items horizontally in the liked anime RecyclerView.
        binding.likedAnimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }
    }

    /**
     * This function returns the list of user's liked items for movies, TV Shows, and anime from the
     * SharedPreferences. It updates each RecyclerView with corresponding items or shows an empty
     * view if no items are liked.
     */
    private fun loadLikedItems() {
        // Create Gson instance for parsing JSON data.
        val gson = Gson()
        // Launches a coroutine on the lifecycle scope
        lifecycleScope.launch {
            // Gets the liked movies from the SharedPreferences on the IO thread.
            val likedMovies = withContext(Dispatchers.IO) {
                val likedMoviesJson = sharedPreferences.getString("liked_movies", "[]")
                val likedMovies: List<Movie> =
                    gson.fromJson(likedMoviesJson, object : TypeToken<List<Movie>>() {}.type)
                // Set isLiked to true for all liked movies
                likedMovies.forEach { it.isLiked = true }
                // Return the list of liked movies.
                likedMovies
            }
            // Update movies RecyclerView or show empty movie text view
            withContext(Dispatchers.Main) {
                if (likedMovies.isEmpty()) {
                    // Show message if no liked movies
                    binding.emptyMoviesTextView.visibility = View.VISIBLE
                    // Hide movies RecyclerView
                    binding.likedMoviesRecyclerView.visibility = View.GONE
                } else {
                    // Hide the message
                    binding.emptyMoviesTextView.visibility = View.GONE
                    // Show movies RecyclerView
                    binding.likedMoviesRecyclerView.visibility = View.VISIBLE
                    // Update the adapter with liked movies.
                    moviesAdapter.setMediaList(likedMovies)
                }
            }

            // Gets the liked TV Shows from the SharedPreferences on the IO thread.
            val likedShows = withContext(Dispatchers.IO) {
                val likedShowsJson = sharedPreferences.getString("liked_shows", "[]")
                val likedShows: List<Show> =
                    gson.fromJson(likedShowsJson, object : TypeToken<List<Show>>() {}.type)
                // Set isLiked to true for all liked shows
                likedShows.forEach { it.isLiked = true }
                // Return the list of liked TV shows.
                likedShows
            }
            // Update TV Shows RecyclerView or show empty TV Show text view
            withContext(Dispatchers.Main) {
                if (likedShows.isEmpty()) {
                    // Show message if no liked TV Shows.
                    binding.emptyTvShowsTextView.visibility = View.VISIBLE
                    // Hide TV Shows RecyclerView
                    binding.likedTvShowsRecyclerView.visibility = View.GONE
                } else {
                    // Hide the message
                    binding.emptyTvShowsTextView.visibility = View.GONE
                    // Show TV Shows RecyclerView
                    binding.likedTvShowsRecyclerView.visibility = View.VISIBLE
                    // Update the adapter with liked TV Shows.
                    showsAdapter.setMediaList(likedShows)
                }
            }

            // Gets the liked anime from the SharedPreferences on the IO thread.
            val likedAnime = withContext(Dispatchers.IO) {
                val likedAnimeJson = sharedPreferences.getString("liked_anime", "[]")
                val likedAnime: List<Anime> =
                    gson.fromJson(likedAnimeJson, object : TypeToken<List<Anime>>() {}.type)
                // Set isLiked to true for all liked anime
                likedAnime.forEach { it.isLiked = true }
                // Return the list of liked anime.
                likedAnime
            }
            // Update anime RecyclerView or show empty anime text view
            withContext(Dispatchers.Main) {
                if (likedAnime.isEmpty()) {
                    // Show message if no liked anime.
                    binding.emptyAnimeTextView.visibility = View.VISIBLE
                    // Hide anime RecyclerView
                    binding.likedAnimeRecyclerView.visibility = View.GONE
                } else {
                    // Hide the message
                    binding.emptyAnimeTextView.visibility = View.GONE
                    // Show anime RecyclerView
                    binding.likedAnimeRecyclerView.visibility = View.VISIBLE
                    // Update the adapter with liked anime.
                    animeAdapter.setMediaList(likedAnime)
                }
            }
        }
    }

    /**
     * This function toggles the liked status of a given item. It also refreshes the liked item list
     * to reflect the current like status.
     *
     * @param item The media item whose liked status is being toggled.
     */
    private fun toggleLikeAndRefresh(item: Any) {
        // Use LikeButtonUtils to toggle like status
        likeButtonUtils.toggleLikeToItem(item)
        // Reload liked items to reflect changes.
        loadLikedItems()
    }

    /**
     * This function handles the expand button click for a media item and toggles the expandable layout.
     *
     * @param item The media item whose expandable layout is to be toggled.
     */
    private fun expandItem(item: Any) {
        when (item) {
            is Movie -> moviesAdapter.toggleItemExpansion(item)
            is Show -> showsAdapter.toggleItemExpansion(item)
            is Anime -> animeAdapter.toggleItemExpansion(item)
        }
    }

    /**
     * This function sets up the bottom navigation bar, configures each tab with its navigation behavior.
     */
    private fun setupBottomNavigation() {
        // Sets the default selected item to the User tab.
        binding.bottomNavigationView.selectedItemId = R.id.user
        // Configures the bottom navigation bar's item selection listener.
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Navigates to the Home tab if the Home is selected.
                    NavigationUtils.navigateToHome(this)
                    true
                }
                // Keeps the user on user screen if user icon is selected.
                R.id.user -> true
                R.id.discover -> {
                    // Navigates to the Discover tab if the Discover is selected.
                    NavigationUtils.navigateToDiscover(this)
                    true
                }
                // No navigation for any other cases.
                else -> false
            }
        }
    }
}
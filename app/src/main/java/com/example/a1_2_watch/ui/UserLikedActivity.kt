package com.example.a1_2_watch.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.UserLikedLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.a1_2_watch.R
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.utils.LikeButtonUtils

/**
 * UserLikedActivity displays a list of media items that the user has marked as liked.
 * It manages functionality to view and manage liked movies, TV shows, and anime.
 */
class UserLikedActivity : AppCompatActivity() {

    // Binds the user page layout for accessing UI components.
    private lateinit var binding: UserLikedLayoutBinding

    // Adapters for managing and displaying the user's liked items.
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var showsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>

    // Utility class for managing like functionality.
    private lateinit var likeButtonUtils: LikeButtonUtils

    /**
     * Called when the activity is created.
     * Initializes bindings, adapters, sets up navigation, and loads the user's liked items.
     *
     * @param savedInstanceState State of the activity if it was previously saved.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("UserLikedActivity", "onCreate: Initializing UserLikedActivity")

            // Inflate the UserLiked layout view.
            binding = UserLikedLayoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize LikeButtonUtils for managing liked items.
            likeButtonUtils = LikeButtonUtils(this)

            // Set up the Go Back button to close the activity.
            binding.goBackButton.setOnClickListener { finish() }

            // Set up the RecyclerViews and adapters.
            setupAdapters()

            // Load liked items for movies, TV shows, and anime.
            loadLikedItems()

            // Set up the bottom navigation.
            setupBottomNavigation()
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error during onCreate: ${e.message}", e)
        }
    }

    /**
     * Called when the activity is resumed.
     * Reloads the user's liked items to reflect any recent changes.
     */
    override fun onResume() {
        super.onResume()
        try {
            Log.d("UserLikedActivity", "onResume: Reloading liked items")
            loadLikedItems()
            binding.bottomNavigationView.selectedItemId = R.id.user
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error during onResume: ${e.message}", e)
        }
    }

    /**
     * Sets up the RecyclerView adapters for movies, TV shows, and anime.
     * Configures each adapter with click listeners and sets horizontal scrolling.
     */
    private fun setupAdapters() {
        try {
            Log.d("UserLikedActivity", "Setting up adapters")

            // Set up the movies adapter.
            moviesAdapter = createMediaAdapter(this, MediaType.MOVIES)
            binding.likedMoviesRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = moviesAdapter
            }

            // Set up the TV shows adapter.
            showsAdapter = createMediaAdapter(this, MediaType.TV_SHOWS)
            binding.likedTvShowsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = showsAdapter
            }

            // Set up the anime adapter.
            animeAdapter = createMediaAdapter(this, MediaType.ANIME)
            binding.likedAnimeRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = animeAdapter
            }
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error setting up adapters: ${e.message}", e)
        }
    }

    /**
     * Creates a MediaAdapter for the specified media type.
     * Configures the adapter with item click and like button click listeners.
     *
     * @param activity The current activity.
     * @param mediaType The type of media (MOVIES, TV_SHOWS, ANIME).
     * @return A configured MediaAdapter instance.
     */
    private fun <T> createMediaAdapter(activity: Activity, mediaType: MediaType): MediaAdapter<T> {
        return MediaAdapter(
            context = activity,
            onItemClick = { item ->
                try {
                    val mediaId = when (item) {
                        is Movie -> item.id
                        is Show -> item.id
                        is Anime -> item.id
                        else -> throw IllegalArgumentException("Unknown media type")
                    }
                    NavigationUtils.navigateToDetails(activity, mediaId, mediaType.name)
                } catch (e: Exception) {
                    Log.e("UserLikedActivity", "Error navigating to details: ${e.message}", e)
                }
            },
            onSaveClick = { item ->
                try {
                    toggleLikeAndRefresh(item)
                } catch (e: Exception) {
                    Log.e("UserLikedActivity", "Error toggling like status: ${e.message}", e)
                }
            }
        )
    }

    /**
     * Loads the user's liked items for movies, TV shows, and anime.
     * Updates the RecyclerViews and empty state text views accordingly.
     */
    private fun loadLikedItems() {
        lifecycleScope.launch {
            try {
                Log.d("UserLikedActivity", "Loading liked items")
                // Reset adapter to clear stale data
                moviesAdapter.setMediaList(emptyList())
                showsAdapter.setMediaList(emptyList())
                animeAdapter.setMediaList(emptyList())
                // Load liked movies.
                val likedMovies = withContext(Dispatchers.IO) { likeButtonUtils.getLikedMovies() }
                updateRecyclerView(likedMovies, moviesAdapter, binding.likedMoviesRecyclerView, binding.emptyMoviesTextView)

                // Load liked TV shows.
                val likedShows = withContext(Dispatchers.IO) { likeButtonUtils.getLikedShows() }
                updateRecyclerView(likedShows, showsAdapter, binding.likedTvShowsRecyclerView, binding.emptyTvShowsTextView)

                // Load liked anime.
                val likedAnime = withContext(Dispatchers.IO) { likeButtonUtils.getLikedAnime() }
                updateRecyclerView(likedAnime, animeAdapter, binding.likedAnimeRecyclerView, binding.emptyAnimeTextView)

            } catch (e: Exception) {
                Log.e("UserLikedActivity", "Error loading liked items: ${e.message}", e)
            }
        }
    }

    /**
     * Updates a RecyclerView with a list of items and handles the visibility of the corresponding empty state text view.
     *
     * @param items The list of media items to be displayed.
     * @param adapter The adapter managing the RecyclerView.
     * @param recyclerView The RecyclerView to be updated.
     * @param emptyTextView The TextView to show when the list is empty.
     */
    private fun <T> updateRecyclerView(items: List<T>, adapter: MediaAdapter<T>, recyclerView: View, emptyTextView: View) {
        try {
            if (items.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setMediaList(items)
            }
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error updating RecyclerView: ${e.message}", e)
        }
    }

    /**
     * Toggles the liked status of a given item.
     * Refreshes the liked items to reflect the updated status and removes the item from the list if it is unliked.
     *
     * @param item The media item to toggle the liked status for.
     */
    private fun <T> toggleLikeAndRefresh(item: T) {
        lifecycleScope.launch {
            try {
                // Toggle the like status of the item.
                likeButtonUtils.toggleLikeToItem(item as Any)

                // Remove the item from the adapter if unliked.
                when (item) {
                    is Movie -> updateAdapterAfterUnlike(item, moviesAdapter, binding.likedMoviesRecyclerView, binding.emptyMoviesTextView)
                    is Show -> updateAdapterAfterUnlike(item, showsAdapter, binding.likedTvShowsRecyclerView, binding.emptyTvShowsTextView)
                    is Anime -> updateAdapterAfterUnlike(item, animeAdapter, binding.likedAnimeRecyclerView, binding.emptyAnimeTextView)
                }
            } catch (e: Exception) {
                Log.e("UserLikedActivity", "Error toggling like status: ${e.message}", e)
            }
        }
    }

    /**
     * Updates the adapter by removing an unliked item and handles empty state visibility.
     *
     * @param item The item to remove from the adapter.
     * @param adapter The adapter managing the RecyclerView.
     * @param recyclerView The RecyclerView being managed.
     * @param emptyTextView The TextView to show when the list becomes empty.
     */
    private fun <T> updateAdapterAfterUnlike(item: T, adapter: MediaAdapter<T>, recyclerView: View, emptyTextView: View) {
        try {
            adapter.removeMediaItem(item)
            if (adapter.itemCount == 0) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error updating adapter after unliking: ${e.message}", e)
        }
    }

    /**
     * Sets up the bottom navigation bar, configuring each tab with its respective navigation behavior.
     */
    private fun setupBottomNavigation() {
        try {
            // Set the default selected item to the User tab.
            binding.bottomNavigationView.selectedItemId = R.id.user

            // Configure the bottom navigation bar item selection listener.
            binding.bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> {
                        NavigationUtils.navigateToHome(this)
                        true
                    }
                    R.id.user -> true // Stay on the current screen.
                    R.id.discover -> {
                        NavigationUtils.navigateToDiscover(this)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("UserLikedActivity", "Error setting up bottom navigation: ${e.message}", e)
        }
    }
}

// DetailsActivity.kt
package com.example.a1_2_watch.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.ProvidersAdapter
import com.example.a1_2_watch.databinding.DetailsLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import com.example.a1_2_watch.workers.FetchDetailsWorker
import com.example.a1_2_watch.workers.FetchProvidersWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * DetailsActivity responsible for displaying detailed information about a selected media item
 * (movie, TV Show, and anime), and handles fetching details, managing user likes, displaying
 * watch providers information, and setting up UI elements.
 */
class DetailsActivity : AppCompatActivity() {
    // Binding object to access the details layout view.
    private lateinit var binding: DetailsLayoutBinding
    // Creates adapter for displaying available streaming providers.
    private lateinit var providersAdapter: ProvidersAdapter
    // Gets the User's country code for region-based filtering.
    private var countryCode: String = Locale.getDefault().country
    // Creates LikeButtonUtils for managing liked items.
    private lateinit var likeButtonUtils: LikeButtonUtils
    // The ID of the selected media item.
    private var mediaId: Int = -1
    // The type of the media (MOVIES, TV_SHOWS, ANIME)
    private lateinit var mediaType: MediaType
    // Holds the fetched details of the media item.
    private var detailedItem: Any? = null
    // Gson instance for JSON parsing
    private val gson = Gson()

    /**
     * This function initializes the activity, setting up the layout, event listeners and fetching
     * data.
     *
     * @param savedInstanceState Used to restore the activity's previously saved state if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and set it as the content view for the activity.
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create the provider adapter with an empty list and set up the RecyclerView.
        providersAdapter = ProvidersAdapter(emptyList())
        setupProvidersRecyclerView()
        // Set up the Go Back button and finish activity when back button is pressed
        binding.goBackButton.setOnClickListener {
            finish()
        }

        // Initialize LikeButtonUtils for managing liked items.
        likeButtonUtils = LikeButtonUtils(this)
        // Setup Bottom Navigation
        setupBottomNavigation()
        // Gets the media ID and type from the intent extras
        extractMediaDataFromIntent()
        // Fetch detailed information if a valid media ID is provided.
        if (mediaId != -1) {
            fetchDetails()
        }
        // Fetch available streaming or watch providers based on media type.
        fetchProvidersBasedOnMediaType()
    }

    /**
     * This function restores and updates the UI when the activity resumes, such as setting the like
     * button status.
     */
    override fun onResume() {
        super.onResume()
        // Set the like button status based on the saved like state of the detailed item.
        detailedItem?.let { setLikeButtonStatus(it) }
    }

    /**
     * This function sets up the RecyclerView to display streaming providers in a layout, and sets up
     * providerAdapter to manage and display the list of providers.
     */
    private fun setupProvidersRecyclerView() {
        // Sets the layout manager to display items horizontally in the RecyclerView.
        binding.providersRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Assigns the adapter for providing views that display provider data.
        binding.providersRecyclerView.adapter = providersAdapter
    }

    /**
     * This function extracts media data ID and type from the intent that started this activity.
     * Sets mediaId and mediaType for later use in fetching media details.
     */
    private fun extractMediaDataFromIntent() {
        // Get the media ID from the intent, defaults to -1 if not provided.
        mediaId = intent.getIntExtra("MEDIA_ID", -1)
        // Get the media type from the intent as a string.
        val mediaTypeString = intent.getStringExtra("MEDIA_TYPE")
        // Convert the media type string to a MediaType enum value, defaults to MOVIES if not provided.
        mediaType = MediaType.valueOf(mediaTypeString ?: "MOVIES")
    }

    /**
     * This function fetches streaming or watch provider information based on media type.
     * Determine which provider fetching method to call for anime or other media types.
     */
    private fun fetchProvidersBasedOnMediaType() {
        // If the media type is ANIME, fetch streaming providers based on its ID
        if (mediaType == MediaType.ANIME) {
            // Convert the media ID to string and pass it to the fetch method.
            fetchStreamingProviders(mediaId.toString())
        } else {
            // Fetches watch provider information for movies or shows.
            fetchWatchProviders()
        }
    }

    /**
     * fetchDetails fetches detailed information for the selected media item using WorkManager and updates the UI based
     * on its type.
     */
    private fun fetchDetails() {
        val inputData = workDataOf(
            FetchDetailsWorker.KEY_MEDIA_TYPE to mediaType.name,
            FetchDetailsWorker.KEY_MEDIA_ID to mediaId
        )

        val fetchDetailsWork = OneTimeWorkRequestBuilder<FetchDetailsWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(fetchDetailsWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchDetailsWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchDetailsWorker.KEY_RESULT)
                        handleDetailsResult(resultJson)
                    } else {
                        // Handle failure
                        showError()
                    }
                }
            })
    }

    /**
     * This function handles the result from the FetchDetailsWorker and updates the UI accordingly.
     *
     * @param resultJson The JSON string containing the fetched details.
     */
    private fun handleDetailsResult(resultJson: String?) {
        if (resultJson == null) {
            showError()
            return
        }
        when (mediaType) {
            MediaType.MOVIES -> {
                val movieDetails = gson.fromJson(resultJson, MovieDetails::class.java)
                detailedItem = movieDetails
                updateMovieDetails(movieDetails)
                setLikeButtonStatus(movieDetails)
                binding.saveButton.setOnClickListener { toggleLike(movieDetails) }
            }
            MediaType.TV_SHOWS -> {
                val showDetails = gson.fromJson(resultJson, ShowDetails::class.java)
                detailedItem = showDetails
                updateTVShowDetails(showDetails)
                setLikeButtonStatus(showDetails)
                binding.saveButton.setOnClickListener { toggleLike(showDetails) }
            }
            MediaType.ANIME -> {
                val animeDetails = gson.fromJson(resultJson, AnimeDetails::class.java)
                detailedItem = animeDetails
                updateAnimeDetails(animeDetails)
                setLikeButtonStatus(animeDetails)
                binding.saveButton.setOnClickListener { toggleLike(animeDetails) }
            }
        }
    }

    /**
     * This function updates the UI details for a given movie.
     *
     * @param movie The movie details to be updated and displayed.
     */
    private fun updateMovieDetails(movie: MovieDetails) {
        // Sets the title of the movie in the title TextView.
        binding.titleTextView.text = movie.title
        // Builds and sets the description text, with a fallback if the overview is unavailable.
        binding.descriptionTextView.text = buildString {
            append("Description: \n ")
            append(movie.overview ?: getString(R.string.no_overview_available))
        }
        // Builds and sets the release date text for the movie.
        binding.releaseDateTextView.text = buildString {
            append("Release Date: ")
            append(movie.release_date)
        }
        // Builds and sets the run time of the movie in minutes.
        binding.runtimeTextView.text = buildString {
            append("Runtime: ")
            append(movie.runtime.let { "$it minutes" })
        }
        // Builds and sets the genre of the movie, separated by commas.
        binding.genresTextView.text = buildString {
            append("Genres: ")
            append(movie.genres.joinToString(", ") { it.name })
        }
        // Builds and sets the revenue of the movie in dollars.
        binding.revenueTextView.text = buildString {
            append("Revenue: \$")
            append(movie.revenue)
        }
        // Builds and sets the budget of the movie in dollars.
        binding.budgetTextView.text = buildString {
            append("Budget: \$")
            append(movie.budget)
        }
        // Build and sets the average rating for the movie.
        binding.mediaRatingTextView.text =
            String.format(Locale.getDefault(), "%.1f", movie.vote_average)
        // Load the movie poster image using the Glide library, with the URL specified.
        Glide.with(this)
            .load(Constants.IMAGE_URL + movie.poster_path)
            .into(binding.movieImageView)
        // Hide Anime-specific and TV Show-specific fields that are not relevant to movies.
        binding.nextReleaseLayout.visibility = View.GONE
        binding.endDateTextView.visibility = View.GONE
        binding.seasonCountTextView.visibility = View.GONE
        binding.episodeCountTextView.visibility = View.GONE
    }

    /**
     * This function updates the UI details for a given TV Show.
     *
     * @param tvShow The TV Show details to be updated and displayed.
     */
    private fun updateTVShowDetails(tvShow: ShowDetails) {
        // Sets the name of the Tv show in the title TextView.
        binding.titleTextView.text = tvShow.name
        // Sets the description text, with a fallback if the overview is unavailable.
        binding.descriptionTextView.text =
            tvShow.overview ?: getString(R.string.no_overview_available)
        // Builds and sets the first air date for the TV Show.
        binding.releaseDateTextView.text = buildString {
            append("First Air Date: ")
            append(tvShow.first_air_date)
        }
        // Builds and sets the number of seasons for the TV Show.
        binding.seasonCountTextView.text = buildString {
            append("Seasons: ")
            append(tvShow.number_of_seasons)
        }
        // Builds and sets the number of episodes for the TV Show.
        binding.episodeCountTextView.text = buildString {
            append("Episodes: ")
            append(tvShow.number_of_episodes)
        }
        // Builds and sets the runtime for episodes for the TV Show in minutes.
        binding.runtimeTextView.text = buildString {
            append("Runtime: ")
            append(tvShow.episode_run_time.joinToString(", ") { "$it min" })
        }
        // Builds and sets the average rating for the TV Show.
        binding.mediaRatingTextView.text =
            String.format(Locale.getDefault(), "%.1f", tvShow.vote_average)
        // Checks if the TV Show has ended, if so, displays the last air date
        if (tvShow.status == "Ended") {
            binding.endDateTextView.text = buildString {
                append("Last Air Date: ")
                append(tvShow.last_air_date ?: "Unknown")
            }
            // And hides the next episode information.
            binding.nextReleaseLayout.visibility = View.GONE
        } else {
            // Hides the end date if the TV Show has not ended
            binding.endDateTextView.visibility = View.GONE
            // Sets an icon for the upcoming episode's air date.
            Glide.with(this)
                .load(R.drawable.sand_clock)
                .into(binding.sandClockView)
            // Displays the name of the next episode if available.
            binding.nextReleaseTextView.text = buildString {
                append("Next Episode to Air: ")
                append(tvShow.next_episode_to_air?.name ?: "Unknown")
            }
            // Displays the air date of the next episode if available
            binding.nextReleaseDateTextView.text = buildString {
                append("When? ")
                append(tvShow.next_episode_to_air?.air_date ?: "Unknown")
            }
        }
        // Builds and sets the genres of the TV show, separated by commas.
        binding.genresTextView.text = buildString {
            append("Genres: ")
            append(tvShow.genres.joinToString(", ") { it.name })
        }
        // Loads the TV Show poster or backdrop image using Glide, with a fallback to the backdrop if no poster is available.
        Glide.with(this)
            .load(Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path))
            .into(binding.movieImageView)

        // Hide Anime-specific and Movie-specific fields which are not relevant for TV Shows.
        binding.revenueTextView.visibility = View.GONE
        binding.budgetTextView.visibility = View.GONE
    }

    /**
     * This function updates the UI details for a given anime.
     *
     * @param animeDetails The anime details to be updated and displayed.
     */
    private fun updateAnimeDetails(animeDetails: AnimeDetails) {
        // Gets the main data object from the anime details.
        val animeData = animeDetails.data
        // Accesses the attributes of the anime if available.
        val attributes = animeData?.attributes
        // Checks if the attributes are present.
        if (attributes != null) {
            // Sets the title of the anime in the title TextView.
            binding.titleTextView.text = attributes.canonicalTitle
            // Sets the synopsis of the anime or provides a fallback if not available.
            binding.descriptionTextView.text =
                attributes.synopsis ?: getString(R.string.no_overview_available)
            // Builds and sets the start date of the anime if available.
            binding.releaseDateTextView.text = buildString {
                append("Start Date: ")
                append(attributes.startDate ?: getString(R.string.unknown))
            }
            // Builds and sets the end date of the anime if available.
            binding.endDateTextView.text = buildString {
                append("End Date: ")
                append(attributes.endDate ?: getString(R.string.unknown))
            }
            // Sets the runtime for each episode of the anime.
            val runtime = attributes.episodeLength.let { "$it min" }
            binding.seasonCountTextView.text = buildString {
                append("Runtime : ")
                append(runtime)
            }
            // Builds and sets the total number of episodes, or fallback if not available.
            binding.episodeCountTextView.text = buildString {
                append("Episodes: ")
                append(attributes.episodeCount ?: getString(R.string.unknown))
            }
            // Displays the length of each episode.
            binding.runtimeTextView.text = buildString {
                append("Episode Length: ")
                append(attributes.episodeLength)
            }
            // Formats and sets the average rating for the anime.
            binding.mediaRatingTextView.text = String.format(
                Locale.getDefault(),
                "%.1f",
                attributes.averageRating.toFloat() / 10
            )
            // Checks if there's an end date, if so, hides the next episode details.
            if (attributes.endDate != null) {
                binding.endDateTextView.text = buildString {
                    append("Last Air Date: ")
                    append(attributes.endDate)
                }
                // And hides the next episode details.
                binding.nextReleaseLayout.visibility = View.GONE
            } else {
                // If no end date, displays a placeholder for the next episode.
                binding.endDateTextView.visibility = View.VISIBLE
                // Load an icon for the next episode release information.
                Glide.with(this)
                    .load(R.drawable.sand_clock)
                    .into(binding.sandClockView)
                // Sets the next episode release title if available.
                binding.nextReleaseTextView.text = buildString {
                    append("Next Episode to Air: ")
                    append(attributes.nextRelease?.subSequence(0, 10))
                }
                // Sets the air date and time for the next episode.
                binding.nextReleaseDateTextView.text = buildString {
                    append("When? ")
                    append(attributes.nextRelease?.subSequence(11, 16))
                }
            }
            // Loads and displays the anime's poster image if available.
            Glide.with(this)
                .load(attributes.posterImage?.medium)
                .into(binding.movieImageView)

            // Hide Movie-specific and TV Show-specific fields which are not relevant for anime.
            binding.genresTextView.visibility = View.GONE
            binding.revenueTextView.visibility = View.GONE
            binding.budgetTextView.visibility = View.GONE
        } else {
            // Shows an error if attributes are missing.
            showError()
        }
    }

    /**
     * Displays an error message when media details are unavailable, and hides detailed
     * fields and shows default error messages.
     */
    private fun showError() {
        // Set a placeholder text in the (title, description) TextView indicating no title or overview is available.
        binding.titleTextView.text = getString(R.string.no_title_available)
        binding.descriptionTextView.text = getString(R.string.no_overview_available)
        // Hide all other details since no data is available.
        binding.releaseDateTextView.visibility = View.GONE
        binding.nextReleaseLayout.visibility = View.GONE
        binding.endDateTextView.visibility = View.GONE
        binding.runtimeTextView.visibility = View.GONE
        binding.genresTextView.visibility = View.GONE
        binding.revenueTextView.visibility = View.GONE
        binding.budgetTextView.visibility = View.GONE
        binding.seasonCountTextView.visibility = View.GONE
        binding.episodeCountTextView.visibility = View.GONE
    }

    /**
     * This function fetches streaming providers for an anime by its ID using WorkManager.
     *
     * @param animeId The ID of the anime for which streaming providers are to be fetched.
     */
    private fun fetchStreamingProviders(animeId: String) {
        val inputData = workDataOf(
            FetchProvidersWorker.KEY_MEDIA_TYPE to MediaType.ANIME.name,
            FetchProvidersWorker.KEY_MEDIA_ID to animeId.toInt()
            // No country code is passed for Anime as per requirement
        )

        val fetchProvidersWork = OneTimeWorkRequestBuilder<FetchProvidersWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this)
            .enqueue(fetchProvidersWork)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchProvidersWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchProvidersWorker.KEY_RESULT)
                        handleProvidersResult(resultJson)
                    } else {
                        // Handle failure
                        handleNoProvidersAvailable()
                    }
                }
            })
    }

    /**
     * This function fetches watch providers for movies and TV shows using WorkManager.
     * Updated to pass the country code for region-based filtering.
     */
    private fun fetchWatchProviders() {
        // **UPDATED**: Include countryCode in inputData for Movies and TV Shows
        val inputData = workDataOf(
            FetchProvidersWorker.KEY_MEDIA_TYPE to mediaType.name,
            FetchProvidersWorker.KEY_MEDIA_ID to mediaId,
            FetchProvidersWorker.KEY_COUNTRY_CODE to countryCode // Pass the country code here
        )

        val fetchProvidersWork = OneTimeWorkRequestBuilder<FetchProvidersWorker>()
            .setInputData(inputData)
            .build()

        // **OPTIONAL**: Use enqueueUniqueWork to prevent duplicate workers for the same media type and ID
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "fetch_providers_${mediaType.name}_$mediaId",
                ExistingWorkPolicy.REPLACE,
                fetchProvidersWork
            )

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(fetchProvidersWork.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultJson = workInfo.outputData.getString(FetchProvidersWorker.KEY_RESULT)
                        handleProvidersResult(resultJson)
                    } else {
                        // Handle failure
                        handleProviderFetchError()
                    }
                }
            })
    }

    /**
     * This function handles the result from FetchProvidersWorker and updates the UI accordingly.
     *
     * @param resultJson The JSON string containing the providers information.
     */

    private fun handleProvidersResult(resultJson: String?) {
        if (resultJson == null || resultJson.isEmpty()) {
            handleNoProvidersAvailable()
            return
        }


        when (mediaType) {
            MediaType.ANIME -> {
                val type = object : TypeToken<List<Provider>>() {}.type
                val providers: List<Provider> = gson.fromJson(resultJson, type)
                if (providers.isNotEmpty()) {
                    providersAdapter.updateProviders(providers)
                    binding.noProvidersTextView.visibility = View.GONE
                } else {
                    handleNoProvidersAvailable()
                }
            }
            MediaType.MOVIES, MediaType.TV_SHOWS -> {
                try {
                    val watchProvidersResponse = gson.fromJson(resultJson, WatchProvidersResponse::class.java)
                    val results = watchProvidersResponse.results

                    if (results.isNullOrEmpty()) {
                        handleNoProvidersAvailable()
                        return
                    }

                    // Attempt to find providers under the country code, "US", or "ALL"
                    val possibleKeys = listOf(countryCode, "US", "ALL")
                    var providersFound = false

                    for (key in possibleKeys) {
                        val providersRegion = results[key]
                        if (providersRegion != null) {
                            val providers = providersRegion.flatrate
                            if (!providers.isNullOrEmpty()) {
                                providersAdapter.updateProviders(providers)
                                binding.noProvidersTextView.visibility = View.GONE
                                providersFound = true
                                break
                            }
                        }
                    }

                    if (!providersFound) {
                        handleNoProvidersAvailable()
                    }

                } catch (e: Exception) {
                    handleProviderFetchError()
                }
            }
        }
    }



    /**
     * Handles the case where no streaming providers are available, and displays a message in the
     * noProvidersTextView to inform the user.
     */
    private fun handleNoProvidersAvailable() {
        // Clears the providers adapter with empty list.
        providersAdapter.updateProviders(emptyList())
        // Displays no providers message.
        binding.noProvidersTextView.visibility = View.VISIBLE
        binding.noProvidersTextView.text = getString(R.string.no_available_providers_in_your_region)
    }

    /**
     * Handles the error scenario for fetching providers, showing an error message in the UI
     */
    private fun handleProviderFetchError() {
        // Display the error message TextView.
        binding.noProvidersTextView.visibility = View.VISIBLE
        // Sets the error text.
        binding.noProvidersTextView.text = getString(R.string.error_fetching_providers)
    }

    /**
     * This function toggles the like button status of a given media item by converting detailed
     * information into a minimal representation and updating its like status.
     *
     * @param detailedItem The full detail object of the media item, which can be a MovieDetails
     *                     , ShowDetails, or AnimeDetails instance.
     */
    private fun toggleLike(detailedItem: Any) {
        lifecycleScope.launch {
            // Toggle like status of the item
            likeButtonUtils.toggleLikeToItem(detailedItem)

            // Update the like button status
            setLikeButtonStatus(detailedItem)
        }
    }


    /**
     * This function updates the like button icon to reflect the current status of detailed item.
     *
     * @param detailedItem The detailed object of the media item to check like status for.
     */
    private fun setLikeButtonStatus(detailedItem: Any) {
        lifecycleScope.launch {
            val isLiked = likeButtonUtils.isItemLiked(detailedItem)
            withContext(Dispatchers.Main) {
                binding.saveButton.setImageResource(
                    if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                )
            }
        }
    }


    /**
     * This function sets up the bottom navigation bar, configures each tab with its navigation behavior.
     */
    private fun setupBottomNavigation() {
        // Access the bottom navigation bar from the binding.
        val bottomNavigationView = binding.bottomNavigationView
        // Disable group checkable behavior, ensuring no selected item in the navigation bar.
        bottomNavigationView.menu.setGroupCheckable(0, false, true)
        // Sets the item selected listener to handle navigation.
        bottomNavigationView.setOnItemSelectedListener { item ->
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

package com.example.a1_2_watch.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

/**
 * Displays detailed information about a selected media item (movie, TV show, or anime).
 * Handles fetching details, managing user likes, displaying streaming providers, and setting up the UI.
 */
class DetailsActivity : AppCompatActivity() {

    // UI binding for the details layout
    private lateinit var binding: DetailsLayoutBinding

    // Adapter for displaying streaming providers
    private lateinit var providersAdapter: ProvidersAdapter

    // Utilities for managing liked items
    private lateinit var likeButtonUtils: LikeButtonUtils

    // Media details
    private var mediaId: Int = -1
    private lateinit var mediaType: MediaType
    private var detailedItem: Any? = null

    // User's region code
    private val countryCode: String = Locale.getDefault().country

    // Gson instance for JSON parsing
    private val gson = Gson()

    /**
     * Initializes the activity, sets up the layout, event listeners, and fetches data.
     *
     * @param savedInstanceState State of the activity if previously saved.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DetailsActivity", "onCreate: Initializing activity")
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Initialize components
            setupUIComponents()
            extractMediaDataFromIntent()

            // Fetch details and providers
            if (mediaId != -1) {
                fetchDetails()
                fetchProvidersBasedOnMediaType()
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error in onCreate: ${e.message}", e)
        }
    }

    /**
     * Restores and updates the UI when the activity resumes, such as the like button status.
     */
    override fun onResume() {
        super.onResume()
        try {
            Log.d("DetailsActivity", "onResume: Restoring like button status")
            // Set the like button status based on the saved like state of the detailed item.
            detailedItem?.let { setLikeButtonStatus(it) }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error in onResume: ${e.message}", e)
        }
    }

    /**
     * Sets up UI components including RecyclerView, bottom navigation, and event listeners.
     */
    private fun setupUIComponents() {
        try {
            Log.d("DetailsActivity", "Setting up UI components")
            // Setup RecyclerView for streaming providers
            providersAdapter = ProvidersAdapter(emptyList())
            binding.providersRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            // Assigns the adapter for providing views that display provider data.
            binding.providersRecyclerView.adapter = providersAdapter

            // Back button action
            binding.goBackButton.setOnClickListener { finish() }

            // Initialize LikeButtonUtils
            likeButtonUtils = LikeButtonUtils(this)

            // Setup bottom navigation
            setupBottomNavigation()
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error setting up UI components: ${e.message}", e)
        }
    }

    /**
     * Extracts media ID and type from the intent that started this activity.
     */
    private fun extractMediaDataFromIntent() {
        try {
            mediaId = intent.getIntExtra("MEDIA_ID", -1)
            mediaType = MediaType.valueOf(intent.getStringExtra("MEDIA_TYPE") ?: "MOVIES")
            Log.d("DetailsActivity", "Media data extracted: ID=$mediaId, Type=$mediaType")
        } catch (e: IllegalArgumentException) {
            Log.e("DetailsActivity", "Error parsing media type: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error extracting media data: ${e.message}", e)
        }
    }

    /**
     * Fetches details of the selected media item using WorkManager.
     */
    private fun fetchDetails() {
        try {
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
                .observe(this) { workInfo ->
                    if (workInfo?.state?.isFinished == true) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            handleDetailsResult(workInfo.outputData.getString(FetchDetailsWorker.KEY_RESULT))
                        } else {
                            Log.e("DetailsActivity", "Work failed for fetching details.")
                            showError()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error fetching details: ${e.message}", e)
        }
    }

    /**
     * Fetches providers based on the media type (movie, TV show, or anime).
     */
    private fun fetchProvidersBasedOnMediaType() {
        try {
            Log.d(
                "DetailsActivity",
                "Fetching providers for mediaType: $mediaType, mediaId: $mediaId"
            )
            when (mediaType) {
                MediaType.ANIME -> fetchStreamingProviders(mediaId.toString())
                MediaType.MOVIES, MediaType.TV_SHOWS -> fetchWatchProviders()
                else -> Log.w("DetailsActivity", "Unsupported mediaType: $mediaType")
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error fetching providers: ${e.message}", e)
        }
    }

    /**
     * Handles the result from FetchDetailsWorker and updates the UI.
     *
     * @param resultJson JSON string containing the fetched details.
     */
    private fun handleDetailsResult(resultJson: String?) {
        try {
            if (resultJson == null) {
                showError()
                return
            }
            detailedItem = when (mediaType) {
                MediaType.MOVIES -> gson.fromJson(resultJson, MovieDetails::class.java).apply {
                    Log.d("DetailsActivity", "Movie details fetched successfully.")
                    updateMovieDetails(this)
                    setupLikeButton(this)
                }

                MediaType.TV_SHOWS -> gson.fromJson(resultJson, ShowDetails::class.java).apply {
                    Log.d("DetailsActivity", "TV Show details fetched successfully.")
                    updateTVShowDetails(this)
                    setupLikeButton(this)
                }

                MediaType.ANIME -> gson.fromJson(resultJson, AnimeDetails::class.java).apply {
                    Log.d("DetailsActivity", "Anime details fetched successfully.")
                    updateAnimeDetails(this)
                    setupLikeButton(this)
                }
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error handling details result: ${e.message}", e)
        }
    }

    /**
     * Updates the like button's status and sets its click listener.
     *
     * @param item The detailed media item.
     */
    private fun setupLikeButton(item: Any) {
        try {
            Log.d("DetailsActivity", "Setting up like button")
            setLikeButtonStatus(item)
            binding.saveButton.setOnClickListener { toggleLike(item) }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error setting up like button: ${e.message}", e)
        }
    }

    /**
     * Toggles the like button status for a given media item.
     *
     * @param detailedItem The detailed media item.
     */
    private fun toggleLike(detailedItem: Any) {
        lifecycleScope.launch {
            try {
                Log.d("DetailsActivity", "Toggling like status for item")
                likeButtonUtils.toggleLikeToItem(detailedItem)
                setLikeButtonStatus(detailedItem)
            } catch (e: Exception) {
                Log.e("DetailsActivity", "Error toggling like status: ${e.message}", e)
            }
        }
    }

    /**
     * Updates the like button icon based on the item's like status.
     *
     * @param detailedItem The detailed media item.
     */
    private fun setLikeButtonStatus(detailedItem: Any) {
        lifecycleScope.launch {
            try {
                val isLiked = likeButtonUtils.isItemLiked(detailedItem)
                withContext(Dispatchers.Main) {
                    binding.saveButton.setImageResource(
                        if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                    )
                }
            } catch (e: Exception) {
                Log.e("DetailsActivity", "Error updating like button status: ${e.message}", e)
            }
        }
    }

    /**
     * Handles errors when fetching details or providers.
     */
    private fun showError() {
        try {
            Log.d("DetailsActivity", "Showing error in the UI")
            binding.titleTextView.text = getString(R.string.no_title_available)
            binding.descriptionTextView.text = getString(R.string.no_overview_available)
            binding.noProvidersTextView.visibility = View.VISIBLE
            binding.noProvidersTextView.text = getString(R.string.error_fetching_providers)
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error displaying error message: ${e.message}", e)
        }
    }

    /**
     * Configures the bottom navigation bar behavior.
     */
    private fun setupBottomNavigation() {
        try {
            Log.d("DetailsActivity", "Setting up bottom navigation")
            binding.bottomNavigationView.menu.setGroupCheckable(0, false, true)
            binding.bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> NavigationUtils.navigateToHome(this).let { true }
                    R.id.user -> NavigationUtils.navigateToUser(this).let { true }
                    R.id.discover -> NavigationUtils.navigateToDiscover(this).let { true }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error setting up bottom navigation: ${e.message}", e)
        }
    }

    /**
     * Fetches streaming providers for an anime by its ID using WorkManager.
     *
     * @param animeId The ID of the anime for which streaming providers are to be fetched.
     */
    private fun fetchStreamingProviders(animeId: String) {
        try {
            val inputData = workDataOf(
                FetchProvidersWorker.KEY_MEDIA_TYPE to MediaType.ANIME.name,
                FetchProvidersWorker.KEY_MEDIA_ID to animeId.toInt()
            )

            val fetchProvidersWork = OneTimeWorkRequestBuilder<FetchProvidersWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this)
                .enqueue(fetchProvidersWork)

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(fetchProvidersWork.id)
                .observe(this) { workInfo ->
                    if (workInfo?.state?.isFinished == true) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            handleProvidersResult(workInfo.outputData.getString(FetchProvidersWorker.KEY_RESULT))
                        } else {
                            handleNoProvidersAvailable()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error fetching streaming providers: ${e.message}", e)
        }
    }

    /**
     * Fetches watch providers for movies and TV shows using WorkManager.
     */
    private fun fetchWatchProviders() {
        try {
            val inputData = workDataOf(
                FetchProvidersWorker.KEY_MEDIA_TYPE to mediaType.name,
                FetchProvidersWorker.KEY_MEDIA_ID to mediaId,
                FetchProvidersWorker.KEY_COUNTRY_CODE to countryCode
            )

            val fetchProvidersWork = OneTimeWorkRequestBuilder<FetchProvidersWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(this).enqueue(fetchProvidersWork)

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(fetchProvidersWork.id)
                .observe(this) { workInfo ->
                    if (workInfo?.state?.isFinished == true) {
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            handleProvidersResult(workInfo.outputData.getString(FetchProvidersWorker.KEY_RESULT))
                        } else {
                            handleNoProvidersAvailable()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error fetching watch providers: ${e.message}", e)
        }
    }

    /**
     * Handles the result from FetchProvidersWorker and updates the UI.
     *
     * @param resultJson JSON string containing the providers information.
     */
    private fun handleProvidersResult(resultJson: String?) {
        try {
            if (resultJson.isNullOrEmpty()) {
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
                        val watchProvidersResponse =
                            gson.fromJson(resultJson, WatchProvidersResponse::class.java)
                        val results = watchProvidersResponse.results

                        if (results.isEmpty()) {
                            handleNoProvidersAvailable()
                            return
                        }

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
                        Log.e("DetailsActivity", "Error parsing providers JSON: $e")
                        handleProviderFetchError()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error handling providers result: ${e.message}", e)
        }
    }

    /**
     * Handles the error scenario for fetching providers, showing an error message in the UI.
     */
    private fun handleProviderFetchError() {
        try {
            Log.d("DetailsActivity", "Handling provider fetch error")
            binding.noProvidersTextView.visibility = View.VISIBLE
            binding.noProvidersTextView.text = getString(R.string.error_fetching_providers)
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error showing provider fetch error: ${e.message}", e)
        }
    }

    /**
     * Handles the case where no streaming providers are available.
     */
    private fun handleNoProvidersAvailable() {
        try {
            Log.d("DetailsActivity", "No providers available for the selected media")
            providersAdapter.updateProviders(emptyList())
            binding.noProvidersTextView.visibility = View.VISIBLE
            binding.noProvidersTextView.text =
                getString(R.string.no_available_providers_in_your_region)
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error handling no providers available: ${e.message}", e)
        }
    }

    /**
     * This function updates the UI details for a given movie.
     *
     * @param movie The movie details to be updated and displayed.
     */
    private fun updateMovieDetails(movie: MovieDetails) {
        try {
            Log.d("DetailsActivity", "Updating movie details")
            binding.titleTextView.text = movie.title
            binding.descriptionTextView.text = buildString {
                append("Description: \n ")
                append(movie.overview ?: getString(R.string.no_overview_available))
            }
            binding.releaseDateTextView.text = buildString {
                append("Release Date: ")
                append(movie.release_date ?: getString(R.string.unknown))
            }
            binding.runtimeTextView.text = buildString {
                append("Runtime: ")
                append(movie.runtime.let { "$it minutes" }) ?: getString(R.string.unknown)
            }
            binding.genresTextView.text = buildString {
                append("Genres: ")
                append(movie.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown))
            }
            binding.revenueTextView.text = buildString {
                append("Revenue: \$")
                append(movie.revenue ?: getString(R.string.unknown))
            }
            binding.budgetTextView.text = buildString {
                append("Budget: \$")
                append(movie.budget ?: getString(R.string.unknown))
            }
            binding.mediaRatingTextView.text =
                String.format(Locale.getDefault(), "%.1f", movie.vote_average)
            Glide.with(this)
                .load(Constants.IMAGE_URL + movie.poster_path)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_no_image)
                .into(binding.movieImageView)

            binding.nextReleaseLayout.visibility = View.GONE
            binding.endDateTextView.visibility = View.GONE
            binding.seasonCountTextView.visibility = View.GONE
            binding.episodeCountTextView.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error updating movie details: ${e.message}", e)
            showError()
        }
    }

    /**
     * This function updates the UI details for a given TV Show.
     *
     * @param tvShow The TV Show details to be updated and displayed.
     */
    private fun updateTVShowDetails(tvShow: ShowDetails) {
        try {
            Log.d("DetailsActivity", "Updating TV show details")
            binding.titleTextView.text = tvShow.name
            binding.descriptionTextView.text =
                StringBuilder().apply {
                    append("Description: \n")
                    append(
                        if (tvShow.overview.isNullOrEmpty()) getString(R.string.no_overview_available)
                        else tvShow.overview
                    )
                }
            binding.releaseDateTextView.text = buildString {
                append("First Air Date: ")
                append(tvShow.first_air_date ?: getString(R.string.unknown))
            }
            binding.seasonCountTextView.text = buildString {
                append("Seasons: ")
                append(tvShow.number_of_seasons ?: getString(R.string.unknown))
            }
            binding.episodeCountTextView.text = buildString {
                append("Episodes: ")
                append(tvShow.number_of_episodes ?: getString(R.string.unknown))
            }
            binding.runtimeTextView.text = buildString {
                append("Runtime: ")
                append(
                    if (tvShow.episode_run_time?.joinToString(", ").isNullOrEmpty() )
                        getString(R.string.unknown)
                    else tvShow.episode_run_time?.joinToString(", ") )
                append(" minutes")
            }
            binding.mediaRatingTextView.text =
                String.format(
                    Locale.getDefault(),
                    "%.1f",
                    tvShow.vote_average ?: getString(R.string._0_0)
                )

            if (tvShow.status == "Ended") {
                binding.endDateTextView.text = buildString {
                    append("Last Air Date: ")
                    append(tvShow.last_air_date ?: getString(R.string.unknown))
                }
                binding.nextReleaseLayout.visibility = View.GONE
            } else {
                binding.endDateTextView.visibility = View.GONE
                Glide.with(this)
                    .load(R.drawable.sand_clock)
                    .into(binding.sandClockView)
                binding.nextReleaseTextView.text = buildString {
                    append("Next Episode to Air: ")
                    append(tvShow.next_episode_to_air?.name ?: getString(R.string.unknown))
                }
                binding.nextReleaseDateTextView.text = buildString {
                    append("When? ")
                    append(tvShow.next_episode_to_air?.air_date ?: getString(R.string.unknown))
                }
            }
            binding.genresTextView.text = buildString {
                append("Genres: ")
                append(tvShow.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown))
            }
            Glide.with(this)
                .load(Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path))
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.placeholder_no_image)
                .into(binding.movieImageView)

            binding.revenueTextView.visibility = View.GONE
            binding.budgetTextView.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error updating TV show details: ${e.message}", e)
            showError()
        }
    }

    /**
     * This function updates the UI details for a given anime.
     *
     * @param animeDetails The anime details to be updated and displayed.
     */
    private fun updateAnimeDetails(animeDetails: AnimeDetails) {
        try {
            Log.d("DetailsActivity", "Updating anime details")
            val animeData = animeDetails.data
            val attributes = animeData.attributes
            if (attributes != null) {
                binding.seasonCountTextView.visibility = View.GONE
                binding.titleTextView.text = attributes.canonicalTitle
                binding.descriptionTextView.text =
                    StringBuilder().apply {
                        append("Synopsis: \n")
                        append(
                            attributes.synopsis ?: getString(R.string.no_overview_available)
                        )
                    }
                binding.releaseDateTextView.text = buildString {
                    append("Start Date: ")
                    append(attributes.startDate ?: getString(R.string.unknown))
                }
                binding.endDateTextView.text = buildString {
                    append("End Date: ")
                    append(attributes.endDate ?: getString(R.string.unknown))
                }
                binding.runtimeTextView.text = buildString {
                    append("Runtime : ")
                    append(attributes.episodeLength ?: getString(R.string.unknown))
                    append(" minutes")
                }
                binding.episodeCountTextView.text = buildString {
                    append("Episodes: ")
                    append(attributes.episodeCount ?: getString(R.string.unknown))
                }
                binding.mediaRatingTextView.text = String.format(
                    Locale.getDefault(),
                    "%.1f",
                    attributes.averageRating?.toFloat()?.div(10) ?: getString(R.string._0_0).toFloat()
                )
                if (attributes.endDate != null) {
                    binding.endDateTextView.text = buildString {
                        append("Last Air Date: ")
                        append(attributes.endDate) ?: getString(R.string.unknown)
                    }
                    binding.nextReleaseLayout.visibility = View.GONE
                } else {
                    binding.endDateTextView.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(R.drawable.sand_clock)
                        .into(binding.sandClockView)
                    binding.nextReleaseTextView.text = buildString {
                        append("Next Episode to Air: ")
                        append(attributes.nextRelease?.subSequence(0, 10))
                            ?: getString(R.string.unknown)
                    }
                    binding.nextReleaseDateTextView.text = buildString {
                        append("When? ")
                        append(attributes.nextRelease?.subSequence(11, 16))
                            ?: getString(R.string.unknown)
                    }
                }
                Glide.with(this)
                    .load(attributes.posterImage?.original)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.placeholder_no_image)
                    .into(binding.movieImageView)

                binding.genresTextView.visibility = View.GONE
                binding.revenueTextView.visibility = View.GONE
                binding.budgetTextView.visibility = View.GONE
            } else {
                showError()
            }
        } catch (e: Exception) {
            Log.e("DetailsActivity", "Error updating anime details: ${e.message}", e)
            showError()
        }
    }
}


package com.example.a1_2_watch.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.ProvidersAdapter
import com.example.a1_2_watch.databinding.DetailsLayoutBinding
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.DetailsRepository
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.utils.NavigationUtils
import java.util.Locale
import kotlin.collections.joinToString

/**
 * DetailsActivity responsible for displaying detailed information about a selected media item
 * (movie, TV Show, and anime), and handles fetching details, managing user likes , displaying
 * watch providers information, and setting up UI elements.
 */
class DetailsActivity : AppCompatActivity() {
    // Binding object to access the details layout view.
    private lateinit var binding: DetailsLayoutBinding
    // Creates adapter for displaying available streaming providers.
    private lateinit var providersAdapter: ProvidersAdapter
    // Creates DetailsRepository instance for fetching media details.
    private val detailsRepository = DetailsRepository()
    // Gets the User's country code for region provides.
    private var countryCode: String = Locale.getDefault().country
    // Creates likeButtonUtils for managing liked items.
    private val likeButtonUtils = LikeButtonUtils(this)
    // The ID of the selected media item.
    private var mediaId: Int = -1
    // The type of the media (MOVIES, TV_SHOWS, ANIME)
    private lateinit var mediaType: MediaType
    // Holds the fetched details of the media item.
    private var detailedItem: Any? = null

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
        // If the media type is ANIME, fetch streaming providers based on his ID
        if (mediaType == MediaType.ANIME) {
            // Convert the media ID to string and pass it to the fetch method.
            fetchStreamingProviders(mediaId.toString())
        } else {
            // Fetches watch provider information for movies or shows.
            fetchWatchProviders()
        }
    }

    /**
     * fetchDetails fetches detailed information for the selected media item and updates the UI based
     * on its type.
     */
    private fun fetchDetails() {
        // Fetch details for the media item by using its ID and type.
        detailsRepository.fetchDetails(mediaId, mediaType) { data ->
            // Runs UI updates on the main thread.
            runOnUiThread {
                // Create variable to store the fetched details.
                detailedItem = data
                // Check the data type, updates the UI based on its type
                when (data) {
                    is MovieDetails -> {
                        // Update UI, sets like button status, sets toggle behavior for the like button.
                        updateMovieDetails(data)
                        setLikeButtonStatus(data)
                        binding.saveButton.setOnClickListener { toggleLike(data) }
                    }
                    is ShowDetails -> {
                        // Update UI, sets like button status, sets toggle behavior for the like button.
                        updateTVShowDetails(data)
                        setLikeButtonStatus(data)
                        binding.saveButton.setOnClickListener { toggleLike(data) }
                    }
                    is AnimeDetails -> {
                        // Update UI, sets like button status, sets toggle behavior for the like button.
                        updateAnimeDetails(data)
                        setLikeButtonStatus(data)
                        binding.saveButton.setOnClickListener { toggleLike(data) }
                    }
                    // Displays error if data type is not supported.
                    else -> showError()
                }
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
            append(tvShow.first_air_date )
        }
        // Builds and sets the number of sessions for the TV Show.
        binding.seasonCountTextView.text = buildString {
            append("Seasons: ")
            append(tvShow.number_of_seasons)
        }
        // Builds and sets the number of episodes for the TV Show.
        binding.episodeCountTextView.text = buildString {
            append("Episodes: ")
            append(tvShow.number_of_episodes )
        }
        // Builds and sets the runtime for episodes for the TV Show in minutes.
        binding.runtimeTextView.text = buildString {
            append("Runtime: ")
            append(tvShow.episode_run_time.joinToString(", ") { "$it min" })
        }
        // Builds and sets the average rating for the TV Show.
        binding.mediaRatingTextView.text =
            String.format(Locale.getDefault(), "%.1f", tvShow.vote_average)
        // Checks if the tv Show has ended, if so , displays the last air date
        if (tvShow.status == "Ended") {
            binding.endDateTextView.text = buildString {
                append("Last Air Date: ")
                append(tvShow.last_air_date ?: "Unknown")
            }
            // And hides the next episode information.
            binding.nextReleaseLayout.visibility = View.GONE
        } else {
            // Hides the end date if the tv Show has not ended
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
                append(attributes.startDate ?: getString(R.string.unknown))
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
            // Checks if there's an end date, if so , hides the next episode details.
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
     * This function displays an error message when media details are unavailable, and hides detailed
     * fields and showing default error messages.
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
     * This function fetches streaming providers for an anime by its ID, if available, retrieves
     * streamer names, otherwise, call handleNoProvidersAvailable to display an appropriate message.
     *
     * @param animeId The ID of the anime for which streaming providers are to be fetched.
     */
    private fun fetchStreamingProviders(animeId: String) {
        // Fetches the streaming links if available by given anime ID.
        detailsRepository.fetchAnimeStreamingLinks(animeId) { streamingLinks ->
            // First checks if the streaming links are available.
            if (!streamingLinks.isNullOrEmpty()) {
                // Iterate through each streaming link to fetch provider details.
                for (link in streamingLinks) {
                    fetchStreamerName(link)
                }
            } else {
                // Call this method to handle the case no providers are available.
                handleNoProvidersAvailable()
            }
        }
    }

    /**
     * This function fetches the name of the streaming provider based on the given streaming link.
     * Adds the provider to the adapter if the site name is available.
     *
     * @param streamingLink The streaming link used to gets the provider's name.
     */
    private fun fetchStreamerName(streamingLink: StreamingLink) {
        // Gets the streaming link ID.
        val streamerLinkId = streamingLink.id
        // Gets the streamer details based on the streaming link ID.
        detailsRepository.fetchStreamerDetails(streamerLinkId) { streamerDetailsResponse ->
            // Gets the streamer details.
            val streamer = streamerDetailsResponse?.data
            streamer?.attributes?.siteName?.let { siteName ->
                val provider = Provider(siteName, null)
                // Adds the provider to the providersAdapter to display it.
                providersAdapter.addProvider(provider)
            }
        }
    }

    /**
     * This function fetches watch providers based on the media type and ID
     * This function works only to fetch the movies and TV shows providers.
     */
    private fun fetchWatchProviders() {
        // Gets the watch providers based on the media type and ID.
        detailsRepository.fetchWatchProviders(mediaId, mediaType) { watchProvidersResponse ->
            // Checks if the response is not null.
            if (watchProvidersResponse != null) {
                // Gets the available regions from the response.
                val availableRegions = watchProvidersResponse.results.keys
                // Sets the region to user's country.
                val region = if (availableRegions.contains(countryCode)) {
                    countryCode
                } else {
                    // If the region country if not available, use 'US' country code.
                    "US"
                }
                val providers = watchProvidersResponse.results[region]
                // If flatrate providers are available, update adapter
                if (providers?.flatrate?.isNotEmpty() == true) {
                    providersAdapter.updateProviders(providers.flatrate)
                    // And hide no providers message.
                    binding.noProvidersTextView.visibility = View.GONE
                } else {
                    // Handle the case where no providers are available
                    handleNoProvidersAvailable()
                }
            } else {
                // Handle the case of an error fetching providers
                handleProviderFetchError()
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
        // First creates a minimal item instance based on the type of detailed item.
        val minimalItem: Any? = when (detailedItem) {
            is MovieDetails -> Movie(
                id = detailedItem.id,
                title = detailedItem.title,
                // If no overview is available then uses an empty overview.
                overview = detailedItem.overview ?: "",
                poster_path = detailedItem.poster_path,
                vote_average = detailedItem.vote_average,
                release_date = detailedItem.release_date,
                // Sets the item as liked.
                isLiked = true
            )
            is ShowDetails -> Show(
                id = detailedItem.id,
                name = detailedItem.name,
                // If no overview is available then uses an empty overview.
                overview = detailedItem.overview ?: "",
                poster_path = detailedItem.poster_path,
                vote_average = detailedItem.vote_average,
                first_air_date = detailedItem.first_air_date,
                // Sets the item as liked.
                isLiked = true
            )
            is AnimeDetails -> {
                // Extract the anime ID and attributes, checking for non-null values before creating an Anime instance.
                val animeId = detailedItem.data?.id?.toInt()
                val attributes = detailedItem.data?.attributes
                if (animeId != null && attributes != null) {
                    Anime(id = animeId, attributes = attributes, isLiked = true)
                } else null // Returns null if the ID or attributes are missing.
            }
            // Returns null for unsupported types.
            else -> null
        }
        // Now if the item is a valid media type, toggle its like status and update the UI icon.
        if (minimalItem is Movie || minimalItem is Show || minimalItem is Anime) {
            // Toggles like status from the shared preferences.
            likeButtonUtils.toggleLikeToItem(minimalItem)
            // Update icon after toggling
            setLikeButtonStatus(detailedItem)
        }
    }

    /**
     * This function updates the like button icon to reflect the current status of detailed item.
     *
     * @param detailedItem The detailed object of the media item to check like status for.
     */
    private fun setLikeButtonStatus(detailedItem: Any) {
        // Check the like status of the media item and assign it to isLiked.
        val isLiked = when (detailedItem) {
            // Converting the MovieDetails into a minimal Movie object.
            is MovieDetails -> likeButtonUtils.isItemLiked(
                Movie(
                    id = detailedItem.id,
                    title = detailedItem.title,
                    // If overview is null then use an empty string.
                    overview = detailedItem.overview ?: "",
                    // If poster is null then use an empty string
                    poster_path = detailedItem.poster_path ?: "",
                    vote_average = detailedItem.vote_average,
                    release_date = detailedItem.release_date
                )
            )
            // Converting the ShowDetails into a minimal Show object.
            is ShowDetails -> likeButtonUtils.isItemLiked(
                Show(
                    id = detailedItem.id,
                    name = detailedItem.name,
                    // If overview is null then use an empty string.
                    overview = detailedItem.overview ?: "",
                    // If poster is null then use an empty string
                    poster_path = detailedItem.poster_path ?: "",
                    vote_average = detailedItem.vote_average,
                    first_air_date = detailedItem.first_air_date
                )
            )
            // Converting the AnimeDetails into a minimal Anime object.
            is AnimeDetails -> {
                // Checking for non-null values before creating an Anime instance.
                val animeId = detailedItem.data?.id?.toInt()
                val title = detailedItem.data?.attributes?.canonicalTitle
                if (animeId != null && title != null) {
                    likeButtonUtils.isItemLiked(
                        Anime(id = animeId, attributes = detailedItem.data.attributes,
                            // Sets the initial isLiked status to false.
                            isLiked = false)
                    )
                } else false // Return false if either animeId or title is unavailable.
            }
            // Sets like status to false for unsupported types.
            else -> false
        }
        // Update the like button icon based on the isLiked status.
        binding.saveButton.setImageResource(
            if (isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
        )
    }

    /**
     * This function sets up the bottom navigation bar, configures each tab with its navigation behavior.
     */
    private fun setupBottomNavigation() {
        // Access the bottom navigation bar from the binding.
        val bottomNavigationView = binding.bottomNavigationView
        // Disable group checkable behavior, ensuring no selected item in the navigation bar.
        bottomNavigationView.menu.setGroupCheckable(0, false, true)
        // Sets the item selected listener to handel navigation.
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
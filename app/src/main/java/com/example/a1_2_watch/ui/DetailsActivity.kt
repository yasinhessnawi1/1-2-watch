// DetailsActivity.kt
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
import com.example.a1_2_watch.utils.NavigationUtils
import java.util.Locale
import kotlin.collections.joinToString

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: DetailsLayoutBinding
    private lateinit var providersAdapter: ProvidersAdapter
    private val detailsRepository = DetailsRepository()
    private var countryCode: String = Locale.getDefault().country
    private var mediaId: Int = -1
    private lateinit var mediaType: MediaType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        providersAdapter = ProvidersAdapter(emptyList())
        setupProvidersRecyclerView()
        binding.goBackButton.setOnClickListener {
            finish() // Finish activity when back button is pressed
        }
        setupBottomNavigation()
        extractMediaDataFromIntent()

        if (mediaId != -1) {
            fetchDetails()
        }

        fetchProvidersBasedOnMediaType()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.menu.setGroupCheckable(0, false, true)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    NavigationUtils.navigateToHome(this)
                    true
                }

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
    }

    private fun extractMediaDataFromIntent() {
        mediaId = intent.getIntExtra("MEDIA_ID", -1)
        val mediaTypeString = intent.getStringExtra("MEDIA_TYPE")
        mediaType = MediaType.valueOf(mediaTypeString ?: "MOVIES")
    }

    private fun fetchProvidersBasedOnMediaType() {
        if (mediaType == MediaType.ANIME) {
            fetchStreamingProviders(mediaId.toString())
        } else {
            fetchWatchProviders()
        }
    }

    private fun setupProvidersRecyclerView() {
        binding.providersRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.providersRecyclerView.adapter = providersAdapter
    }

    private fun fetchDetails() {
        detailsRepository.fetchDetails(mediaId, mediaType) { data ->
            runOnUiThread {
                when (data) {
                    is MovieDetails -> updateMovieDetails(data)
                    is ShowDetails -> updateTVShowDetails(data)
                    is AnimeDetails -> updateAnimeDetails(data)
                    else -> showError()
                }
            }
        }
    }

    private fun updateMovieDetails(movie: MovieDetails) {
        binding.titleTextView.text = movie.title
        binding.descriptionTextView.text = buildString {
            append("Description: \n ")
            append(movie.overview ?: getString(R.string.no_overview_available))
        }
        binding.releaseDateTextView.text = buildString {
            append("Release Date: ")
            append(movie.release_date)
        }
        binding.runtimeTextView.text = buildString {
            append("Runtime: ")
            append(movie.runtime.let { "$it minutes" })
        }
        binding.genresTextView.text = buildString {
            append("Genres: ")
            append(movie.genres.joinToString(", ") { it.name })
        }
        binding.revenueTextView.text = buildString {
            append("Revenue: \$")
            append(movie.revenue)
        }
        binding.budgetTextView.text = buildString {
            append("Budget: \$")
            append(movie.budget)
        }
        binding.mediaRatingTextView.text =
            String.format(Locale.getDefault(), "%.1f", movie.vote_average)

        Glide.with(this)
            .load(Constants.IMAGE_URL + movie.poster_path)
            //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
            //.error(R.drawable.ic_error) // Optional: Add an error image
            .into(binding.movieImageView)


        // Hide Anime-specific and TV Show-specific fields
        binding.nextReleaseLayout.visibility = View.GONE
        binding.endDateTextView.visibility = View.GONE
        binding.seasonCountTextView.visibility = View.GONE
        binding.episodeCountTextView.visibility = View.GONE
    }

    private fun updateTVShowDetails(tvShow: ShowDetails) {
        binding.titleTextView.text = tvShow.name
        binding.descriptionTextView.text =
            tvShow.overview ?: getString(R.string.no_overview_available)
        binding.releaseDateTextView.text = buildString {
            append("First Air Date: ")
            append(tvShow.first_air_date)
        }
        binding.seasonCountTextView.text = buildString {
            append("Seasons: ")
            append(tvShow.number_of_seasons)
        }
        binding.episodeCountTextView.text = buildString {
            append("Episodes: ")
            append(tvShow.number_of_episodes)
        }
        binding.runtimeTextView.text = buildString {
            append("Runtime: ")
            append(tvShow.episode_run_time.joinToString(", ") { "$it min" })
        }
        binding.mediaRatingTextView.text =
            String.format(Locale.getDefault(), "%.1f", tvShow.vote_average)
        if (tvShow.status == "Ended") {
            binding.endDateTextView.text = buildString {
                append("Last Air Date: ")
                append(tvShow.last_air_date)
            }
            binding.nextReleaseLayout.visibility = View.GONE
        } else {
            binding.endDateTextView.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.sand_clock)
                //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
                //.error(R.drawable.ic_error) // Optional: Add an error image
                .into(binding.sandClockView)
            binding.nextReleaseTextView.text = buildString {
                append("Next Episode to Air: ")
                append(tvShow.next_episode_to_air?.name)
            }
            binding.nextReleaseDateTextView.text = buildString {
                append("When? ")
                append(tvShow.next_episode_to_air?.air_date)
            }
        }

        binding.genresTextView.text = buildString {
            append("Genres: ")
            append(tvShow.genres.joinToString(", ") { it.name })
        }

        Glide.with(this)
            .load(Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path))
            //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
            //.error(R.drawable.ic_error) // Optional: Add an error image
            .into(binding.movieImageView)

        // Hide Anime-specific and Movie-specific fields
        binding.revenueTextView.visibility = View.GONE
        binding.budgetTextView.visibility = View.GONE
    }

    private fun updateAnimeDetails(animeDetails: AnimeDetails) {
        val animeData = animeDetails.data
        val attributes = animeData?.attributes
        if (attributes != null) {
            binding.titleTextView.text = attributes.canonicalTitle
            binding.descriptionTextView.text =
                attributes.synopsis ?: getString(R.string.no_overview_available)
            binding.releaseDateTextView.text = buildString {
                append("Start Date: ")
                append(attributes.startDate ?: getString(R.string.unknown))
            }
            binding.endDateTextView.text = buildString {
                append("End Date: ")
                append(attributes.startDate ?: getString(R.string.unknown))
            }
            //binding.genresTextView.text = "Genres: ${attributes.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown)}"
            val runtime = attributes.episodeLength.let { "$it min" }
            binding.seasonCountTextView.text = buildString {
                append("Runtime : ")
                append(runtime)
            }
            binding.episodeCountTextView.text = buildString {
                append("Episodes: ")
                append(attributes.episodeCount ?: getString(R.string.unknown))
            }
            binding.runtimeTextView.text = buildString {
                append("Episode Length: ")
                append(attributes.episodeLength)
            }
            binding.mediaRatingTextView.text = String.format(
                Locale.getDefault(),
                "%.1f",
                attributes.averageRating.toFloat() / 10
            )
            if (attributes.endDate != null) {
                binding.endDateTextView.text = buildString {
                    append("Last Air Date: ")
                    append(attributes.endDate)
                }
                binding.nextReleaseLayout.visibility = View.GONE
            } else {
                binding.endDateTextView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(R.drawable.sand_clock)
                    //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
                    //.error(R.drawable.ic_error) // Optional: Add an error image
                    .into(binding.sandClockView)
                binding.nextReleaseTextView.text = buildString {
                    append("Next Episode to Air: ")
                    append(attributes.nextRelease?.subSequence(0, 10))
                }
                binding.nextReleaseDateTextView.text = buildString {
                    append("When? ")
                    append(attributes.nextRelease?.subSequence(11, 16))
                }
            }
            Glide.with(this)
                .load(attributes.posterImage?.medium)
                //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
                //.error(R.drawable.ic_error) // Optional: Add an error image
                .into(binding.movieImageView)


            // Hide Movie-specific and TV Show-specific fields
            binding.genresTextView.visibility = View.GONE
            binding.revenueTextView.visibility = View.GONE
            binding.budgetTextView.visibility = View.GONE
        } else {
            showError()
        }
    }

    private fun showError() {
        binding.titleTextView.text = getString(R.string.no_title_available)
        binding.descriptionTextView.text = getString(R.string.no_overview_available)
        // Hide all other details
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

    private fun fetchStreamingProviders(animeId: String) {
        detailsRepository.fetchAnimeStreamingLinks(animeId) { streamingLinks ->
            if (streamingLinks != null && streamingLinks.isNotEmpty()) {
                for (link in streamingLinks) {
                    fetchStreamerName(link)
                }
            } else {
                handleNoProvidersAvailable()
            }
        }
    }


    private fun fetchStreamerName(streamingLink: StreamingLink) {
        val streamerLinkId = streamingLink.id
        detailsRepository.fetchStreamerDetails(streamerLinkId) { streamerDetailsResponse ->
            val streamer = streamerDetailsResponse?.data
            streamer?.attributes?.siteName?.let { siteName ->
                val provider = Provider(siteName, null)
                providersAdapter.addProvider(provider)
            }
        }
    }

    private fun fetchWatchProviders() {
        detailsRepository.fetchWatchProviders(mediaId, mediaType) { watchProvidersResponse ->
            if (watchProvidersResponse != null) {
                val availableRegions = watchProvidersResponse.results.keys
                val region = if (availableRegions.contains(countryCode)) {
                    countryCode
                } else {
                    "US" // Fallback to "US" or any preferred default
                }

                val providers = watchProvidersResponse.results[region]
                if (providers?.flatrate?.isNotEmpty() == true) {
                    providersAdapter.updateProviders(providers.flatrate)
                    binding.noProvidersTextView.visibility = View.GONE
                } else {
                    handleNoProvidersAvailable()
                }
            } else {
                handleProviderFetchError()
            }
        }
    }

    private fun handleNoProvidersAvailable() {
        providersAdapter.updateProviders(emptyList())
        binding.noProvidersTextView.visibility = View.VISIBLE
        binding.noProvidersTextView.text = "No available providers in your region."
    }

    private fun handleProviderFetchError() {
        binding.noProvidersTextView.visibility = View.VISIBLE
        binding.noProvidersTextView.text = "Error fetching providers."
    }
}
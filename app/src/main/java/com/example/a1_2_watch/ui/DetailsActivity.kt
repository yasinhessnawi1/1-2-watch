package com.example.a1_2_watch.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: DetailsLayoutBinding
    private lateinit var providersAdapter: ProvidersAdapter
    private val detailsRepository = DetailsRepository()
    private var countryCode = "US"
    private var mediaId: Int = -1
    private lateinit var mediaType: MediaType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ProvidersAdapter
        providersAdapter = ProvidersAdapter(emptyList())
        setupProvidersRecyclerView()

        setupBottomNavigation()
        extractMediaDataFromIntent()

        if (mediaId != -1) {
            fetchDetails()
        }

        setupRegionSpinner()
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

    private fun setupRegionSpinner() {
        val regions = listOf(
            "US", "NO", "GB", "FR", "DE", "IN", "CA", "AU",
            "ES", "IT", "JP", "KR", "BR", "NL", "RU", "MX", "SE", "TR"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, regions)
        binding.regionSpinner.adapter = adapter

        binding.regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                countryCode = regions[position]
                fetchWatchProviders()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupProvidersRecyclerView() {
        binding.providersRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        binding.titleTextView.text = movie.title ?: getString(R.string.no_title_available)
        binding.descriptionTextView.text = movie.overview ?: getString(R.string.no_overview_available)
        binding.releaseDateTextView.text = "Release Date: ${movie.release_date ?: getString(R.string.unknown)}"
        binding.runtimeTextView.text = "Runtime: ${movie.runtime?.let { "$it minutes" } ?: getString(R.string.unknown)}"
        binding.genresTextView.text = "Genres: ${movie.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown)}"
        binding.revenueTextView.text = "Revenue: \$${movie.revenue ?: getString(R.string.unknown)}"
        binding.budgetTextView.text = "Budget: \$${movie.budget ?: getString(R.string.unknown)}"

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
        binding.titleTextView.text = tvShow.name ?: getString(R.string.no_title_available)
        binding.descriptionTextView.text = tvShow.overview ?: getString(R.string.no_overview_available)
        binding.releaseDateTextView.text = "First Air Date: ${tvShow.first_air_date ?: getString(R.string.unknown)}"
        binding.seasonCountTextView.text = "Seasons: ${tvShow.number_of_seasons ?: getString(R.string.unknown)}"
        binding.episodeCountTextView.text = "Episodes: ${tvShow.number_of_episodes ?: getString(R.string.unknown)}"
        binding.runtimeTextView.text = "Runtime: ${tvShow.episode_run_time?.joinToString(", ") { "$it min" } ?: getString(R.string.unknown)}"
        if(tvShow.status == "Ended"){
            binding.endDateTextView.text = "Last Air Date: ${tvShow.last_air_date}"
        }else{
            binding.nextReleaseTextView.text = "Next Episode to Air: ${tvShow.next_episode_to_air?.name}"
            binding.nextReleaseDateTextView.text = "When? ${tvShow.next_episode_to_air?.air_date}"
        }

        binding.genresTextView.text = "Genres: ${tvShow.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown)}"

        Glide.with(this)
            .load(Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path))
            //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
            //.error(R.drawable.ic_error) // Optional: Add an error image
            .into(binding.movieImageView)

        // Hide Anime-specific and Movie-specific fields
        binding.nextReleaseLayout.visibility = View.GONE
        binding.endDateTextView.visibility = View.GONE
        binding.revenueTextView.visibility = View.GONE
        binding.budgetTextView.visibility = View.GONE
    }

    private fun updateAnimeDetails(animeDetails: AnimeDetails) {
        val animeData = animeDetails.data
        val attributes = animeData?.attributes
        if (attributes != null) {
            binding.titleTextView.text = attributes.canonicalTitle ?: getString(R.string.no_title_available)
            binding.descriptionTextView.text = attributes.synopsis ?: getString(R.string.no_overview_available)
            binding.releaseDateTextView.text = "Start Date: ${attributes.startDate ?: getString(R.string.unknown)}"
            binding.endDateTextView.text = "End Date: ${attributes.startDate ?: getString(R.string.unknown)}"
            //binding.genresTextView.text = "Genres: ${attributes.genres?.joinToString(", ") { it.name } ?: getString(R.string.unknown)}"
            binding.seasonCountTextView.text = "Runtime : ${attributes.episodeLength}"
            binding.episodeCountTextView.text = "Episodes: ${attributes.episodeCount ?: getString(R.string.unknown)}"
            if(attributes.endDate != null){
                binding.endDateTextView.text = "Last Air Date: ${attributes.endDate}"
            }else {
                binding.
                nextReleaseDateTextView.text =
                    "Next episode: ${attributes.nextRelease}"
            }
            Glide.with(this)
                .load(attributes.posterImage?.medium)
                //.placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder image
                //.error(R.drawable.ic_error) // Optional: Add an error image
                .into(binding.movieImageView)

            // Update Next Release Info
            if (attributes.nextRelease != null) {
                binding.nextReleaseLayout.visibility = View.VISIBLE
                binding.nextReleaseTextView.text = "Next Release:"
                binding.nextReleaseDateTextView.text = attributes.nextRelease
                // Optionally, set an image or animation for sandClockView
                binding.sandClockView.setImageResource(R.drawable.sand_clock) // Ensure you have this drawable
            } else {
                binding.nextReleaseLayout.visibility = View.GONE
            }

            // Hide Movie-specific and TV Show-specific fields
            binding.endDateTextView.visibility = View.GONE
            binding.runtimeTextView.visibility = View.GONE
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

    private fun fetchWatchProviders() {
        detailsRepository.fetchWatchProviders(mediaId, mediaType) { watchProvidersResponse ->
            runOnUiThread {
                if (watchProvidersResponse != null) {
                    val providers = watchProvidersResponse.results[countryCode]
                    if (providers?.flatrate != null && providers.flatrate.isNotEmpty()) {
                        providersAdapter.updateProviders(providers.flatrate)
                        binding.noProvidersTextView.visibility = View.GONE
                    } else {
                        providersAdapter.updateProviders(emptyList())
                        binding.noProvidersTextView.visibility = View.VISIBLE
                        binding.noProvidersTextView.text = getString(R.string.no_providers_available)
                    }
                } else {
                    providersAdapter.updateProviders(emptyList())
                    binding.noProvidersTextView.visibility = View.VISIBLE
                    binding.noProvidersTextView.text = getString(R.string.error_fetching_providers)
                }
        }
        }
    }
}
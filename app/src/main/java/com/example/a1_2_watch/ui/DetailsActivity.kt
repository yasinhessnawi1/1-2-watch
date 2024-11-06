// DetailsActivity.kt
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

        providersAdapter = ProvidersAdapter(emptyList())

        setupBottomNavigation()
        extractMediaDataFromIntent()

        if (mediaId != -1) {
            fetchDetails()
        }

        setupRegionSpinner()
        setupProvidersRecyclerView()
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
        binding.providersRecyclerView.layoutManager = LinearLayoutManager(this)
        providersAdapter = ProvidersAdapter(emptyList())
        binding.providersRecyclerView.adapter = providersAdapter
    }

    private fun fetchDetails() {
        detailsRepository.fetchDetails(mediaId, mediaType) { data ->
            when (data) {
                is MovieDetails -> updateMovieDetails(data)
                is ShowDetails -> updateTVShowDetails(data)
                is AnimeDetails -> updateAnimeDetails(data)
                else -> showError()
            }
        }
    }

    private fun updateMovieDetails(movie: MovieDetails) {
        binding.titleTextView.text = movie.title ?: "No Title Available"
        binding.descriptionTextView.text = movie.overview ?: "No Overview Available"
        binding.releaseDateTextView.text = "Release Date: ${movie.release_date ?: "Unknown"}"
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780/${movie.poster_path}")
            .into(binding.movieImageView)
    }

    private fun updateTVShowDetails(tvShow: ShowDetails) {
        binding.titleTextView.text = tvShow.name ?: "No Title Available"
        binding.descriptionTextView.text = tvShow.overview ?: "No Overview Available"
        binding.releaseDateTextView.text = "First Air Date: ${tvShow.first_air_date ?: "Unknown"}"
        Glide.with(this)
            .load(Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path))
            .into(binding.movieImageView)
    }

    private fun updateAnimeDetails(animeDetails: AnimeDetails) {
        val animeData = animeDetails.data
        val attributes = animeData?.attributes
        if (attributes != null) {
            binding.titleTextView.text = attributes.canonicalTitle ?: "No Title Available"
            binding.descriptionTextView.text = attributes.synopsis ?: "No Overview Available"
            binding.releaseDateTextView.text = "Start Date: ${attributes.startDate ?: "Unknown"}"
            attributes.posterImage?.medium?.let { posterUrl ->
                Glide.with(this).load(posterUrl).into(binding.movieImageView)
            }
            fetchStreamingProviders(mediaId.toString())
        } else {
            showError()
        }
    }

    private fun showError() {
        binding.titleTextView.text = "No Title Available"
        binding.descriptionTextView.text = "No Overview Available"
    }

    private fun fetchStreamingProviders(animeId: String) {
        detailsRepository.fetchAnimeStreamingLinks(animeId) { streamingLinks ->
            if (!streamingLinks.isNullOrEmpty()) {
                for (link in streamingLinks) {
                    fetchStreamerName(link)
                }
            } else {
                binding.providerNameTextView.text = "No streaming providers available"
            }
        }
    }

    private fun fetchStreamerName(streamingLink: StreamingLink) {
        val streamerLinkId = streamingLink.id
        detailsRepository.fetchStreamerDetails(streamerLinkId) { streamerDetailsResponse ->
            val streamer = streamerDetailsResponse?.data
            streamer?.attributes?.siteName?.let { siteName ->
                binding.providerNameTextView.append("Available on: $siteName\n")
            }
        }
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
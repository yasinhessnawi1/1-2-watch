package com.example.a1_2_watch.repository

import android.content.Context
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.example.a1_2_watch.adapters.ProvidersAdapter
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.data.AnimeStreamingLinksResponse
import com.example.a1_2_watch.data.StreamerDetailsResponse
import com.example.a1_2_watch.data.StreamingLink
import com.example.a1_2_watch.data.WatchProvidersResponse
import com.example.a1_2_watch.databinding.DetailsLayoutBinding
import com.example.a1_2_watch.models.AnimeDetails
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.models.MovieDetails
import com.example.a1_2_watch.models.ShowDetails
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.Constants.KITSU_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsHandler {

    private val apiKey = Constants.API_KEY

    // Main function to handle fetching details based on media type
    fun fetchDetails(
        mediaId: Int,
        mediaType: MediaType,
        context: Context,
        binding: DetailsLayoutBinding
    ) {
        when (mediaType) {
            MediaType.MOVIES -> fetchMovieDetails(mediaId, context, binding)
            MediaType.TV_SHOWS -> fetchTVShowDetails(mediaId, context, binding)
            MediaType.ANIME -> fetchAnimeDetails(
                mediaId.toString(),
                context,
                binding
            ) // Convert animeId to String
        }
    }

    private fun fetchMovieDetails(movieId: Int, context: Context, binding: DetailsLayoutBinding) {
        ApiClient.getApiService().getMovieDetails(movieId, apiKey)
            .enqueue(object : Callback<MovieDetails> {
                override fun onResponse(
                    call: Call<MovieDetails>,
                    response: Response<MovieDetails>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { movie ->
                            binding.titleTextView.text = movie.title ?: "No Title Available"
                            binding.descriptionTextView.text =
                                movie.overview ?: "No Overview Available"
                            binding.releaseDateTextView.text =
                                "Release Date: ${movie.release_date ?: "Unknown"}"
                            Glide.with(context)
                                .load("https://image.tmdb.org/t/p/w780/${movie.poster_path}")
                                .into(binding.movieImageView)
                        }
                    } else {
                        Log.e(
                            "DetailsHandler",
                            "Failed to fetch movie details: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                    Log.e("DetailsHandler", "Failed to fetch movie details", t)
                }
            })
    }

    private fun fetchTVShowDetails(tvId: Int, context: Context, binding: DetailsLayoutBinding) {
        ApiClient.getApiService().getTVShowDetails(tvId, apiKey)
            .enqueue(object : Callback<ShowDetails> {
                override fun onResponse(call: Call<ShowDetails>, response: Response<ShowDetails>) {
                    if (response.isSuccessful) {
                        response.body()?.let { tvShow ->
                            binding.titleTextView.text = tvShow.name ?: "No Title Available"
                            binding.descriptionTextView.text =
                                tvShow.overview ?: "No Overview Available"
                            binding.releaseDateTextView.text =
                                "First Air Date: ${tvShow.first_air_date ?: "Unknown"}"
                            Glide.with(context).load(
                                Constants.IMAGE_URL + (tvShow.poster_path ?: tvShow.backdrop_path)
                            )
                                .into(binding.movieImageView)
                        }
                    } else {
                        Log.e(
                            "DetailsHandler",
                            "Failed to fetch TV show details: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ShowDetails>, t: Throwable) {
                    Log.e("DetailsHandler", "Failed to fetch TV show details", t)
                }
            })
    }

    private fun fetchAnimeDetails(
        animeId: String,
        context: Context,
        binding: DetailsLayoutBinding
    ) {
        ApiClient.getApiService(KITSU_URL).getAnimeDetails(animeId)
            .enqueue(object : Callback<AnimeDetails> {
                override fun onResponse(
                    call: Call<AnimeDetails>,
                    response: Response<AnimeDetails>
                ) {
                    if (response.isSuccessful) {
                        val animeData = response.body()?.data
                        val attributes = animeData?.attributes
                        if (attributes != null) {
                            binding.titleTextView.text =
                                attributes.canonicalTitle ?: "No Title Available"
                            binding.descriptionTextView.text =
                                attributes.synopsis ?: "No Overview Available"
                            binding.releaseDateTextView.text =
                                "Start Date: ${attributes.startDate ?: "Unknown"}"
                            attributes.posterImage?.medium?.let { posterUrl ->
                                Glide.with(context).load(posterUrl).into(binding.movieImageView)
                            }
                            fetchStreamingProviders(animeId, binding)
                        } else {
                            Log.e("DetailsHandler", "Attributes or data is missing in response")
                            binding.titleTextView.text = "No Title Available"
                            binding.descriptionTextView.text = "No Overview Available"
                        }
                    } else {
                        Log.e(
                            "DetailsHandler",
                            "Failed to fetch anime details: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<AnimeDetails>, t: Throwable) {
                    Log.e("DetailsHandler", "Failed to fetch anime details", t)
                }
            })
    }

    private fun fetchStreamingProviders(animeId: String, binding: DetailsLayoutBinding) {
        ApiClient.getApiService(KITSU_URL).getAnimeStreamingLinks(animeId)
            .enqueue(object : Callback<AnimeStreamingLinksResponse> {
                override fun onResponse(
                    call: Call<AnimeStreamingLinksResponse>,
                    response: Response<AnimeStreamingLinksResponse>
                ) {
                    if (response.isSuccessful) {
                        val streamingLinks = response.body()?.data ?: emptyList()
                        if (streamingLinks.isNotEmpty()) {
                            for (link in streamingLinks) {
                                fetchStreamerName(link, binding)
                            }
                        } else {
                            binding.providerNameTextView.text = "No streaming providers available"
                        }
                    } else {
                        Log.e(
                            "DetailsHandler",
                            "Failed to fetch streaming links: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<AnimeStreamingLinksResponse>, t: Throwable) {
                    Log.e("DetailsHandler", "Failed to fetch streaming links", t)
                }
            })
    }

    private fun fetchStreamerName(streamingLink: StreamingLink, binding: DetailsLayoutBinding) {
        val streamerLinkId = streamingLink.id
        ApiClient.getApiService(KITSU_URL).getStreamerDetails(streamerLinkId)
            .enqueue(object : Callback<StreamerDetailsResponse> {
                override fun onResponse(
                    call: Call<StreamerDetailsResponse>,
                    response: Response<StreamerDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        val streamer = response.body()?.data
                        streamer?.attributes?.siteName?.let { siteName ->
                            binding.providerNameTextView.append("Available on: $siteName\n")
                        }
                    } else {
                        Log.e(
                            "DetailsHandler",
                            "Failed to fetch streamer name: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<StreamerDetailsResponse>, t: Throwable) {
                    Log.e("DetailsHandler", "Failed to fetch streamer name", t)
                }
            })
    }

    fun fetchWatchProviders(
        mediaId: Int,
        mediaType: MediaType,
        countryCode: String,
        adapter: ProvidersAdapter,
        binding: DetailsLayoutBinding
    ) {
        val call: Call<WatchProvidersResponse>? = when (mediaType) {
            MediaType.MOVIES -> ApiClient.getApiService().getWatchProviders(mediaId, apiKey)
            MediaType.TV_SHOWS -> ApiClient.getApiService().getTVShowWatchProviders(mediaId, apiKey)
            MediaType.ANIME -> null // Anime does not have watch providers in this setup
        }

        if (call != null) {
            call.enqueue(object : Callback<WatchProvidersResponse> {
                override fun onResponse(
                    call: Call<WatchProvidersResponse>,
                    response: Response<WatchProvidersResponse>
                ) {
                    if (response.isSuccessful) {
                        val providers = response.body()?.results?.get(countryCode)
                        if (providers != null && providers.flatrate != null && providers.flatrate.isNotEmpty()) {
                            adapter.updateProviders(providers.flatrate)
                            binding.noProvidersTextView.visibility = View.GONE
                        } else {
                            adapter.updateProviders(emptyList())
                            binding.noProvidersTextView.visibility = View.VISIBLE
                            binding.noProvidersTextView.text =
                                "No available providers in this region."
                        }
                    } else {
                        Log.e(
                            "DetailsRepository",
                            "Failed to fetch watch providers: ${response.message()}"
                        )
                        binding.noProvidersTextView.visibility = View.VISIBLE
                        binding.noProvidersTextView.text = "Error fetching providers."
                    }
                }

                override fun onFailure(call: Call<WatchProvidersResponse>, t: Throwable) {
                    Log.e("DetailsRepository", "Failed to fetch watch providers", t)
                    binding.noProvidersTextView.visibility = View.VISIBLE
                    binding.noProvidersTextView.text = "Error fetching providers."
                }
            })
        } else {
            binding.noProvidersTextView.visibility = View.VISIBLE
            binding.noProvidersTextView.text = ""
        }
    }
}

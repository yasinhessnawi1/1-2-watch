package com.example.a1_2_watch.repository
import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailsRepository {

    private val apiKey = Constants.API_KEY

    // Generic function to handle API calls
    private fun <T> fetchData(call: Call<T>, callback: (T?) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e(
                        "DetailsRepository",
                        "API call failed: ${response.message()}"
                    )
                    callback(null)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e("DetailsRepository", "API call failed", t)
                callback(null)
            }
        })
    }

    // Fetch details based on media type
    fun fetchDetails(mediaId: Int, mediaType: MediaType, callback: (Any?) -> Unit) {
        when (mediaType) {
            MediaType.MOVIES -> {
                val call = ApiClient.getApiService().getMovieDetails(mediaId, apiKey)
                fetchData(call, callback)
            }
            MediaType.TV_SHOWS -> {
                val call = ApiClient.getApiService().getTVShowDetails(mediaId, apiKey)
                fetchData(call, callback)
            }
            MediaType.ANIME -> {
                val call = ApiClient.getApiService(Constants.KITSU_URL).getAnimeDetails(mediaId.toString())
                fetchData(call, callback)
            }
        }
    }

    // Fetch watch providers
    fun fetchWatchProviders(
        mediaId: Int,
        mediaType: MediaType,
        callback: (WatchProvidersResponse?) -> Unit
    ) {
        val call: Call<WatchProvidersResponse>? = when (mediaType) {
            MediaType.MOVIES -> ApiClient.getApiService().getWatchProviders(mediaId, apiKey)
            MediaType.TV_SHOWS -> ApiClient.getApiService().getTVShowWatchProviders(mediaId, apiKey)
            MediaType.ANIME -> null
        }

        if (call != null) {
            fetchData(call, callback)
        } else {
            callback(null)
        }
    }

    // Fetch anime streaming links
    fun fetchAnimeStreamingLinks(animeId: String, callback: (List<StreamingLink>?) -> Unit) {
        val call = ApiClient.getApiService(Constants.KITSU_URL).getAnimeStreamingLinks(animeId)
        fetchData(call) { response: AnimeStreamingLinksResponse? ->
            callback(response?.data)
        }
    }

    // Fetch streamer details
    fun fetchStreamerDetails(streamerLinkId: String, callback: (StreamerDetailsResponse?) -> Unit) {
        val call = ApiClient.getApiService(Constants.KITSU_URL).getStreamerDetails(streamerLinkId)
        fetchData(call, callback)
    }
}
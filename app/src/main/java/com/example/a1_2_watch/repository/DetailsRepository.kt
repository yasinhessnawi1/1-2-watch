package com.example.a1_2_watch.repository
import android.util.Log
import com.example.a1_2_watch.api.ApiClient
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * DetailsRepository class responsible for fetching detailed information about any media items, like movies,
 * tv shows, or Anime.
 */
class DetailsRepository {
    // The API key used to authenticate API requests For TMDB.
    private val apiKey = Constants.API_KEY

    /**
     * This function is a generic function to handle API requests and process responses.
     *
     * @param call The Retrofit call object representing the API request.
     * @param callback A lambda function that receives the API response or null when request fails.
     */
    private fun <T> fetchData(call: Call<T>, callback: (T?) -> Unit) {
        call.enqueue(object : Callback<T> {
            /**
             * This function called when the API response is received.
             *
             * @param call The API call.
             * @param response The API response.
             */
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                if (response.isSuccessful) {
                    // If successful, passes the response body to the callback.
                    callback(response.body())
                } else {
                    // Logs error and passes null to callback.
                    Log.e(
                        "DetailsRepository",
                        "API call failed: ${response.message()}"
                    )
                    callback(null)
                }
            }

            /**
             * This function called when the API call fails.
             *
             * @param call The API call.
             * @param t The throwable that representing the error.
             */
            override fun onFailure(call: Call<T>, t: Throwable) {
                // Logs error and passes null to callback.
                Log.e("DetailsRepository", "API call failed", t)
                callback(null)
            }
        })
    }

    /**
     * This function fetches detailed information about any media item based on its type.
     *
     * @param mediaId The unique ID of the media item
     * @param mediaType The type of the media item (movie, tv show, or anime).
     * @param callback A lambda function that receives the fetched details or null if the request fails.
     */
    fun fetchDetails(mediaId: Int, mediaType: MediaType, callback: (Any?) -> Unit) {
        // Check the media type
        when (mediaType) {
            MediaType.MOVIES -> {
                // Fetch movie details by movie ID.
                val call = ApiClient.getApiService().getMovieDetails(mediaId, apiKey)
                fetchData(call, callback)
            }
            MediaType.TV_SHOWS -> {
                // Fetch TV show details by show ID.
                val call = ApiClient.getApiService().getTVShowDetails(mediaId, apiKey)
                fetchData(call, callback)
            }
            MediaType.ANIME -> {
                // Fetch anime details by anime ID (using the kitsu API).
                val call = ApiClient.getApiService(Constants.KITSU_URL).getAnimeDetails(mediaId.toString())
                fetchData(call, callback)
            }
        }
    }

    /**
     * This function fetches information about which providers are available for this media item.
     *
     * @param mediaId The unique ID of the media item.
     * @param mediaType The type of the media item (movie, tv show, but not for anime)
     * @param callback A lambda function that receives the fetched provider details or null if request failed.
     */
    fun fetchWatchProviders(
        mediaId: Int,
        mediaType: MediaType,
        callback: (WatchProvidersResponse?) -> Unit
    ) {
        // Determine the correct API call (This call just for movie and Tv show.) by using TMDB API.
        val call: Call<WatchProvidersResponse>? = when (mediaType) {
            MediaType.MOVIES -> ApiClient.getApiService().getWatchProviders(mediaId, apiKey)
            MediaType.TV_SHOWS -> ApiClient.getApiService().getTVShowWatchProviders(mediaId, apiKey)
            MediaType.ANIME -> null
        }
        // If call is not null, proceed the fetch data.
        if (call != null) {
            fetchData(call, callback)
        } else {
            // Else pass null to the callback. (for example in anime case)
            callback(null)
        }
    }

    /**
     * This function fetches available streaming links for a given anime based on his ID.
     *
     * @param animeId The unique ID of the anime.
     * @param callback A lambda function that receives the list of available streaming links.
     */
    fun fetchAnimeStreamingLinks(animeId: String, callback: (List<StreamingLink>?) -> Unit) {
        // Make an API call to fetch streaming links for the given anime based on his ID.
        val call = ApiClient.getApiService(Constants.KITSU_URL).getAnimeStreamingLinks(animeId)
        fetchData(call) { response: AnimeStreamingLinksResponse? ->
            // Pass the link data from the response to the callback.
            callback(response?.data)
        }
    }

    /**
     * This function fetches details about a given streaming service based on his ID.
     *
     * @param streamerLinkId The unique ID of the streaming service link.
     * @param callback A lambda function that receives the streamer details.
     */
    fun fetchStreamerDetails(streamerLinkId: String, callback: (StreamerDetailsResponse?) -> Unit) {
        // Make an API call to fetch details about the given streaming service.
        val call = ApiClient.getApiService(Constants.KITSU_URL).getStreamerDetails(streamerLinkId)
        fetchData(call, callback)
    }
}
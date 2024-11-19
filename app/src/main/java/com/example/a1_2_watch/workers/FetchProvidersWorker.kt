package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.a1_2_watch.models.*
import com.example.a1_2_watch.repository.DetailsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Worker for fetching watch provider details for Movies, TV Shows, or Anime.
 * This worker uses the WorkManager API to perform background tasks.
 *
 * @param context The application context.
 * @param params The worker parameters, including input data and runtime constraints.
 */
class FetchProvidersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        // Keys for input and output data for WorkManager.
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE" // Input key for the media type.
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID" // Input key for the media ID.
        const val KEY_COUNTRY_CODE = "KEY_COUNTRY_CODE" // Input key for the country code.
        const val KEY_RESULT =
            "KEY_RESULT" // Output key for the fetched provider data in JSON format.
        private const val TAG = "FetchProvidersWorker"
    }

    // Gson instance for serializing and deserializing JSON data.
    private val gson = Gson()

    // Repository responsible for fetching detailed information about media items and providers.
    private val detailsRepository = DetailsRepository()

    /**
     * Executes the work to fetch watch providers for the given media type and ID.
     * Filters the providers based on the user's country code or aggregates providers from all regions.
     *
     * @return [Result.success] with the fetched provider data if successful,
     *         [Result.failure] if input data is invalid or unsupported, or
     *         [Result.retry] in case of exceptions.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve input data.
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)
            val countryCode = inputData.getString(KEY_COUNTRY_CODE) ?: Locale.getDefault().country

            // Validate input data.
            if (mediaId == -1) {
                Log.e(TAG, "Invalid media ID: $mediaId")
                return@withContext Result.failure()
            }

            // Fetch and process provider data based on the media type.
            val resultJson: String = when (mediaType) {
                MediaType.MOVIES, MediaType.TV_SHOWS -> {
                    val watchProvidersResponse =
                        detailsRepository.fetchWatchProviders(mediaId, mediaType)

                    // Handle null responses.
                    if (watchProvidersResponse == null) {
                        Log.e(TAG, "No watch providers found for mediaId: $mediaId, mediaType: $mediaType")
                        return@withContext Result.failure()
                    }

                    val filteredProviders = filterProvidersByRegion(watchProvidersResponse.results, countryCode)

                    val responseToReturn: WatchProvidersResponse = if (filteredProviders.isNotEmpty()) {
                        WatchProvidersResponse(
                            id = mediaId,
                            results = filteredProviders
                        )
                    } else {
                        val aggregatedProviders =
                            extractUniqueProvidersFromAllRegions(watchProvidersResponse.results)
                        WatchProvidersResponse(id = mediaId, results = aggregatedProviders)
                    }

                    gson.toJson(responseToReturn) // Convert response to JSON format.
                }

                MediaType.ANIME -> {
                    val streamingLinks =
                        detailsRepository.fetchAnimeStreamingLinks(mediaId.toString())
                    val providers = mutableListOf<Provider>()

                    // Process streaming links to fetch provider details.
                    if (!streamingLinks.isNullOrEmpty()) {
                        for (link in streamingLinks) {
                            val streamerDetailsResponse =
                                detailsRepository.fetchStreamerDetails(link.id)
                            streamerDetailsResponse?.data?.attributes?.siteName?.let { siteName ->
                                providers.add(Provider(providerName = siteName, logoPath = null))
                            }
                        }
                    }

                    gson.toJson(providers) // Convert providers list to JSON format.
                }

                else -> {
                    Log.e(TAG, "Unsupported media type: $mediaType")
                    return@withContext Result.failure()
                }
            }

            // Prepare and return the output data.
            val output = workDataOf(KEY_RESULT to resultJson)
            Result.success(output)
        } catch (e: Exception) {
            // Log the exception and retry the work.
            Log.e(TAG, "Error fetching watch providers", e)
            Result.retry()
        }
    }

    /**
     * Filters the watch providers response based on the specified country code.
     *
     * @param results The original map of watch providers grouped by region.
     * @param countryCode The country code to filter providers for.
     * @return A filtered map containing providers specific to the given country.
     */
    private fun filterProvidersByRegion(
        results: Map<String, ProvidersRegion>,
        countryCode: String
    ): Map<String, ProvidersRegion> {
        return when {
            results.containsKey(countryCode) -> {
                mapOf(countryCode to results[countryCode]!!)
            }

            results.containsKey("US") -> {
                mapOf("US" to results["US"]!!)
            }

            else -> {
                // Log that no specific region was found and return an empty map.
                Log.w(TAG, "No region-specific providers found for countryCode: $countryCode")
                emptyMap()
            }
        }
    }

    /**
     * Aggregates unique providers from all regions in the watch providers response.
     *
     * @param results The original map of watch providers grouped by region.
     * @return A map containing a single key "ALL" with unique providers aggregated from all regions.
     */
    private fun extractUniqueProvidersFromAllRegions(
        results: Map<String, ProvidersRegion>
    ): Map<String, ProvidersRegion> {
        val uniqueProvidersMap = mutableMapOf<String, Provider>()

        // Iterate through all providers in each region and collect unique entries.
        for (providersRegion in results.values) {
            providersRegion.flatrate?.let { providersList ->
                for (provider in providersList) {
                    if (!uniqueProvidersMap.containsKey(provider.providerName)) {
                        uniqueProvidersMap[provider.providerName] = provider
                    }
                }
            }
        }

        // Log the aggregation of unique providers.
        Log.d(TAG, "Aggregated unique providers from all regions: ${uniqueProvidersMap.size}")

        // Wrap the unique providers in a ProvidersRegion object under the key "ALL".
        val allProvidersRegion = ProvidersRegion(flatrate = uniqueProvidersMap.values.toList())
        return mapOf("ALL" to allProvidersRegion)
    }
}

// FetchProvidersWorker.kt
package com.example.a1_2_watch.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.models.Provider
import com.example.a1_2_watch.models.ProvidersRegion
import com.example.a1_2_watch.models.WatchProvidersResponse
import com.example.a1_2_watch.repository.DetailsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class FetchProvidersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
        const val KEY_COUNTRY_CODE = "KEY_COUNTRY_CODE"
        const val KEY_RESULT = "KEY_RESULT"
    }

    private val gson = Gson()
    private val detailsRepository = DetailsRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve input parameters
            val mediaTypeStr = inputData.getString(KEY_MEDIA_TYPE)
            val mediaType = MediaType.valueOf(mediaTypeStr ?: return@withContext Result.failure())
            val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)
            val countryCode = inputData.getString(KEY_COUNTRY_CODE) ?: Locale.getDefault().country

            if (mediaId == -1) return@withContext Result.failure()

            Log.d("FetchProvidersWorker", "MediaType: $mediaType, MediaID: $mediaId, CountryCode: $countryCode")

            val resultJson: String = when (mediaType) {
                MediaType.MOVIES, MediaType.TV_SHOWS -> {
                    val watchProvidersResponse = detailsRepository.fetchWatchProviders(mediaId, mediaType)

                    if (watchProvidersResponse == null) {
                        return@withContext Result.failure()
                    }

                    val filteredProviders = filterProvidersByRegion(watchProvidersResponse.results, countryCode)

                    val responseToReturn: WatchProvidersResponse = if (filteredProviders.isNotEmpty()) {
                        WatchProvidersResponse(
                            id = mediaId,
                            results = filteredProviders
                        )
                    } else {
                        // Aggregate unique providers from all regions
                        val aggregatedProviders = extractUniqueProvidersFromAllRegions(watchProvidersResponse.results)
                        WatchProvidersResponse(
                            id = mediaId,
                            results = aggregatedProviders
                        )
                    }

                    gson.toJson(responseToReturn)
                }
                MediaType.ANIME -> {
                    // Anime processing remains unchanged
                    val streamingLinks = detailsRepository.fetchAnimeStreamingLinks(mediaId.toString())
                    val providers = mutableListOf<Provider>()
                    if (!streamingLinks.isNullOrEmpty()) {
                        for (link in streamingLinks) {
                            val streamerDetailsResponse = detailsRepository.fetchStreamerDetails(link.id)
                            streamerDetailsResponse?.data?.attributes?.siteName?.let { siteName ->
                                providers.add(Provider(providerName = siteName, logoPath = null))
                            }
                        }
                    }
                    gson.toJson(providers)
                }
                else -> {
                    // Unsupported media type
                    return@withContext Result.failure()
                }
            }

            Log.d("FetchProvidersWorker", "Result JSON: $resultJson")

            val output = workDataOf(KEY_RESULT to resultJson)
            Result.success(output)
        } catch (e: Exception) {
            Log.e("FetchProvidersWorker", "Error fetching providers: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Filters the watch providers response based on the user's country code.
     *
     * @param results The original watch providers results map.
     * @param countryCode The user's country code.
     * @return A filtered map containing only the providers for the specified country.
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
                // Return empty map to indicate no region-specific providers
                emptyMap()
            }
        }
    }

    /**
     * Extracts unique providers from all regions in the watchProvidersResponse.
     *
     * @param results The original watch providers results map.
     * @return A map containing unique providers under the key "ALL".
     */
    private fun extractUniqueProvidersFromAllRegions(
        results: Map<String, ProvidersRegion>
    ): Map<String, ProvidersRegion> {
        val uniqueProvidersMap = mutableMapOf<String, Provider>()

        // Iterate through all regions
        for (providersRegion in results.values) {
            // For each category: flatrate, rent, buy
            providersRegion.flatrate?.let { providersList ->
                for (provider in providersList) {
                    if (!uniqueProvidersMap.containsKey(provider.providerName)) {
                        uniqueProvidersMap[provider.providerName] = provider
                    }
                }
            }
        }

        // Wrap the list of unique providers in a ProvidersRegion object under "ALL"
        val allProvidersRegion = ProvidersRegion(
            flatrate = uniqueProvidersMap.values.toList(),
        )

        return mapOf("ALL" to allProvidersRegion)
    }
}

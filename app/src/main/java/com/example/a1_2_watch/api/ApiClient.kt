package com.example.a1_2_watch.api

import com.example.a1_2_watch.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient object for providing the Retrofit client with the correct API instances.
 */
object ApiClient {

    /**
     * This method creates a new Retrofit client with the given base URL.
     *
     * @param baseUrl The base URL for the API requests, The Default URL will be TMBDs base URL.
     * @return A new Retrofit client instance.
     */
    fun getApiClient(baseUrl: String = Constants.TMDB_URL): Retrofit {
        return Retrofit.Builder()                                 // Create the Retrofit Builder
            .baseUrl(baseUrl)                                     // Set the base URL for the API requests.
            .addConverterFactory(GsonConverterFactory.create())   // Add the converter for JSON parsing.
            .build()                                              // Build the Retrofit client instance.
    }

    /**
     * This method creates an instance of the API service for making API calls.
     *
     * @param baseUrl The base URL for the API requests, The Default URL will be TMBDs base URL.
     * @return A new instance of the API service.
     */
    fun getApiService(baseUrl: String = Constants.TMDB_URL): ApiService {
        // Create and return an instance of the API service.
        return getApiClient(baseUrl).create(ApiService::class.java)
    }
}
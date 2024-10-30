package com.example.a1_2_watch.api

import com.example.a1_2_watch.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    fun getApiClient(baseUrl: String = Constants.TMDB_URL): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(baseUrl: String = Constants.TMDB_URL): ApiService {
        return getApiClient(baseUrl).create(ApiService::class.java)
    }
}
package com.example.a1_2_watch.data

data class Provider(
    val provider_name: String,
    val logo_path: String,
)

data class WatchProvidersResponse(
    val results: Map<String, WatchProviderDetails> // Mapping country codes to providers
)

data class WatchProviderDetails(
    val link: String, // Link to the provider page on TMDB
    val flatrate: List<Provider>? // List of streaming services (optional)
)
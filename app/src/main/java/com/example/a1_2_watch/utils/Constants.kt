package com.example.a1_2_watch.utils

/**
 * Constants used in the application for the API keys and base URLs for the TMBD API, and
 * Kitsu.
 */
object Constants {
    // The API key for authentication requests to the TMBD API.
    const val API_KEY = "785f5aad8965f81b1162d186aea1074a"
    // The Base URL for the TMBD API requests.
    const val TMDB_URL= "https://api.themoviedb.org/3/"
    // The Base URL for fetching images from the Kitsu API.
    const val IMAGE_URL = "https://image.tmdb.org/t/p/w500"
    // The Base URL for YouTube videos which is used as a trailer (For the Kitsu API).
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v="
    // The Base URL for Kitsu API requests.
    const val KITSU_URL = "https://kitsu.io/api/edge/"
    const val PLACEHOLDER_URL = "https://placehold.co/600x400/000000/FFFFFF.png?text=No+Image&font=roboto"
}
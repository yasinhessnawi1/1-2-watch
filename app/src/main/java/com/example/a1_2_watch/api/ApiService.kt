package com.example.a1_2_watch.api

import com.example.a1_2_watch.data.AnimeStreamingLinksResponse
import com.example.a1_2_watch.data.StreamerDetailsResponse
import com.example.a1_2_watch.data.WatchProvidersResponse
import com.example.a1_2_watch.models.MovieDetails
import com.example.a1_2_watch.models.MovieResponse
import com.example.a1_2_watch.models.AnimeDetails
import com.example.a1_2_watch.models.AnimeResponse
import com.example.a1_2_watch.models.ShowDetails
import com.example.a1_2_watch.models.ShowResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Fetch popular movies from TMDB
    @GET("movie/popular")
    fun getPopularMovies(@Query("api_key") apiKey: String, @Query("page") page: Int): Call<MovieResponse>

    // Fetch movie details by ID
    @GET("movie/{movie_id}")
    fun getMovieDetails(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<MovieDetails>

    // Fetch popular TV shows from TMDB
    @GET("tv/popular")
    fun getPopularTVShows(@Query("api_key") apiKey: String, @Query("page") page: Int): Call<ShowResponse>

    // Fetch TV show details by ID
    @GET("tv/{tv_id}")
    fun getTVShowDetails(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String): Call<ShowDetails>

    // Fetch popular anime from Kitsu
    @GET("anime")
    fun getPopularAnimeKitsu(@Query("page[limit]") limit: Int, @Query("page[offset]") offset: Int): Call<AnimeResponse>

    // Fetch anime details from Kitsu
    @GET("anime/{anime_id}")
    fun getAnimeDetails(@Path("anime_id") animeId: String): Call<AnimeDetails>

    // Fetch watch providers for a movie by ID
    @GET("movie/{movie_id}/watch/providers")
    fun getWatchProviders(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<WatchProvidersResponse>

    // New method for fetching watch providers for TV shows
    @GET("tv/{tv_id}/watch/providers")
    fun getTVShowWatchProviders(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String): Call<WatchProvidersResponse>

    @GET("anime/{anime_id}/streaming-links")
    fun getAnimeStreamingLinks(@Path("anime_id") animeId: String): Call<AnimeStreamingLinksResponse>

    @GET("streaming-links/{streaming_link_id}/streamer")
    fun getStreamerDetails(@Path("streaming_link_id") linkId: String): Call<StreamerDetailsResponse>

    // Movie search endpoint
    @GET("search/movie")
    fun searchMovies(@Query("api_key") apiKey: String, @Query("query") query: String): Call<MovieResponse>

    // TV show search endpoint
    @GET("search/tv")
    fun searchTVShows(@Query("api_key") apiKey: String, @Query("query") query: String): Call<ShowResponse>

    // Anime search endpoint (Kitsu)
    @GET("anime")
    fun searchAnime(@Query("filter[text]") query: String): Call<AnimeResponse>

}

package com.example.a1_2_watch.api

import com.example.a1_2_watch.moduls.MovieDetails
import com.example.a1_2_watch.moduls.MovieResponse
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
    fun getPopularTVShows(@Query("api_key") apiKey: String, @Query("page") page: Int): Call<MovieResponse>

    // Fetch TV show details by ID
    @GET("tv/{tv_id}")
    fun getTVShowDetails(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String): Call<MovieDetails>

    // Fetch anime (using a genre filter, if applicable, for TMDB)
    @GET("discover/tv")
    fun getPopularAnime(@Query("api_key") apiKey: String, @Query("with_genres") genreId: Int, @Query("page") page: Int): Call<MovieResponse>
}
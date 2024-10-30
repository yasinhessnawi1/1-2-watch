package com.example.a1_2_watch.api

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

    // Fetch anime (using a genre filter, if applicable, for TMDB)
    @GET("discover/tv")
    fun getPopularAnime(@Query("api_key") apiKey: String, @Query("with_genres") genreId: Int, @Query("page") page: Int): Call<ShowResponse>

    // Fetch anime details from Kitsu
    @GET("anime/{anime_id}")
    fun getAnimeDetails(@Path("anime_id") animeId: String): Call<AnimeDetails>

    // Fetch popular anime from Kitsu
    @GET("anime")
    fun getPopularAnimeKitsu(@Query("page[limit]") limit: Int, @Query("page[offset]") offset: Int): Call<AnimeResponse>
}

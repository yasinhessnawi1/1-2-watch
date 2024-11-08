package com.example.a1_2_watch.api

import com.example.a1_2_watch.models.MovieDetails
import com.example.a1_2_watch.models.MovieResponse
import com.example.a1_2_watch.models.AnimeDetails
import com.example.a1_2_watch.models.AnimeResponse
import com.example.a1_2_watch.models.AnimeStreamingLinksResponse
import com.example.a1_2_watch.models.ShowDetails
import com.example.a1_2_watch.models.ShowResponse
import com.example.a1_2_watch.models.StreamerDetailsResponse
import com.example.a1_2_watch.models.WatchProvidersResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ApiService interface defining API endpoints for obtaining data from TMBD and Kitsu APIs.
 */
interface ApiService {

    /**
     * Fetches a list of popular movies form the TMDB API.
     *
     * @param apiKey The API key for authentication.
     * @param page The page number for pagination.
     * @return A [Call] object with the response containing the list of popular movies.
     */
    @GET("movie/popular")
    fun getPopularMovies(@Query("api_key") apiKey: String, @Query("page") page: Int): Call<MovieResponse>

    /**
     * Fetches detailed information about a specific movie by the given ID.
     *
     * @param movieId The ID of the movie.
     * @param apiKey The API key for authentication.
     * @return A [Call] object with the movie details response.
     */
    @GET("movie/{movie_id}")
    fun getMovieDetails(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<MovieDetails>

    /**
     * Fetches a list of popular TV shows form the TMDB API.
     *
     * @param apiKey The API key for authentication.
     * @param page The page number for pagination.
     * @return A [Call] object with the response containing the list of popular TV shows.
     */
    @GET("tv/popular")
    fun getPopularTVShows(@Query("api_key") apiKey: String, @Query("page") page: Int): Call<ShowResponse>

    /**
     * Fetches detailed information about a specific TV show by the given ID.
     *
     * @param tvId The ID of the TV show.
     * @param apiKey The API key for authentication.
     * @return A [Call] object with the TV show details response.
     */
    @GET("tv/{tv_id}")
    fun getTVShowDetails(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String): Call<ShowDetails>

    /**
     * Fetches a list of popular anime from the Kitsu API.
     *
     * @param limit The number of anime items to retrieve per page.
     * @param offset The offset for pagination.
     * @return A [Call] object with the response containing a list of popular anime.
     */
    @GET("anime/?sort=ratingRank")
    fun getPopularAnimeKitsu(@Query("page[limit]") limit: Int, @Query("page[offset]") offset: Int): Call<AnimeResponse>

    /**
     * Fetches detailed information about a specific anime by the given ID from the Kitsu API.
     *
     * @param animeId the ID of the anime.
     * @return A [Call] object with the anime details response.
     */
    @GET("anime/{anime_id}")
    fun getAnimeDetails(@Path("anime_id") animeId: String): Call<AnimeDetails>

    /**
     * Fetches available watch providers for a specific movie by the given ID.
     *
     * @param movieId The ID of the movie.
     * @param apiKey The API key for authentication.
     * @return A [Call] object with the response containing a list of movie watch providers.
     */
    @GET("movie/{movie_id}/watch/providers")
    fun getWatchProviders(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<WatchProvidersResponse>

    /**
     * Fetches available watch providers for a specific TV show by given ID.
     *
     * @param tvId The ID of the TV show.
     * @param apiKey The API key for authentication.
     * @return A [Call] object with the response containing a list of TV show watch providers.
     */
    @GET("tv/{tv_id}/watch/providers")
    fun getTVShowWatchProviders(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String): Call<WatchProvidersResponse>

    /**
     * Fetches streaming links for a specific anime by given ID.
     *
     * @param animeId The ID of the anime.
     * @return A [Call] object with the response containing the list of streaming links.
     */
    @GET("anime/{anime_id}/streaming-links")
    fun getAnimeStreamingLinks(@Path("anime_id") animeId: String): Call<AnimeStreamingLinksResponse>

    /**
     * Fetches details of a specific streamer by its streaming link ID.
     *
     * @param linkId The ID of the streaming link.
     * @return A [Call] object with the response containing streamer details.
     */
    @GET("streaming-links/{streaming_link_id}/streamer")
    fun getStreamerDetails(@Path("streaming_link_id") linkId: String): Call<StreamerDetailsResponse>

    /**
     * Searches for movies on TMDB by a query string.
     *
     * @param apiKey The API key for authentication.
     * @param query The search query string.
     * @return A [Call] object with the response containing search results for movies.
     */
    @GET("search/movie")
    fun searchMovies(@Query("api_key") apiKey: String, @Query("query") query: String): Call<MovieResponse>

    /**
     * Searches for TV shows on TMDB by a query string.
     *
     * @param apiKey The API key for authentication.
     * @param query The search query string.
     * @return A [Call] object with the response containing search results for TV shows.
     */
    @GET("search/tv")
    fun searchTVShows(@Query("api_key") apiKey: String, @Query("query") query: String): Call<ShowResponse>

    /**
     * Searches for anime on Kitsu by a query string.
     *
     * @param query The search query string.
     * @return A [Call] object with the response containing search results for anime.
     */
    @GET("anime")
    fun searchAnime(@Query("filter[text]") query: String): Call<AnimeResponse>


    @GET("movie/{movie_id}/similar")
    fun getRelatedMovies(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String): Call<MovieResponse>

    @GET("tv/{tv_id}/similar")
    fun getRelatedTVShows(@Path("tv_id") tvShowId: Int, @Query("api_key") apiKey: String): Call<ShowResponse>

    @GET("anime")
    fun searchAnimeByType(@Query("filter[subtype]") subtype: String): Call<AnimeResponse>

}

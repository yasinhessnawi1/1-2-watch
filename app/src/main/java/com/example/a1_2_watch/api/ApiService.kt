package com.example.a1_2_watch.api

import com.example.a1_2_watch.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ApiService interface defining API endpoints for obtaining data from TMDB and Kitsu APIs.
 */
interface ApiService {

    /**
     * Fetches a list of popular movies from the TMDB API.
     *
     * @param apiKey The API key for authentication.
     * @param page The page number for pagination.
     * @return A Response object with the response containing the list of popular movies.
     */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Response<MovieResponse>

    /**
     * Fetches detailed information about a specific movie by the given ID.
     *
     * @param movieId The ID of the movie.
     * @param apiKey The API key for authentication.
     * @return A Response object with the movie details response.
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieDetails>

    /**
     * Fetches a list of popular TV shows from the TMDB API.
     *
     * @param apiKey The API key for authentication.
     * @param page The page number for pagination.
     * @return A Response object with the response containing the list of popular TV shows.
     */
    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Response<ShowResponse>

    /**
     * Fetches detailed information about a specific TV show by the given ID.
     *
     * @param tvId The ID of the TV show.
     * @param apiKey The API key for authentication.
     * @return A Response object with the TV show details response.
     */
    @GET("tv/{tv_id}")
    suspend fun getTVShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Response<ShowDetails>

    /**
     * Fetches a list of popular anime from the Kitsu API.
     *
     * @param limit The number of anime items to retrieve per page.
     * @param offset The offset for pagination.
     * @return A Response object with the response containing a list of popular anime.
     */
    @GET("anime/?sort=ratingRank")
    suspend fun getPopularAnimeKitsu(
        @Query("page[limit]") limit: Int,
        @Query("page[offset]") offset: Int
    ): Response<AnimeResponse>

    /**
     * Fetches detailed information about a specific anime by the given ID from the Kitsu API.
     *
     * @param animeId the ID of the anime.
     * @return A Response object with the anime details response.
     */
    @GET("anime/{anime_id}")
    suspend fun getAnimeDetails(
        @Path("anime_id") animeId: String
    ): Response<AnimeDetails>




    /**
     * Fetches available watch providers for a specific movie by the given ID.
     *
     * @param movieId The ID of the movie.
     * @param apiKey The API key for authentication.
     * @return A Response object with the response containing a list of movie watch providers.
     */
    @GET("movie/{movie_id}/watch/providers")
    suspend fun getWatchProviders(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<WatchProvidersResponse>

    /**
     * Fetches available watch providers for a specific TV show by given ID.
     *
     * @param tvId The ID of the TV show.
     * @param apiKey The API key for authentication.
     * @return A Response object with the response containing a list of TV show watch providers.
     */
    @GET("tv/{tv_id}/watch/providers")
    suspend fun getTVShowWatchProviders(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Response<WatchProvidersResponse>

    /**
     * Fetches streaming links for a specific anime by given ID.
     *
     * @param animeId The ID of the anime.
     * @return A Response object with the response containing the list of streaming links.
     */
    @GET("anime/{anime_id}/streaming-links")
    suspend fun getAnimeStreamingLinks(
        @Path("anime_id") animeId: String
    ): Response<AnimeStreamingLinksResponse>

    /**
     * Fetches details of a specific streamer by its streaming link ID.
     *
     * @param linkId The ID of the streaming link.
     * @return A Response object with the response containing streamer details.
     */
    @GET("streaming-links/{streaming_link_id}/streamer")
    suspend fun getStreamerDetails(
        @Path("streaming_link_id") linkId: String
    ): Response<StreamerDetailsResponse>

    /**
     * Searches for movies on TMDB by a query string.
     *
     * @param apiKey The API key for authentication.
     * @param query The search query string.
     * @return A Response object with the response containing search results for movies.
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<MovieResponse>

    /**
     * Searches for TV shows on TMDB by a query string.
     *
     * @param apiKey The API key for authentication.
     * @param query The search query string.
     * @return A Response object with the response containing search results for TV shows.
     */
    @GET("search/tv")
    suspend fun searchTVShows(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<ShowResponse>

    /**
     * Searches for anime on Kitsu by a query string.
     *
     * @param query The search query string.
     * @return A Response object with the response containing search results for anime.
     */
    @GET("anime")
    suspend fun searchAnime(
        @Query("filter[text]") query: String
    ): Response<AnimeResponse>

    /**
     * Fetches a list of movies related to a specific movie by a given ID.
     *
     * @param movieId The unique ID of the movie.
     * @param apiKey The API key for authentication.
     * @return A Response object with the response containing a list of related movies.
     */
    @GET("movie/{movie_id}/similar")
    suspend fun getRelatedMovies(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>

    /**
     * Fetches a list of TV shows related to a specific TV show by a given ID.
     *
     * @param tvShowId The unique ID of the TV show.
     * @param apiKey The API key for authentication.
     * @return A Response object with the response containing a list of related TV shows.
     */
    @GET("tv/{tv_id}/similar")
    suspend fun getRelatedTVShows(
        @Path("tv_id") tvShowId: Int,
        @Query("api_key") apiKey: String
    ): Response<ShowResponse>

    /**
     * Searches for anime on Kitsu by a specific type.
     *
     * @param subtype The subtype of the anime to filter results by.
     * @return A Response object with the response containing a list of anime matching the subtype.
     */
    @GET("anime/{id}")
    suspend fun getAnime(
        @Path("id") id: String,
        @Query("include") include: String
    ): Response<AnimeResponseIncluded>

    @GET("anime")
    suspend fun getAnimeList(
        @Query("filter[id]") ids: String
    ): Response<AnimeResponse>
}
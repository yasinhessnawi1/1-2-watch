package com.example.a1_2_watch.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.utils.LikeButtonUtils
import com.example.a1_2_watch.workers.FetchRelatedItemsWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DiscoverViewModel is responsible for fetching, managing, and providing related movies, TV shows,
 * and anime for the Discover screen in the application.
 */
class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val _relatedMovies = MutableLiveData<List<Movie>>()
    val relatedMovies: LiveData<List<Movie>> get() = _relatedMovies

    private val _relatedShows = MutableLiveData<List<Show>>()
    val relatedShows: LiveData<List<Show>> get() = _relatedShows

    private val _relatedAnime = MutableLiveData<List<Anime>>()
    val relatedAnime: LiveData<List<Anime>> get() = _relatedAnime

    private val _isFetchingData = MutableLiveData<Boolean>()
    val isFetchingData: LiveData<Boolean> get() = _isFetchingData

    private val gson = Gson()
    private val relatedItemsPreferences = getApplication<Application>()
        .getSharedPreferences("related_items", Context.MODE_PRIVATE)

    private var fetchRelatedItemsWork: OneTimeWorkRequest? = null
    private var workInfoObserver: Observer<WorkInfo>? = null

    init {
        fetchRelatedItems()
    }

    /**
     * Fetches related items using WorkManager and observes its completion.
     */
    fun fetchRelatedItems() {
        _isFetchingData.postValue(true)
        removePreviousWorkObserver()

        fetchRelatedItemsWork = OneTimeWorkRequestBuilder<FetchRelatedItemsWorker>().build()
        WorkManager.getInstance(getApplication()).enqueue(fetchRelatedItemsWork!!)

        workInfoObserver = Observer { workInfo ->
            if (workInfo.id == fetchRelatedItemsWork?.id && workInfo.state.isFinished) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.d("DiscoverViewModel", "FetchRelatedItemsWorker succeeded.")
                    loadRelatedItemsFromPreferences()
                } else {
                    Log.e("DiscoverViewModel", "FetchRelatedItemsWorker failed.")
                    postEmptyLists()
                }
                _isFetchingData.postValue(false)
            }
        }

        WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(fetchRelatedItemsWork!!.id)
            .observeForever(workInfoObserver!!)
    }

    /**
     * Loads related items from SharedPreferences and updates LiveData objects.
     */
    private fun loadRelatedItemsFromPreferences() {
        try {
            Log.d("DiscoverViewModel", "Loading related items from preferences.")
            val relatedMoviesJson = relatedItemsPreferences.getString("related_movies", null)
            val relatedShowsJson = relatedItemsPreferences.getString("related_shows", null)
            val relatedAnimeJson = relatedItemsPreferences.getString("related_anime", null)

            val typeMovieList = object : TypeToken<List<Movie>>() {}.type
            val typeShowList = object : TypeToken<List<Show>>() {}.type
            val typeAnimeList = object : TypeToken<List<Anime>>() {}.type

            val relatedMovies = gson.fromJson<List<Movie>>(relatedMoviesJson ?: "[]", typeMovieList)
            val relatedShows = gson.fromJson<List<Show>>(relatedShowsJson ?: "[]", typeShowList)
            val relatedAnime = gson.fromJson<List<Anime>>(relatedAnimeJson ?: "[]", typeAnimeList)

            updateItemsWithLikedStatus(relatedMovies, relatedShows, relatedAnime)
        } catch (e: Exception) {
            Log.e("DiscoverViewModel", "Error loading related items: ${e.message}", e)
            postEmptyLists()
        }
    }

    /**
     * Updates the "isLiked" status of each item based on the user's liked items.
     */
    private fun updateItemsWithLikedStatus(
        relatedMovies: List<Movie>,
        relatedShows: List<Show>,
        relatedAnime: List<Anime>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val likeButtonUtils = LikeButtonUtils(getApplication())
                val likedMovies = likeButtonUtils.getLikedMovies()
                val likedShows = likeButtonUtils.getLikedShows()
                val likedAnime = likeButtonUtils.getLikedAnime()

                val updatedMovies = relatedMovies.map { movie ->
                    movie.isLiked = likedMovies.any { it.id == movie.id }
                    movie
                }
                val updatedShows = relatedShows.map { show ->
                    show.isLiked = likedShows.any { it.id == show.id }
                    show
                }
                val updatedAnime = relatedAnime.map { anime ->
                    anime.isLiked = likedAnime.any { it.id == anime.id }
                    anime
                }

                withContext(Dispatchers.Main) {
                    _relatedMovies.value = updatedMovies
                    _relatedShows.value = updatedShows
                    _relatedAnime.value = updatedAnime
                }
            } catch (e: Exception) {
                Log.e("DiscoverViewModel", "Error updating liked status: ${e.message}", e)
                postEmptyLists()
            }
        }
    }

    /**
     * Posts empty lists to the LiveData objects.
     */
    private fun postEmptyLists() {
        _relatedMovies.postValue(emptyList())
        _relatedShows.postValue(emptyList())
        _relatedAnime.postValue(emptyList())
    }

    /**
     * Removes the previously registered observer from WorkManager to prevent memory leaks.
     */
    private fun removePreviousWorkObserver() {
        fetchRelatedItemsWork?.id?.let { workId ->
            workInfoObserver?.let { observer ->
                WorkManager.getInstance(getApplication())
                    .getWorkInfoByIdLiveData(workId)
                    .removeObserver(observer)
            }
        }
    }

    /**
     * Cleans up resources when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        removePreviousWorkObserver()
    }
}

package com.example.a1_2_watch.ui

import android.app.Application
import android.content.Context
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
    private val relatedItemsPreferences = getApplication<Application>().getSharedPreferences("related_items", Context.MODE_PRIVATE)

    private var fetchRelatedItemsWork: OneTimeWorkRequest? = null
    private var workInfoObserver: Observer<WorkInfo>? = null

    init {
        fetchRelatedItems()
    }

    fun fetchRelatedItems() {
        _isFetchingData.postValue(true)

        // Remove previous observer if any
        fetchRelatedItemsWork?.id?.let { workId ->
            workInfoObserver?.let {
                WorkManager.getInstance(getApplication())
                    .getWorkInfoByIdLiveData(workId)
                    .removeObserver(it)
            }
        }

        // Enqueue new worker
        fetchRelatedItemsWork = OneTimeWorkRequestBuilder<FetchRelatedItemsWorker>().build()
        WorkManager.getInstance(getApplication()).enqueue(fetchRelatedItemsWork!!)

        // Observe the new worker's status
        workInfoObserver = Observer<WorkInfo> { workInfo ->
            if (workInfo != null && workInfo.id == fetchRelatedItemsWork?.id && workInfo.state.isFinished) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    loadRelatedItemsFromPreferences()
                } else {
                    _relatedMovies.postValue(emptyList())
                    _relatedShows.postValue(emptyList())
                    _relatedAnime.postValue(emptyList())
                }
                _isFetchingData.postValue(false)
            }
        }

        WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(fetchRelatedItemsWork!!.id)
            .observeForever(workInfoObserver!!)
    }

    private fun loadRelatedItemsFromPreferences() {
        val relatedMoviesJson = relatedItemsPreferences.getString("related_movies", null)
        val relatedShowsJson = relatedItemsPreferences.getString("related_shows", null)
        val relatedAnimeJson = relatedItemsPreferences.getString("related_anime", null)

        val typeMovieList = object : TypeToken<List<Movie>>() {}.type
        val typeShowList = object : TypeToken<List<Show>>() {}.type
        val typeAnimeList = object : TypeToken<List<Anime>>() {}.type

        val relatedMovies: List<Movie> = if (!relatedMoviesJson.isNullOrEmpty()) {
            gson.fromJson(relatedMoviesJson, typeMovieList)
        } else {
            emptyList()
        }

        val relatedShows: List<Show> = if (!relatedShowsJson.isNullOrEmpty()) {
            gson.fromJson(relatedShowsJson, typeShowList)
        } else {
            emptyList()
        }

        val relatedAnime: List<Anime> = if (!relatedAnimeJson.isNullOrEmpty()) {
            gson.fromJson(relatedAnimeJson, typeAnimeList)
        } else {
            emptyList()
        }

        // Update isLiked status based on liked items
        viewModelScope.launch(Dispatchers.IO) {
            val likeButtonUtils = LikeButtonUtils(getApplication())
            val likedMovies = likeButtonUtils.getLikedMovies()
            val likedShows = likeButtonUtils.getLikedShows()
            val likedAnime = likeButtonUtils.getLikedAnime()

            val updatedMovies = relatedMovies.map { movie ->
                movie.isLiked = likedMovies.any { likedMovie -> likedMovie.id == movie.id }
                movie
            }

            val updatedShows = relatedShows.map { show ->
                show.isLiked = likedShows.any { likedShow -> likedShow.id == show.id }
                show
            }

            val updatedAnime = relatedAnime.map { anime ->
                anime.isLiked = likedAnime.any { likedItem -> likedItem.id == anime.id }
                anime
            }

            withContext(Dispatchers.Main) {
                _relatedMovies.value = updatedMovies
                _relatedShows.value = updatedShows
                _relatedAnime.value = updatedAnime
            }
        }
    }

    fun refreshRelatedItems() {
        fetchRelatedItems()
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the observer to prevent memory leaks
        workInfoObserver?.let { observer ->
            fetchRelatedItemsWork?.id?.let { workId ->
                WorkManager.getInstance(getApplication())
                    .getWorkInfoByIdLiveData(workId)
                    .removeObserver(observer)
            }
        }
    }
}

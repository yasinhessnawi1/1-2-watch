package com.example.a1_2_watch.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.HomeLayoutBinding
import com.example.a1_2_watch.repository.MediaHandler

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeLayoutBinding
    private lateinit var moviesAdapter: MediaAdapter
    private lateinit var tvShowsAdapter: MediaAdapter
    private lateinit var animeAdapter: MediaAdapter
    private val mediaHandler = MediaHandler()
    private var currentPage = 1
    private var isLoading = false
    private val animeGenreId = 16 // Genre ID for anime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        fetchAllMedia() // Fetch data for all categories on activity start
        setupBottomNavigation()
    }

    private fun setupRecyclerViews() {

        moviesAdapter = MediaAdapter { movie ->
            // Handle movie item click
        }
        binding.moviesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.moviesRecyclerView.adapter = moviesAdapter

        tvShowsAdapter = MediaAdapter { movie ->
            // Handle movie item click
        }
        binding.tvShowsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tvShowsRecyclerView.adapter = tvShowsAdapter

        animeAdapter = MediaAdapter { movie ->
            // Handle movie item click
        }
        binding.animeRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.animeRecyclerView.adapter = animeAdapter
    }

    private fun fetchAllMedia() {
        isLoading = true
        // Fetch movies
        mediaHandler.fetchPopularMovies(currentPage, moviesAdapter, this) { onLoadingComplete() }
        // Fetch TV shows
        mediaHandler.fetchPopularTVShows(currentPage, tvShowsAdapter, this) { onLoadingComplete() }
        // Fetch anime
        mediaHandler.fetchPopularAnime(currentPage, animeGenreId, animeAdapter, this) { onLoadingComplete() }
    }

    private fun onLoadingComplete() {
        isLoading = false
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.home
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true
                R.id.user -> {
                    true
                }
                R.id.discover -> {
                    true
                }
                else -> false
            }
        }
    }
}
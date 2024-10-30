package com.example.a1_2_watch.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.MediaAdapter
import com.example.a1_2_watch.databinding.HomeLayoutBinding
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.repository.MediaHandler

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeLayoutBinding
    private lateinit var moviesAdapter: MediaAdapter<Movie>
    private lateinit var tvShowsAdapter: MediaAdapter<Show>
    private lateinit var animeAdapter: MediaAdapter<Anime>
    private val mediaHandler = MediaHandler()
    private var currentPage = 1
    private var isLoading = false
    private val animeLimit = 20 // Limit for anime items per page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        fetchAllMedia() // Fetch data for all categories on activity start
        setupBottomNavigation()
    }

    private fun setupRecyclerViews() {
        // Set up Movies RecyclerView
        moviesAdapter = MediaAdapter(
            onItemClick = { movie ->
                // Handle movie item click
            },
            onSaveClick = { movie ->
                Toast.makeText(this, "Saved ${movie.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.moviesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.moviesRecyclerView.adapter = moviesAdapter
        setupPaginationForRecyclerView(binding.moviesRecyclerView, MediaType.MOVIES)

        // Set up TV Shows RecyclerView
        tvShowsAdapter = MediaAdapter(
            onItemClick = { show ->
                // Handle show item click
            },
            onSaveClick = { show ->
                Toast.makeText(this, "Saved ${show.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.tvShowsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.tvShowsRecyclerView.adapter = tvShowsAdapter
        setupPaginationForRecyclerView(binding.tvShowsRecyclerView, MediaType.TV_SHOWS)

        // Set up Anime RecyclerView
        animeAdapter = MediaAdapter(
            onItemClick = { anime ->
                // Handle anime item click
            },
            onSaveClick = { anime ->
                Toast.makeText(this, "Saved ${anime.attributes.canonicalTitle}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.animeRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.animeRecyclerView.adapter = animeAdapter
        setupPaginationForRecyclerView(binding.animeRecyclerView, MediaType.ANIME)
    }

    private fun setupPaginationForRecyclerView(recyclerView: RecyclerView, mediaType: MediaType) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                    isLoading = true
                    currentPage++
                    when (mediaType) {
                        MediaType.MOVIES -> mediaHandler.fetchPopularMovies(currentPage, moviesAdapter, this@HomeActivity) { onLoadingComplete() }
                        MediaType.TV_SHOWS -> mediaHandler.fetchPopularTVShows(currentPage, tvShowsAdapter, this@HomeActivity) { onLoadingComplete() }
                        MediaType.ANIME -> mediaHandler.fetchPopularAnime(currentPage, animeLimit, animeAdapter, this@HomeActivity) { onLoadingComplete() }
                    }
                }
            }
        })
    }

    private fun fetchAllMedia() {
        isLoading = true
        // Fetch movies
        mediaHandler.fetchPopularMovies(currentPage, moviesAdapter, this) { onLoadingComplete() }
        // Fetch TV shows
        mediaHandler.fetchPopularTVShows(currentPage, tvShowsAdapter, this) { onLoadingComplete() }
        // Fetch anime
        mediaHandler.fetchPopularAnime(currentPage, animeLimit, animeAdapter, this) { onLoadingComplete() }
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



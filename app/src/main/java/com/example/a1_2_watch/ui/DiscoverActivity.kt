package com.example.a1_2_watch.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.DiscoverAdapter
import com.example.a1_2_watch.databinding.DiscoverLayoutBinding
import com.example.a1_2_watch.models.Anime
import com.example.a1_2_watch.models.Movie
import com.example.a1_2_watch.models.Show
import com.example.a1_2_watch.repository.DiscoverHandler
import com.example.a1_2_watch.utils.Constants
import com.example.a1_2_watch.utils.NavigationUtils

class DiscoverActivity : AppCompatActivity() {
    private lateinit var binding: DiscoverLayoutBinding
    private val searchRepository = DiscoverHandler()
    private val discoverAdapter = DiscoverAdapter(onItemClick = { item ->
        handleItemClick(item) // Handle item click based on type
    })

    // Flag to track if the query is empty
    private var isQueryEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DiscoverLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goBackButton.setOnClickListener {
            finish()
        }


        setupRecyclerView()
        setupSearchView()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiscoverActivity)
            adapter = discoverAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val trimmedQuery = newText?.trim() ?: ""

                // If the query is empty, clear the search results
                if (trimmedQuery.isEmpty()) {
                    isQueryEmpty = true // Set flag to true
                    discoverAdapter.updateItems(emptyList())
                    return true
                }

                isQueryEmpty = false // Set flag to false for non-empty query
                performSearch(trimmedQuery)
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        val trimmedQuery = query.trim()

        // If the query is empty, clear the search results and set the flag
        if (trimmedQuery.isEmpty() || trimmedQuery.isBlank()) {
            isQueryEmpty = true
            discoverAdapter.updateItems(emptyList())
            return
        }

        isQueryEmpty = false // Reset flag when performing a search
        val searchResults = mutableListOf<Any>()

        searchRepository.searchMovies(Constants.API_KEY, trimmedQuery) { movieResponse ->
            if (!isQueryEmpty) { // Only update if query is not empty
                movieResponse?.results?.let { movies ->
                    searchResults.addAll(movies)
                    discoverAdapter.updateItems(searchResults)
                }
            }
        }

        searchRepository.searchTVShows(Constants.API_KEY, trimmedQuery) { showResponse ->
            if (!isQueryEmpty) { // Only update if query is not empty
                showResponse?.results?.let { shows ->
                    searchResults.addAll(shows)
                    discoverAdapter.updateItems(searchResults)
                }
            }
        }

        searchRepository.searchAnime(trimmedQuery) { animeResponse ->
            if (!isQueryEmpty) { // Only update if query is not empty
                animeResponse?.data?.let { animes ->
                    searchResults.addAll(animes)
                    discoverAdapter.updateItems(searchResults)
                }
            }
        }
    }

    private fun handleItemClick(item: Any) {
        when (item) {
            is Movie -> NavigationUtils.navigateToDetails(this, item.id, "MOVIES")
            is Show -> NavigationUtils.navigateToDetails(this, item.id, "TV_SHOWS")
            is Anime -> NavigationUtils.navigateToDetails(this, item.id, "ANIME")
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.discover
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    NavigationUtils.navigateToHome(this)
                    true
                }
                R.id.user -> {
                    NavigationUtils.navigateToUser(this)
                    true
                }
                R.id.discover -> true
                else -> false
            }
        }
    }
}


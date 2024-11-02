package com.example.a1_2_watch.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a1_2_watch.R
import com.example.a1_2_watch.adapters.ProvidersAdapter
import com.example.a1_2_watch.databinding.DetailsLayoutBinding
import com.example.a1_2_watch.models.MediaType
import com.example.a1_2_watch.repository.DetailsHandler
import com.example.a1_2_watch.utils.NavigationUtils

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: DetailsLayoutBinding
    private lateinit var providersAdapter: ProvidersAdapter
    private val detailsRepository = DetailsHandler()
    private var countryCode = "US"
    private var mediaId: Int = -1
    private lateinit var mediaType: MediaType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        providersAdapter = ProvidersAdapter(emptyList())

        setupBottomNavigation()
        extractMediaDataFromIntent()

        if (mediaId != -1) {
            // Fetch the details based on mediaType and mediaId
            detailsRepository.fetchDetails(mediaId, mediaType, this, binding)
        }

        setupRegionSpinner()
        setupProvidersRecyclerView()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.menu.setGroupCheckable(0, false, true) // Deselect all items by default
        bottomNavigationView.setOnItemSelectedListener { item ->
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

    private fun extractMediaDataFromIntent() {
        mediaId = intent.getIntExtra("MEDIA_ID", -1)
        val mediaTypeString = intent.getStringExtra("MEDIA_TYPE")
        mediaType = MediaType.valueOf(mediaTypeString ?: "MOVIES")
    }

    private fun setupRegionSpinner() {
        val regions = listOf("US", "NO", "GB", "FR", "DE", "IN", "CA", "AU", "ES", "IT", "JP", "KR", "BR", "NL", "RU", "MX", "SE", "TR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, regions)
        binding.regionSpinner.adapter = adapter

        binding.regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                countryCode = regions[position]
                fetchWatchProviders(mediaId, mediaType) // Ensure mediaType is passed here
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupProvidersRecyclerView() {
        binding.providersRecyclerView.layoutManager = LinearLayoutManager(this)
        providersAdapter = ProvidersAdapter(emptyList())
        binding.providersRecyclerView.adapter = providersAdapter
    }

    private fun fetchWatchProviders(mediaId: Int, mediaType: MediaType) {
        detailsRepository.fetchWatchProviders(mediaId, mediaType, countryCode, providersAdapter, binding)
    }
}
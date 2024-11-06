package com.example.a1_2_watch.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.databinding.ProviderLayoutBinding
import com.example.a1_2_watch.models.Provider

/**
 * This adapter for displaying a list of streaming providers.
 *
 * @property providers List of providers to be displayed in the RecyclerView adapter.
 */
class ProvidersAdapter(private var providers: List<Provider>) :
    RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder>() {

    /**
     * This function is called when the RecyclerView needs a new ViewHolder for displaying a provider item.
     *
     * @param parent The parent ViewGroup that holds the view.
     * @param viewType The type of the view
     * @return ProviderViewHolder A new instance of ProviderViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        // Inflate the provider layout for each provider item by using ProviderLayoutBinding.
        val binding =
            ProviderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProviderViewHolder(binding)
    }

    /**
     * This Function to display the data at the specified position in the RecycleView.
     *
     * @param holder ProviderViewHolder that holds the UI elements for each provider.
     * @param position Position of the item to be displayed in the RecycleView.
     */
    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        // Gets the provider item at the specified position.
        val provider = providers[position]
        // Binds the provider data to the view holder.
        holder.bind(provider)
    }

    /**
     * Returns the total number of providers in the list.
     *
     * @return Int the size of the providers list.
     */
    override fun getItemCount(): Int = providers.size

    /**
     * This function to update the list of providers in the adapter and notify the RecyclerView that the
     * list has changed.
     *
     * @param newProviders The new list of providers to display.
     */
    fun updateProviders(newProviders: List<Provider>) {
        // Updates the list of providers with the new data.
        providers = newProviders
        // Notify the adapter that the list has changed
        notifyDataSetChanged()
    }

    /**
     * ProviderViewHolder class represents individual provider items in the RecyclerView adapter.
     *
     * @param binding The binding object for ProviderLayout, which holds references to UI elements.
     */
    class ProviderViewHolder(private val binding: ProviderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds data to the UI elements in the item view based on the provider object.
         *
         * @param provider The provider item to bind data for.
         */
        fun bind(provider: Provider) {
            // Sets the provider name.
            binding.providerNameTextView.text = provider.provider_name
            // Load provider logo using Glide library.
            Glide.with(binding.providerLogoImageView.context)
                .load("https://image.tmdb.org/t/p/original/${provider.logo_path}")
                .into(binding.providerLogoImageView)
        }
    }
}
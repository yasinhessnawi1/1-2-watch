package com.example.a1_2_watch.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a1_2_watch.databinding.ProviderLayoutBinding
import com.example.a1_2_watch.models.Provider

class ProvidersAdapter(private var providers: List<Provider>) : RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding = ProviderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProviderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.bind(provider)
    }

    override fun getItemCount(): Int = providers.size

    fun updateProviders(newProviders: List<Provider>) {
        providers = newProviders
        notifyDataSetChanged()
    }

    class ProviderViewHolder(private val binding: ProviderLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(provider: Provider) {
            binding.providerNameTextView.text = provider.provider_name
            // Load provider logo
            Glide.with(binding.providerLogoImageView.context)
                .load("https://image.tmdb.org/t/p/original/${provider.logo_path}")
                .into(binding.providerLogoImageView)
        }
    }
}
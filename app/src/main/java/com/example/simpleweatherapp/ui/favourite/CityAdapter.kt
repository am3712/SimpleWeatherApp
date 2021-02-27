package com.example.simpleweatherapp.ui.favourite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweatherapp.data.local.City
import com.example.simpleweatherapp.data.repository.TemperatureUnit
import com.example.simpleweatherapp.databinding.FavouriteListItemBinding

class CityAdapter(private val clickListener: FavouriteCityListener) : ListAdapter<City,
        CityAdapter.ViewHolder>(WeatherDayDiffCallback()) {

    var temperatureUnit: TemperatureUnit? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(clickListener, temperatureUnit, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: FavouriteListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: FavouriteCityListener,
            temperatureUnit: TemperatureUnit?,
            item: City
        ) {
            binding.city = item
            binding.tempUnit = temperatureUnit
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FavouriteListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

interface FavouriteCityListener {
    fun onClick(city: City)
    fun onRemove(city: City)
}

class WeatherDayDiffCallback : DiffUtil.ItemCallback<City>() {
    override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.lat == newItem.lat
                && oldItem.lng == newItem.lng
    }

    override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem == newItem
    }
}

package com.myapps.simpleweatherapp.ui.weather

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapps.simpleweatherapp.data.local.Hour
import com.myapps.simpleweatherapp.data.repository.TemperatureUnit
import com.myapps.simpleweatherapp.databinding.HourListItemBinding

class HourAdapter : ListAdapter<Hour,
        HourAdapter.ViewHolder>(WeatherHourDiffCallback()) {

    var temperatureUnit: TemperatureUnit? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, temperatureUnit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: HourListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Hour, temperatureUnit: TemperatureUnit?) {
            binding.hour = item
            binding.tempUnit = temperatureUnit
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HourListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class WeatherHourDiffCallback : DiffUtil.ItemCallback<Hour>() {
    override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean {
        return oldItem.index == newItem.index
    }

    override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean {
        return oldItem == newItem
    }
}

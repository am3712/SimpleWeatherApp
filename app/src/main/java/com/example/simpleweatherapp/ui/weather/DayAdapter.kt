package com.example.simpleweatherapp.ui.weather

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweatherapp.data.local.Day
import com.example.simpleweatherapp.data.repository.TemperatureUnit
import com.example.simpleweatherapp.databinding.DayListItemBinding

class DayAdapter : ListAdapter<Day,
        DayAdapter.ViewHolder>(WeatherDayDiffCallback()) {

    var temperatureUnit: TemperatureUnit? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, temperatureUnit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: DayListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Day, temperatureUnit: TemperatureUnit?) {
            binding.day = item
            binding.tempUnit = temperatureUnit
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DayListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class WeatherDayDiffCallback : DiffUtil.ItemCallback<Day>() {
    override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
        return oldItem.index == newItem.index
    }

    override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
        return oldItem == newItem
    }
}

package com.example.simpleweatherapp.ui.alarms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweatherapp.data.local.Alarm
import com.example.simpleweatherapp.databinding.AlarmListItemBinding


class AlertAdapter(private val clickListener: AlarmListener) : ListAdapter<Alarm,
        AlertAdapter.ViewHolder>(AlertDiffCallback()) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: AlarmListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Alarm, clickListener: AlarmListener) {
            binding.alarm = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AlarmListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

interface AlarmListener {
    fun onDelete(alarm: Alarm)
    fun startStop(alarm: Alarm)
}

class AlertDiffCallback : DiffUtil.ItemCallback<Alarm>() {
    override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem.alarmId == newItem.alarmId
    }

    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem == newItem
    }
}

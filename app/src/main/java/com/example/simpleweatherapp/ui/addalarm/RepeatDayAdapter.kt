package com.example.simpleweatherapp.ui.addalarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweatherapp.databinding.RepeatListItemBinding

class RepeatDayAdapter : RecyclerView.Adapter<RepeatDayAdapter.ViewHolder>() {

    var data = listOf<RepeatDay>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class ViewHolder private constructor(val binding: RepeatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RepeatDay) {
            binding.repeatDay = item
            binding.checkBox.setOnCheckedChangeListener { _, isChecked -> item.checked = isChecked }
            binding.root.setOnClickListener {
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RepeatListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

data class RepeatDay(val dayIndex: Int, var checked: Boolean)
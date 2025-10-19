package com.PRO.propdf.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.databinding.ItemSettingBinding

class SettingsAdapter(
    private val onSettingClick: (SettingItem) -> Unit
) : ListAdapter<SettingItem, SettingsAdapter.SettingViewHolder>(SettingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val setting = getItem(position)
        holder.bind(setting)
    }

    inner class SettingViewHolder(
        private val binding: ItemSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(setting: SettingItem) {
            binding.apply {
                textSettingTitle.text = setting.title
                textSettingDescription.text = setting.description
                imageSettingIcon.setImageResource(setting.iconRes)

                // Show/hide arrow based on type
                imageArrow.visibility = if (setting.type == SettingType.NORMAL) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Handle switch visibility
                switchSetting.visibility = if (setting.type == SettingType.SWITCH) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Set click listener
                root.setOnClickListener {
                    onSettingClick(setting)
                }
            }
        }
    }
}

class SettingDiffCallback : DiffUtil.ItemCallback<SettingItem>() {
    override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem == newItem
    }
}
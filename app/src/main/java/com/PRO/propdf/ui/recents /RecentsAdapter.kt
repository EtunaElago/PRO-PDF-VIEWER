package com.PRO.propdf.ui.recents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.data.model.RecentModel
import com.PRO.propdf.databinding.ItemRecentBinding

class RecentsAdapter(
    private val listener: RecentItemListener
) : ListAdapter<RecentModel, RecentsAdapter.RecentViewHolder>(RecentDiffCallback()) {

    private val selectedItems = mutableSetOf<RecentModel>()

    interface RecentItemListener {
        fun onRecentClicked(recent: RecentModel)
        fun onRecentLongClicked(recent: RecentModel): Boolean
        fun onRecentSelected(recent: RecentModel, isSelected: Boolean)
        fun onRemoveRecent(recent: RecentModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = ItemRecentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val recent = getItem(position)
        val isSelected = selectedItems.contains(recent)
        holder.bind(recent, isSelected)
    }

    fun setItemSelected(recent: RecentModel, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(recent)
        } else {
            selectedItems.remove(recent)
        }
        notifyItemChanged(currentList.indexOf(recent))
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<RecentModel> {
        return selectedItems.toList()
    }

    inner class RecentViewHolder(
        private val binding: ItemRecentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recent: RecentModel, isSelected: Boolean) {
            binding.apply {
                textPdfName.text = recent.pdfName
                textLastOpened.text = recent.lastOpenedFormatted
                textPageInfo.text = recent.pageProgress
                textFolderName.text = recent.folderName ?: "Unknown Folder"

                // Show warning if PDF doesn't exist
                if (!recent.isPdfExists) {
                    textPdfName.alpha = 0.5f
                    imageWarning.visibility = android.view.View.VISIBLE
                    textFileMissing.visibility = android.view.View.VISIBLE
                } else {
                    textPdfName.alpha = 1.0f
                    imageWarning.visibility = android.view.View.GONE
                    textFileMissing.visibility = android.view.View.GONE
                }

                // Handle selection state
                handleSelectionState(isSelected)

                // Set click listeners
                root.setOnClickListener {
                    listener.onRecentClicked(recent)
                }

                root.setOnLongClickListener {
                    listener.onRecentLongClicked(recent)
                }

                // Remove button
                buttonRemove.setOnClickListener {
                    listener.onRemoveRecent(recent)
                }

                // Selection checkbox
                checkboxSelection.setOnCheckedChangeListener(null) // Prevent recursive calls
                checkboxSelection.isChecked = isSelected
                checkboxSelection.setOnCheckedChangeListener { _, isChecked ->
                    toggleSelection(recent, isChecked)
                }
            }
        }

        private fun handleSelectionState(isSelected: Boolean) {
            binding.apply {
                if (selectedItems.isNotEmpty()) {
                    // Selection mode is active
                    checkboxSelection.visibility = android.view.View.VISIBLE
                    selectionOverlay.visibility = android.view.View.VISIBLE
                    buttonRemove.visibility = android.view.View.GONE
                    root.alpha = if (isSelected) 0.7f else 1.0f
                } else {
                    // Normal mode
                    checkboxSelection.visibility = android.view.View.GONE
                    selectionOverlay.visibility = android.view.View.GONE
                    buttonRemove.visibility = android.view.View.VISIBLE
                    root.alpha = 1.0f
                }

                // Update background based on selection
                root.setBackgroundResource(
                    if (isSelected) R.drawable.bg_item_selected
                    else R.drawable.bg_rounded_corner
                )
            }
        }

        private fun toggleSelection(recent: RecentModel, isSelected: Boolean) {
            binding.checkboxSelection.isChecked = isSelected
            handleSelectionState(isSelected)
            listener.onRecentSelected(recent, isSelected)
        }
    }
}

class RecentDiffCallback : DiffUtil.ItemCallback<RecentModel>() {
    override fun areItemsTheSame(oldItem: RecentModel, newItem: RecentModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RecentModel, newItem: RecentModel): Boolean {
        return oldItem == newItem
    }
}
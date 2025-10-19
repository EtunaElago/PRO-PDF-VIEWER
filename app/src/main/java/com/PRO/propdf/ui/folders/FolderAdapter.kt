package com.PRO.propdf.ui.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.databinding.ItemFolderBinding

class FolderAdapter(
    private val listener: FolderItemListener
) : ListAdapter<FolderModel, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Long>()

    interface FolderItemListener {
        fun onFolderClicked(folder: FolderModel)
        fun onFolderLongClicked(folder: FolderModel): Boolean
        fun onFolderSelected(folder: FolderModel, isSelected: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(position)
        val isSelected = selectedItems.contains(folder.id)
        holder.bind(folder, isSelected, isSelectionMode)
    }

    fun setSelectionMode(selectionMode: Boolean) {
        isSelectionMode = selectionMode
        if (!selectionMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun setItemSelected(folderId: Long, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(folderId)
        } else {
            selectedItems.remove(folderId)
        }
        notifyItemChanged(currentList.indexOfFirst { it.id == folderId })
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedFolders(): List<FolderModel> {
        return currentList.filter { selectedItems.contains(it.id) }
    }

    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList.map { it.id })
        notifyDataSetChanged()
    }

    fun isSelectionMode(): Boolean = isSelectionMode

    fun getSelectedCount(): Int = selectedItems.size

    inner class FolderViewHolder(
        private val binding: ItemFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: FolderModel, isSelected: Boolean, isSelectionMode: Boolean) {
            binding.apply {
                textFolderName.text = folder.name
                textItemCount.text = folder.displayItemCount

                // Set folder icon and color
                setFolderAppearance(folder)

                // Handle selection state
                handleSelectionState(isSelected, isSelectionMode)

                // Set click listeners
                root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(folder, !isSelected)
                    } else {
                        listener.onFolderClicked(folder)
                    }
                }

                root.setOnLongClickListener {
                    if (!isSelectionMode) {
                        listener.onFolderLongClicked(folder)
                    } else {
                        toggleSelection(folder, !isSelected)
                    }
                    true
                }

                // Selection checkbox
                checkboxSelection.setOnCheckedChangeListener(null) // Prevent recursive calls
                checkboxSelection.isChecked = isSelected
                checkboxSelection.setOnCheckedChangeListener { _, isChecked ->
                    toggleSelection(folder, isChecked)
                }
            }
        }

        private fun setFolderAppearance(folder: FolderModel) {
            // This would set folder icon color based on folder color
            // Implementation depends on your icon setup
            val context = binding.root.context
            val folderDrawable = binding.imageFolder.drawable
            folder.color?.let { color ->
                folderDrawable?.setTint(color)
            }
        }

        private fun handleSelectionState(isSelected: Boolean, isSelectionMode: Boolean) {
            binding.apply {
                if (isSelectionMode) {
                    checkboxSelection.visibility = android.view.View.VISIBLE
                    selectionOverlay.visibility = android.view.View.VISIBLE
                    root.alpha = if (isSelected) 0.7f else 1.0f
                } else {
                    checkboxSelection.visibility = android.view.View.GONE
                    selectionOverlay.visibility = android.view.View.GONE
                    root.alpha = 1.0f
                }

                // Update background based on selection
                root.setBackgroundResource(
                    if (isSelected) R.drawable.bg_item_selected
                    else R.drawable.bg_rounded_corner
                )
            }
        }

        private fun toggleSelection(folder: FolderModel, isSelected: Boolean) {
            binding.checkboxSelection.isChecked = isSelected
            handleSelectionState(isSelected, true)
            listener.onFolderSelected(folder, isSelected)
        }
    }
}

class FolderDiffCallback : DiffUtil.ItemCallback<FolderModel>() {
    override fun areItemsTheSame(oldItem: FolderModel, newItem: FolderModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FolderModel, newItem: FolderModel): Boolean {
        return oldItem == newItem
    }
}
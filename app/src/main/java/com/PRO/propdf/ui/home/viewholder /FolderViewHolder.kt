package com.PRO.propdf.ui.home.viewholder

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.R
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.databinding.ItemFolderBinding
import com.PRO.propdf.utils.ThemeUtils

class FolderViewHolder(
    private val binding: ItemFolderBinding,
    private val listener: FolderItemListener
) : RecyclerView.ViewHolder(binding.root) {

    interface FolderItemListener {
        fun onFolderClicked(folder: FolderModel)
        fun onFolderLongClicked(folder: FolderModel): Boolean
        fun onFolderSelected(folder: FolderModel, isSelected: Boolean)
    }

    fun bind(folder: FolderModel, isSelected: Boolean, isSelectionMode: Boolean = false) {
        binding.apply {
            textFolderName.text = folder.name
            textItemCount.text = folder.displayItemCount

            // Set folder icon with color
            setFolderIcon(folder)

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
                listener.onFolderLongClicked(folder)
            }
        }
    }

    private fun setFolderIcon(folder: FolderModel) {
        val context = binding.root.context
        val folderColor = folder.color ?: ThemeUtils.getAttributeColor(
            context, 
            R.attr.colorPrimary
        )

        // Create a colored folder icon
        val folderDrawable = ContextCompat.getDrawable(context, R.drawable.ic_folder)
        folderDrawable?.setTint(folderColor)
        binding.imageFolder.setImageDrawable(folderDrawable)

        // Set background with color
        val background = binding.folderIconBackground.background as? GradientDrawable
        background?.setColor(ThemeUtils.getColorWithAlpha(folderColor, 0.2f))
    }

    private fun handleSelectionState(isSelected: Boolean, isSelectionMode: Boolean) {
        binding.apply {
            if (isSelectionMode) {
                // Show selection UI
                selectionOverlay.visibility = View.VISIBLE
                checkboxSelection.visibility = View.VISIBLE
                checkboxSelection.isChecked = isSelected
                
                // Dim the item when selected
                root.alpha = if (isSelected) 0.7f else 1.0f
            } else {
                // Hide selection UI
                selectionOverlay.visibility = View.GONE
                checkboxSelection.visibility = View.GONE
                root.alpha = 1.0f
            }

            // Add ripple effect or border based on selection
            if (isSelected) {
                root.setBackgroundResource(R.drawable.bg_item_selected)
            } else {
                root.setBackgroundResource(R.drawable.bg_rounded_corner)
            }
        }
    }

    private fun toggleSelection(folder: FolderModel, isSelected: Boolean) {
        binding.checkboxSelection.isChecked = isSelected
        handleSelectionState(isSelected, true)
        listener.onFolderSelected(folder, isSelected)
    }

    fun setSelectionMode(isSelectionMode: Boolean) {
        binding.checkboxSelection.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        binding.selectionOverlay.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
    }

    companion object {
        const val VIEW_TYPE = 0
    }
}
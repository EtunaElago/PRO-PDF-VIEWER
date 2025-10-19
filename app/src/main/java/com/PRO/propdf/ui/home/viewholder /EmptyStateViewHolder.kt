package com.PRO.propdf.ui.home.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.databinding.LayoutEmptyStateBinding

class EmptyStateViewHolder(
    private val binding: LayoutEmptyStateBinding,
    private val listener: EmptyStateListener
) : RecyclerView.ViewHolder(binding.root) {

    interface EmptyStateListener {
        fun onCreateFolderClicked()
        fun onAddPdfClicked()
    }

    fun bind(emptyStateType: EmptyStateType) {
        binding.apply {
            when (emptyStateType) {
                EmptyStateType.HOME -> {
                    imageEmptyState.setImageResource(R.drawable.ic_empty_home)
                    textEmptyTitle.text = "No PDFs or Folders"
                    textEmptyDescription.text = "Start by adding your first PDF or creating a folder"
                    buttonAction1.text = "Create Folder"
                    buttonAction2.text = "Add PDF"
                    
                    buttonAction1.setOnClickListener { listener.onCreateFolderClicked() }
                    buttonAction2.setOnClickListener { listener.onAddPdfClicked() }
                }
                EmptyStateType.FOLDER -> {
                    imageEmptyState.setImageResource(R.drawable.ic_empty_folder)
                    textEmptyTitle.text = "Empty Folder"
                    textEmptyDescription.text = "This folder is empty. Add PDFs or create subfolders"
                    buttonAction1.text = "Add PDF"
                    buttonAction2.text = "Create Subfolder"
                    
                    buttonAction1.setOnClickListener { listener.onAddPdfClicked() }
                    buttonAction2.setOnClickListener { listener.onCreateFolderClicked() }
                }
                EmptyStateType.SEARCH -> {
                    imageEmptyState.setImageResource(R.drawable.ic_empty_search)
                    textEmptyTitle.text = "No Results Found"
                    textEmptyDescription.text = "Try different search terms or browse all files"
                    buttonAction1.text = "Clear Search"
                    buttonAction2.visibility = android.view.View.GONE
                    
                    buttonAction1.setOnClickListener { /* Handle clear search */ }
                }
            }
        }
    }

    enum class EmptyStateType {
        HOME, FOLDER, SEARCH
    }
}
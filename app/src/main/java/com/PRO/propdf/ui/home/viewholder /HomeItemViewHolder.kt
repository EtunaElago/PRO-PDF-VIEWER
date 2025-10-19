package com.PRO.propdf.ui.home.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.ItemFolderBinding
import com.PRO.propdf.databinding.ItemPdfBinding
import com.PRO.propdf.ui.home.HomeAdapter

class HomeItemViewHolderAdapter(
    private val folderListener: FolderViewHolder.FolderItemListener,
    private val pdfListener: PdfViewHolder.PdfItemListener
) : ListAdapter<Any, RecyclerView.ViewHolder>(HomeItemDiffCallback()) {

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Any>()

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FolderModel -> FolderViewHolder.VIEW_TYPE
            is PdfModel -> PdfViewHolder.VIEW_TYPE
            else -> throw IllegalArgumentException("Unknown view type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FolderViewHolder.VIEW_TYPE -> {
                val binding = ItemFolderBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FolderViewHolder(binding, folderListener)
            }
            PdfViewHolder.VIEW_TYPE -> {
                val binding = ItemPdfBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PdfViewHolder(binding, pdfListener)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = selectedItems.contains(item)

        when (holder) {
            is FolderViewHolder -> holder.bind(item as FolderModel, isSelected, isSelectionMode)
            is PdfViewHolder -> holder.bind(item as PdfModel, isSelected, isSelectionMode)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PdfViewHolder -> holder.recycle()
        }
        super.onViewRecycled(holder)
    }

    fun setSelectionMode(selectionMode: Boolean) {
        isSelectionMode = selectionMode
        notifyDataSetChanged()
    }

    fun setItemSelected(item: Any, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
        notifyItemChanged(currentList.indexOf(item))
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Set<Any> = selectedItems.toSet()

    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
    }

    fun getSelectedFolders(): List<FolderModel> {
        return selectedItems.filterIsInstance<FolderModel>()
    }

    fun getSelectedPdfs(): List<PdfModel> {
        return selectedItems.filterIsInstance<PdfModel>()
    }
}

class HomeItemDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is FolderModel && newItem is FolderModel -> oldItem.id == newItem.id
            oldItem is PdfModel && newItem is PdfModel -> oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is FolderModel && newItem is FolderModel -> 
                oldItem.name == newItem.name && 
                oldItem.totalItems == newItem.totalItems
            oldItem is PdfModel && newItem is PdfModel -> 
                oldItem.name == newItem.name && 
                oldItem.size == newItem.size && 
                oldItem.pageCount == newItem.pageCount
            else -> false
        }
    }
}
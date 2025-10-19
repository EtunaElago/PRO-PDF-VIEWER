package com.PRO.propdf.ui.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.ui.home.viewholder.*

class HomeAdapter(
    private val folderListener: FolderViewHolder.FolderItemListener,
    private val pdfListener: PdfViewHolder.PdfItemListener,
    private val emptyStateListener: EmptyStateViewHolder.EmptyStateListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = emptyList()
    private var showEmptyState = false
    private var showLoading = false
    private var emptyStateType = EmptyStateViewHolder.EmptyStateType.HOME

    fun submitList(newItems: List<Any>) {
        items = newItems
        showEmptyState = newItems.isEmpty()
        showLoading = false
        notifyDataSetChanged()
    }

    fun showLoading() {
        showLoading = true
        showEmptyState = false
        notifyDataSetChanged()
    }

    fun showEmptyState(type: EmptyStateViewHolder.EmptyStateType) {
        showEmptyState = true
        showLoading = false
        emptyStateType = type
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            showLoading -> VIEW_TYPE_LOADING
            showEmptyState -> VIEW_TYPE_EMPTY_STATE
            else -> when (items[position]) {
                is FolderModel -> VIEW_TYPE_FOLDER
                is PdfModel -> VIEW_TYPE_PDF
                else -> throw IllegalArgumentException("Unknown view type")
            }
        }
    }

    override fun getItemCount(): Int {
        return when {
            showLoading -> 1
            showEmptyState -> 1
            else -> items.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FOLDER -> {
                val binding = ItemFolderBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FolderViewHolder(binding, folderListener)
            }
            VIEW_TYPE_PDF -> {
                val binding = ItemPdfBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PdfViewHolder(binding, pdfListener)
            }
            VIEW_TYPE_EMPTY_STATE -> {
                val binding = LayoutEmptyStateBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EmptyStateViewHolder(binding, emptyStateListener)
            }
            VIEW_TYPE_LOADING -> {
                val binding = LayoutLoadingBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderViewHolder -> holder.bind(items[position] as FolderModel, false, false)
            is PdfViewHolder -> holder.bind(items[position] as PdfModel, false, false)
            is EmptyStateViewHolder -> holder.bind(emptyStateType)
            is LoadingViewHolder -> holder.bind()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PdfViewHolder -> holder.recycle()
        }
        super.onViewRecycled(holder)
    }

    companion object {
        private const val VIEW_TYPE_FOLDER = 0
        private const val VIEW_TYPE_PDF = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
        private const val VIEW_TYPE_LOADING = 3
    }
}
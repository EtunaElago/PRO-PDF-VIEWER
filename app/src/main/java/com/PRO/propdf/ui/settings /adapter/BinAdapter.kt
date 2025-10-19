package com.PRO.propdf.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.ItemBinBinding

class BinAdapter(
    private val listener: BinItemListener
) : ListAdapter<PdfModel, BinAdapter.BinViewHolder>(BinDiffCallback()) {

    private val selectedItems = mutableSetOf<PdfModel>()

    interface BinItemListener {
        fun onPdfClicked(pdf: PdfModel)
        fun onPdfLongClicked(pdf: PdfModel): Boolean
        fun onPdfSelected(pdf: PdfModel, isSelected: Boolean)
        fun onRestorePdf(pdf: PdfModel)
        fun onDeletePdf(pdf: PdfModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinViewHolder {
        val binding = ItemBinBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BinViewHolder, position: Int) {
        val pdf = getItem(position)
        val isSelected = selectedItems.contains(pdf)
        holder.bind(pdf, isSelected)
    }

    fun setItemSelected(pdf: PdfModel, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(pdf)
        } else {
            selectedItems.remove(pdf)
        }
        notifyItemChanged(currentList.indexOf(pdf))
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

    fun getSelectedItems(): List<PdfModel> {
        return selectedItems.toList()
    }

    inner class BinViewHolder(
        private val binding: ItemBinBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pdf: PdfModel, isSelected: Boolean) {
            binding.apply {
                textPdfName.text = pdf.name
                textFileSize.text = pdf.sizeFormatted
                textDeletedDate.text = "Deleted on ${pdf.modifiedAt}" // Format date properly

                // Handle selection state
                handleSelectionState(isSelected)

                // Set click listeners
                root.setOnClickListener {
                    listener.onPdfClicked(pdf)
                }

                root.setOnLongClickListener {
                    listener.onPdfLongClicked(pdf)
                }

                // Action buttons
                buttonRestore.setOnClickListener {
                    listener.onRestorePdf(pdf)
                }

                buttonDelete.setOnClickListener {
                    listener.onDeletePdf(pdf)
                }

                // Selection checkbox
                checkboxSelection.setOnCheckedChangeListener(null) // Prevent recursive calls
                checkboxSelection.isChecked = isSelected
                checkboxSelection.setOnCheckedChangeListener { _, isChecked ->
                    toggleSelection(pdf, isChecked)
                }
            }
        }

        private fun handleSelectionState(isSelected: Boolean) {
            binding.apply {
                if (selectedItems.isNotEmpty()) {
                    // Selection mode is active
                    checkboxSelection.visibility = android.view.View.VISIBLE
                    selectionOverlay.visibility = android.view.View.VISIBLE
                    layoutActions.visibility = android.view.View.GONE
                    root.alpha = if (isSelected) 0.7f else 1.0f
                } else {
                    // Normal mode
                    checkboxSelection.visibility = android.view.View.GONE
                    selectionOverlay.visibility = android.view.View.GONE
                    layoutActions.visibility = android.view.View.VISIBLE
                    root.alpha = 1.0f
                }

                // Update background based on selection
                root.setBackgroundResource(
                    if (isSelected) R.drawable.bg_item_selected
                    else R.drawable.bg_rounded_corner
                )
            }
        }

        private fun toggleSelection(pdf: PdfModel, isSelected: Boolean) {
            binding.checkboxSelection.isChecked = isSelected
            handleSelectionState(isSelected)
            listener.onPdfSelected(pdf, isSelected)
        }
    }
}

class BinDiffCallback : DiffUtil.ItemCallback<PdfModel>() {
    override fun areItemsTheSame(oldItem: PdfModel, newItem: PdfModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PdfModel, newItem: PdfModel): Boolean {
        return oldItem == newItem
    }
}
package com.PRO.propdf.ui.home.viewholder

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.R
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.ItemPdfBinding
import com.PRO.propdf.utils.FileUtils
import com.PRO.propdf.utils.ThemeUtils
import kotlinx.coroutines.*

class PdfViewHolder(
    private val binding: ItemPdfBinding,
    private val listener: PdfItemListener
) : RecyclerView.ViewHolder(binding.root) {

    private var thumbnailJob: Job? = null

    interface PdfItemListener {
        fun onPdfClicked(pdf: PdfModel)
        fun onPdfLongClicked(pdf: PdfModel): Boolean
        fun onPdfSelected(pdf: PdfModel, isSelected: Boolean)
    }

    fun bind(pdf: PdfModel, isSelected: Boolean, isSelectionMode: Boolean = false) {
        binding.apply {
            textPdfName.text = pdf.name
            textFileSize.text = pdf.sizeFormatted
            textPageCount.text = getPageCountText(pdf.pageCount)
            textLastOpened.text = getLastOpenedText(pdf)

            // Load thumbnail if available
            loadThumbnail(pdf)

            // Handle selection state
            handleSelectionState(isSelected, isSelectionMode)

            // Set click listeners
            root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(pdf, !isSelected)
                } else {
                    listener.onPdfClicked(pdf)
                }
            }

            root.setOnLongClickListener {
                listener.onPdfLongClicked(pdf)
            }
        }
    }

    private fun loadThumbnail(pdf: PdfModel) {
        thumbnailJob?.cancel() // Cancel previous thumbnail loading

        if (pdf.thumbnailPath != null) {
            // Load from cached thumbnail
            thumbnailJob = CoroutineScope(Dispatchers.Main).launch {
                val bitmap = withContext(Dispatchers.IO) {
                    FileUtils.loadBitmapFromPath(pdf.thumbnailPath!!)
                }
                bitmap?.let {
                    binding.imageThumbnail.setImageBitmap(it)
                    binding.imagePdfIcon.visibility = View.GONE
                } ?: run {
                    showPdfIcon()
                }
            }
        } else {
            showPdfIcon()
        }
    }

    private fun showPdfIcon() {
        binding.imagePdfIcon.visibility = View.VISIBLE
        binding.imageThumbnail.setImageDrawable(null)
        
        val pdfColor = ThemeUtils.getAttributeColor(
            binding.root.context, 
            R.attr.colorPrimary
        )
        val pdfDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_pdf)
        pdfDrawable?.setTint(pdfColor)
        binding.imagePdfIcon.setImageDrawable(pdfDrawable)
    }

    private fun getPageCountText(pageCount: Int): String {
        return when (pageCount) {
            0 -> "No pages"
            1 -> "1 page"
            else -> "$pageCount pages"
        }
    }

    private fun getLastOpenedText(pdf: PdfModel): String {
        // This would typically come from recent files data
        // For now, show creation date or modified date
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return "Created: ${dateFormat.format(pdf.createdAt)}"
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

    private fun toggleSelection(pdf: PdfModel, isSelected: Boolean) {
        binding.checkboxSelection.isChecked = isSelected
        handleSelectionState(isSelected, true)
        listener.onPdfSelected(pdf, isSelected)
    }

    fun setSelectionMode(isSelectionMode: Boolean) {
        binding.checkboxSelection.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        binding.selectionOverlay.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
    }

    fun recycle() {
        thumbnailJob?.cancel()
        binding.imageThumbnail.setImageDrawable(null)
    }

    companion object {
        const val VIEW_TYPE = 1
    }
}
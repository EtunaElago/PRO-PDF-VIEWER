package com.PRO.propdf.ui.home.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.PRO.propdf.databinding.LayoutLoadingBinding

class LoadingViewHolder(
    private val binding: LayoutLoadingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(loadingType: LoadingType = LoadingType.NORMAL) {
        binding.apply {
            when (loadingType) {
                LoadingType.NORMAL -> {
                    progressBar.isIndeterminate = true
                    textLoading.text = "Loading..."
                }
                LoadingType.SEARCH -> {
                    progressBar.isIndeterminate = true
                    textLoading.text = "Searching..."
                }
                LoadingType.MORE -> {
                    progressBar.isIndeterminate = true
                    textLoading.text = "Loading more..."
                }
            }
        }
    }

    enum class LoadingType {
        NORMAL, SEARCH, MORE
    }
}
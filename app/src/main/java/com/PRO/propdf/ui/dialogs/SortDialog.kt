package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.data.model.SortConfig
import com.PRO.propdf.databinding.DialogSortBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SortDialog(
    private val currentSort: SortConfig,
    private val onSortSelected: (SortConfig) -> Unit
) : DialogFragment() {

    private var _binding: DialogSortBinding? = null
    private val binding get() = _binding!!

    private var selectedSortBy = currentSort.sortBy
    private var selectedSortOrder = currentSort.sortOrder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSortBinding.inflate(requireActivity().layoutInflater)

        setupSortOptions()
        setupSortOrderOptions()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.sort_by))
            .setView(binding.root)
            .setPositiveButton(R.string.apply) { dialog, _ ->
                applySort()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun setupSortOptions() {
        binding.radioGroupSortBy.setOnCheckedChangeListener { _, checkedId ->
            selectedSortBy = when (checkedId) {
                R.id.radio_name -> "name"
                R.id.radio_date -> "date"
                R.id.radio_size -> "size"
                R.id.radio_last_opened -> "last_opened"
                else -> "name"
            }
        }

        // Set initial selection
        when (currentSort.sortBy) {
            "name" -> binding.radioName.isChecked = true
            "date" -> binding.radioDate.isChecked = true
            "size" -> binding.radioSize.isChecked = true
            "last_opened" -> binding.radioLastOpened.isChecked = true
            else -> binding.radioName.isChecked = true
        }
    }

    private fun setupSortOrderOptions() {
        binding.radioGroupSortOrder.setOnCheckedChangeListener { _, checkedId ->
            selectedSortOrder = when (checkedId) {
                R.id.radio_ascending -> "ascending"
                R.id.radio_descending -> "descending"
                else -> "ascending"
            }
        }

        // Set initial selection
        when (currentSort.sortOrder) {
            "ascending" -> binding.radioAscending.isChecked = true
            "descending" -> binding.radioDescending.isChecked = true
            else -> binding.radioAscending.isChecked = true
        }
    }

    private fun applySort() {
        val newSortConfig = SortConfig(
            sortBy = selectedSortBy,
            sortOrder = selectedSortOrder
        )
        onSortSelected(newSortConfig)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SortDialog"
    }
}
package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.databinding.DialogViewModeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ViewModeDialog(
    private val currentViewMode: String,
    private val onViewModeSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogViewModeBinding? = null
    private val binding get() = _binding!!

    private var selectedViewMode = currentViewMode

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogViewModeBinding.inflate(requireActivity().layoutInflater)

        setupViewModeOptions()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_view_mode))
            .setView(binding.root)
            .setPositiveButton(R.string.apply) { dialog, _ ->
                applyViewMode()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun setupViewModeOptions() {
        binding.radioGroupViewMode.setOnCheckedChangeListener { _, checkedId ->
            selectedViewMode = when (checkedId) {
                R.id.radio_grid -> "grid"
                R.id.radio_list -> "list"
                else -> "grid"
            }
        }

        // Set initial selection
        when (currentViewMode) {
            "grid" -> binding.radioGrid.isChecked = true
            "list" -> binding.radioList.isChecked = true
            else -> binding.radioGrid.isChecked = true
        }
    }

    private fun applyViewMode() {
        onViewModeSelected(selectedViewMode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ViewModeDialog"
    }
}
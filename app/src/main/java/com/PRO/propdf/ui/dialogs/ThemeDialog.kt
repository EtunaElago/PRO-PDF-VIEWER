package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.databinding.DialogThemeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ThemeDialog(
    private val currentTheme: String,
    private val onThemeSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogThemeBinding? = null
    private val binding get() = _binding!!

    private var selectedTheme = currentTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogThemeBinding.inflate(requireActivity().layoutInflater)

        setupThemeOptions()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_theme))
            .setView(binding.root)
            .setPositiveButton(R.string.apply) { dialog, _ ->
                applyTheme()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun setupThemeOptions() {
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            selectedTheme = when (checkedId) {
                R.id.radio_light -> "light"
                R.id.radio_dark -> "dark"
                R.id.radio_system -> "system"
                else -> "system"
            }
        }

        // Set initial selection
        when (currentTheme) {
            "light" -> binding.radioLight.isChecked = true
            "dark" -> binding.radioDark.isChecked = true
            "system" -> binding.radioSystem.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }
    }

    private fun applyTheme() {
        onThemeSelected(selectedTheme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ThemeDialog"
    }
}
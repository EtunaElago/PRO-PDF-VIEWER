package com.PRO.propdf.ui.pdfviewer

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.databinding.DialogSearchPdfBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchPdfDialog(
    private val onSearch: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogSearchPdfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSearchPdfBinding.inflate(requireActivity().layoutInflater)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.search_in_pdf))
            .setView(binding.root)
            .setPositiveButton(R.string.search, null) // We'll override this
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                setOnShowListener {
                    val positiveButton = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    positiveButton.isEnabled = false
                    positiveButton.setOnClickListener {
                        performSearch()
                    }
                    
                    setupInputListeners()
                }
            }
    }

    private fun setupInputListeners() {
        binding.editSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInput(s.toString())
            }
        })

        binding.editSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Request focus and show keyboard
        binding.editSearchQuery.requestFocus()
    }

    private fun validateInput(searchQuery: String) {
        val positiveButton = (dialog as? android.app.AlertDialog)?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        
        if (searchQuery.isBlank()) {
            positiveButton?.isEnabled = false
        } else {
            positiveButton?.isEnabled = true
        }
    }

    private fun performSearch() {
        val searchQuery = binding.editSearchQuery.text.toString().trim()
        
        if (searchQuery.isNotBlank()) {
            onSearch(searchQuery)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SearchPdfDialog"
    }
}
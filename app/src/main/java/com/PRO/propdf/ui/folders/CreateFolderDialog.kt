package com.PRO.propdf.ui.folders

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.databinding.DialogCreateFolderBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateFolderDialog(
    private val parentFolderId: Long? = null,
    private val onCreateFolder: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateFolderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateFolderBinding.inflate(requireActivity().layoutInflater)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_new_folder))
            .setView(binding.root)
            .setPositiveButton(R.string.create, null) // We'll override this
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                setOnShowListener {
                    val positiveButton = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    positiveButton.isEnabled = false
                    positiveButton.setOnClickListener {
                        createFolder()
                    }
                    
                    setupInputListeners()
                }
            }
    }

    private fun setupInputListeners() {
        binding.editFolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInput(s.toString())
            }
        })

        binding.editFolderName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createFolder()
                true
            } else {
                false
            }
        }

        // Request focus and show keyboard
        binding.editFolderName.requestFocus()
    }

    private fun validateInput(folderName: String) {
        val positiveButton = (dialog as? android.app.AlertDialog)?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        
        when {
            folderName.isBlank() -> {
                binding.textInputLayout.error = getString(R.string.folder_name_cannot_empty)
                positiveButton?.isEnabled = false
            }
            folderName.length > 50 -> {
                binding.textInputLayout.error = getString(R.string.folder_name_too_long)
                positiveButton?.isEnabled = false
            }
            folderName.contains("/") || folderName.contains("\\") -> {
                binding.textInputLayout.error = getString(R.string.folder_name_invalid_chars)
                positiveButton?.isEnabled = false
            }
            else -> {
                binding.textInputLayout.error = null
                positiveButton?.isEnabled = true
            }
        }
    }

    private fun createFolder() {
        val folderName = binding.editFolderName.text.toString().trim()
        
        if (folderName.isNotBlank()) {
            onCreateFolder(folderName)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateFolderDialog"
    }
}
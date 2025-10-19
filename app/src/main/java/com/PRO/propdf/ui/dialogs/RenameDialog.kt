package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.databinding.DialogRenameBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RenameDialog(
    private val currentName: String,
    private val onRename: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogRenameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRenameBinding.inflate(requireActivity().layoutInflater)

        binding.editName.setText(currentName)
        binding.editName.setSelection(currentName.length)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.rename))
            .setView(binding.root)
            .setPositiveButton(R.string.rename, null) // We'll override this
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                setOnShowListener {
                    val positiveButton = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    positiveButton.isEnabled = true
                    positiveButton.setOnClickListener {
                        performRename()
                    }
                    
                    setupInputListeners()
                }
            }
    }

    private fun setupInputListeners() {
        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInput(s.toString())
            }
        })

        binding.editName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performRename()
                true
            } else {
                false
            }
        }

        // Request focus and show keyboard
        binding.editName.requestFocus()
        binding.editName.requestFocus()
    }

    private fun validateInput(newName: String) {
        val positiveButton = (dialog as? android.app.AlertDialog)?.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        
        when {
            newName.isBlank() -> {
                binding.textInputLayout.error = getString(R.string.name_cannot_empty)
                positiveButton?.isEnabled = false
            }
            newName.length > 100 -> {
                binding.textInputLayout.error = getString(R.string.name_too_long)
                positiveButton?.isEnabled = false
            }
            newName == currentName -> {
                binding.textInputLayout.error = getString(R.string.name_unchanged)
                positiveButton?.isEnabled = false
            }
            else -> {
                binding.textInputLayout.error = null
                positiveButton?.isEnabled = true
            }
        }
    }

    private fun performRename() {
        val newName = binding.editName.text.toString().trim()
        
        if (newName.isNotBlank() && newName != currentName) {
            onRename(newName)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RenameDialog"
    }
}
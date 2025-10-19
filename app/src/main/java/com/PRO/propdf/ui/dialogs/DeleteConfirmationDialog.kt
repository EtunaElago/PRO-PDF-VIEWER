package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteConfirmationDialog(
    private val itemName: String,
    private val isPermanent: Boolean = false,
    private val onConfirm: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = if (isPermanent) {
            getString(R.string.delete_permanently)
        } else {
            getString(R.string.move_to_bin)
        }
        
        val message = if (isPermanent) {
            getString(R.string.delete_permanently_confirmation, itemName)
        } else {
            getString(R.string.move_to_bin_confirmation, itemName)
        }
        
        val positiveButtonText = if (isPermanent) {
            getString(R.string.delete)
        } else {
            getString(R.string.move_to_bin)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "DeleteConfirmationDialog"
    }
}
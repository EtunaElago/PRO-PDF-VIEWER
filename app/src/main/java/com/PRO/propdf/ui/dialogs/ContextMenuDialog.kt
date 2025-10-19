package com.PRO.propdf.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.PRO.propdf.R
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.data.model.RecentModel
import com.PRO.propdf.databinding.DialogContextMenuBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ContextMenuDialog(
    private val item: Any,
    private val onMoveToBin: ((Any) -> Unit)? = null,
    private val onRename: ((Any) -> Unit)? = null,
    private val onMoveTo: ((Any) -> Unit)? = null,
    private val onCopyTo: ((Any) -> Unit)? = null,
    private val onRemoveFromFolder: ((Any) -> Unit)? = null,
    private val onRestore: ((Any) -> Unit)? = null,
    private val onDeletePermanently: ((Any) -> Unit)? = null
) : DialogFragment() {

    private var _binding: DialogContextMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogContextMenuBinding.inflate(requireActivity().layoutInflater)

        setupMenuItems()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupMenuItems() {
        binding.menuRemove.visibility = android.view.View.GONE
        binding.menuRename.visibility = android.view.View.GONE
        binding.menuMoveTo.visibility = android.view.View.GONE
        binding.menuCopyTo.visibility = android.view.View.GONE
        binding.menuMoveToBin.visibility = android.view.View.GONE
        binding.menuRestore.visibility = android.view.View.GONE
        binding.menuDeletePermanently.visibility = android.view.View.GONE

        when (item) {
            is PdfModel -> {
                setupPdfMenu(item)
            }
            is FolderModel -> {
                setupFolderMenu(item)
            }
            is RecentModel -> {
                setupRecentMenu(item)
            }
        }

        // Common dismiss action
        binding.menuCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupPdfMenu(pdf: PdfModel) {
        if (pdf.isInBin) {
            // PDF is in bin - show restore and delete permanently
            binding.menuRestore.visibility = android.view.View.VISIBLE
            binding.menuDeletePermanently.visibility = android.view.View.VISIBLE
            
            binding.menuRestore.setOnClickListener {
                onRestore?.invoke(pdf)
                dismiss()
            }
            
            binding.menuDeletePermanently.setOnClickListener {
                onDeletePermanently?.invoke(pdf)
                dismiss()
            }
        } else {
            // Normal PDF - show regular options
            binding.menuMoveToBin.visibility = android.view.View.VISIBLE
            binding.menuRename.visibility = android.view.View.VISIBLE
            binding.menuMoveTo.visibility = android.view.View.VISIBLE
            binding.menuCopyTo.visibility = android.view.View.VISIBLE
            
            binding.menuMoveToBin.setOnClickListener {
                onMoveToBin?.invoke(pdf)
                dismiss()
            }
            
            binding.menuRename.setOnClickListener {
                onRename?.invoke(pdf)
                dismiss()
            }
            
            binding.menuMoveTo.setOnClickListener {
                onMoveTo?.invoke(pdf)
                dismiss()
            }
            
            binding.menuCopyTo.setOnClickListener {
                onCopyTo?.invoke(pdf)
                dismiss()
            }
        }
    }

    private fun setupFolderMenu(folder: FolderModel) {
        if (folder.canBeDeleted) {
            binding.menuMoveToBin.visibility = android.view.View.VISIBLE
            binding.menuMoveToBin.setOnClickListener {
                onMoveToBin?.invoke(folder)
                dismiss()
            }
        }
        
        binding.menuRename.visibility = android.view.View.VISIBLE
        binding.menuRename.setOnClickListener {
            onRename?.invoke(folder)
            dismiss()
        }
        
        binding.menuMoveTo.visibility = android.view.View.VISIBLE
        binding.menuMoveTo.setOnClickListener {
            onMoveTo?.invoke(folder)
            dismiss()
        }
    }

    private fun setupRecentMenu(recent: RecentModel) {
        binding.menuRemove.visibility = android.view.View.VISIBLE
        binding.menuRemove.setOnClickListener {
            onRemoveFromFolder?.invoke(recent)
            dismiss()
        }
        
        binding.menuMoveToBin.visibility = android.view.View.VISIBLE
        binding.menuMoveToBin.setOnClickListener {
            onMoveToBin?.invoke(recent)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ContextMenuDialog"
    }
}
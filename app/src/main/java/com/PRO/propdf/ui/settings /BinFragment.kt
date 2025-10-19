package com.PRO.propdf.ui.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.PRO.propdf.R
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.FragmentBinBinding
import com.PRO.propdf.ui.dialogs.ContextMenuDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BinFragment : Fragment(), BinAdapter.BinItemListener {

    private var _binding: FragmentBinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BinViewModel by viewModels()
    private lateinit var binAdapter: BinAdapter

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<PdfModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.bin)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.inflateMenu(R.menu.bin_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_empty_bin -> {
                    showEmptyBinConfirmation()
                    true
                }
                R.id.action_select_all -> {
                    selectAllItems()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binAdapter = BinAdapter(this)
        binding.recyclerViewBin.apply {
            adapter = binAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.binState.collectLatest { state ->
                when (state) {
                    is BinViewModel.BinState.Loading -> {
                        showLoading(true)
                    }
                    is BinViewModel.BinState.Success -> {
                        showLoading(false)
                        binAdapter.submitList(state.pdfs)
                        handleEmptyState(state.pdfs.isEmpty())
                    }
                    is BinViewModel.BinState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collectLatest { state ->
                when (state) {
                    is BinViewModel.OperationState.Success -> {
                        showSnackbar(state.message)
                        viewModel.loadBinItems()
                    }
                    is BinViewModel.OperationState.Error -> {
                        showSnackbar(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.emptyState.buttonAction1.setOnClickListener {
            // Navigate back to home
            findNavController().navigateUp()
        }
    }

    private fun showEmptyBinConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.empty_bin_permanently))
            .setMessage(getString(R.string.empty_bin_confirmation))
            .setPositiveButton(R.string.delete) { dialog, _ ->
                viewModel.emptyBin()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun selectAllItems() {
        if (!isSelectionMode) {
            enterSelectionMode()
        }
        binAdapter.selectAll()
        selectedItems.clear()
        selectedItems.addAll(binAdapter.getCurrentList())
        updateSelectionMode()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewBin.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (isEmpty) {
            binding.emptyState.imageEmptyState.setImageResource(R.drawable.ic_empty_bin)
            binding.emptyState.textEmptyTitle.text = getString(R.string.empty_bin)
            binding.emptyState.textEmptyDescription.text = getString(R.string.empty_bin_description)
            binding.emptyState.buttonAction1.text = getString(R.string.back_to_home)
            binding.emptyState.buttonAction2.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // BinItemListener implementations
    override fun onPdfClicked(pdf: PdfModel) {
        if (isSelectionMode) {
            toggleItemSelection(pdf)
        } else {
            // Show restore/delete options
            showContextMenu(pdf)
        }
    }

    override fun onPdfLongClicked(pdf: PdfModel): Boolean {
        if (!isSelectionMode) {
            enterSelectionMode()
        }
        toggleItemSelection(pdf)
        return true
    }

    override fun onPdfSelected(pdf: PdfModel, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(pdf)
        } else {
            selectedItems.remove(pdf)
        }
        updateSelectionMode()
    }

    override fun onRestorePdf(pdf: PdfModel) {
        viewModel.restoreFromBin(pdf.id)
    }

    override fun onDeletePdf(pdf: PdfModel) {
        viewModel.deletePermanently(pdf.id)
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        binding.toolbar.menu.findItem(R.id.action_empty_bin)?.isVisible = false
        
        binding.toolbar.title = "0 selected"
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        binAdapter.clearSelection()
        
        binding.toolbar.menu.findItem(R.id.action_empty_bin)?.isVisible = true
        
        binding.toolbar.title = getString(R.string.bin)
    }

    private fun toggleItemSelection(pdf: PdfModel) {
        val isSelected = selectedItems.contains(pdf)
        if (isSelected) {
            selectedItems.remove(pdf)
        } else {
            selectedItems.add(pdf)
        }
        binAdapter.setItemSelected(pdf, !isSelected)
        updateSelectionMode()
    }

    private fun updateSelectionMode() {
        binding.toolbar.title = "${selectedItems.size} selected"
        
        if (selectedItems.isEmpty()) {
            exitSelectionMode()
        }
    }

    private fun showContextMenu(pdf: PdfModel) {
        ContextMenuDialog(
            item = pdf,
            onMoveToBin = null, // Not applicable in bin
            onRestore = {
                viewModel.restoreFromBin(pdf.id)
            },
            onDeletePermanently = {
                viewModel.deletePermanently(pdf.id)
            }
        ).show(parentFragmentManager, "ContextMenuDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
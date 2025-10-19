package com.PRO.propdf.ui.recents

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.PRO.propdf.R
import com.PRO.propdf.data.model.RecentModel
import com.PRO.propdf.databinding.FragmentRecentsBinding
import com.PRO.propdf.ui.dialogs.ContextMenuDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecentsFragment : Fragment(), RecentsAdapter.RecentItemListener {

    private var _binding: FragmentRecentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecentsViewModel by viewModels()
    private lateinit var recentsAdapter: RecentsAdapter

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<RecentModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSwipeRefresh()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.recents)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.inflateMenu(R.menu.recents_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_clear_all -> {
                    showClearAllConfirmation()
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
        recentsAdapter = RecentsAdapter(this)
        binding.recyclerViewRecents.apply {
            adapter = recentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentsState.collectLatest { state ->
                when (state) {
                    is RecentsViewModel.RecentsState.Loading -> {
                        showLoading(true)
                    }
                    is RecentsViewModel.RecentsState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        recentsAdapter.submitList(state.recents)
                        handleEmptyState(state.recents.isEmpty())
                    }
                    is RecentsViewModel.RecentsState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collectLatest { state ->
                when (state) {
                    is RecentsViewModel.OperationState.Success -> {
                        showSnackbar(state.message)
                        // Refresh the list
                        viewModel.loadRecentFiles()
                    }
                    is RecentsViewModel.OperationState.Error -> {
                        showSnackbar(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController().navigate(R.id.action_recentsFragment_to_homeFragment)
                    true
                }
                R.id.navigation_recents -> {
                    // Already on recents
                    true
                }
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.action_recentsFragment_to_settingsFragment)
                    true
                }
                else -> false
            }
        }

        binding.emptyState.buttonAction1.setOnClickListener {
            // Navigate to home to add PDFs
            findNavController().navigate(R.id.action_recentsFragment_to_homeFragment)
        }

        binding.emptyState.buttonAction2.setOnClickListener {
            // Clear the empty state message
            binding.emptyState.root.visibility = View.GONE
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRecentFiles()
        }
    }

    private fun showClearAllConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all_recents))
            .setMessage(getString(R.string.clear_all_recents_confirmation))
            .setPositiveButton(R.string.clear) { dialog, _ ->
                viewModel.clearAllRecentFiles()
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
        recentsAdapter.selectAll()
        selectedItems.clear()
        selectedItems.addAll(recentsAdapter.getCurrentList())
        updateSelectionMode()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewRecents.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (isEmpty) {
            binding.emptyState.imageEmptyState.setImageResource(R.drawable.ic_empty_recent)
            binding.emptyState.textEmptyTitle.text = getString(R.string.no_recent_files)
            binding.emptyState.textEmptyDescription.text = getString(R.string.no_recent_files_description)
            binding.emptyState.buttonAction1.text = getString(R.string.browse_files)
            binding.emptyState.buttonAction2.text = getString(R.string.dismiss)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // RecentItemListener implementations
    override fun onRecentClicked(recent: RecentModel) {
        if (isSelectionMode) {
            toggleItemSelection(recent)
        } else {
            if (recent.isPdfExists) {
                val action = RecentsFragmentDirections.actionRecentsFragmentToPdfViewerFragment(
                    pdfId = recent.pdfId,
                    pdfPath = recent.pdfPath,
                    pdfName = recent.pdfName
                )
                findNavController().navigate(action)
            } else {
                showSnackbar(getString(R.string.pdf_file_not_found))
                // Remove from recents if file doesn't exist
                viewModel.removeRecentFile(recent.id)
            }
        }
    }

    override fun onRecentLongClicked(recent: RecentModel): Boolean {
        if (!isSelectionMode) {
            enterSelectionMode()
        }
        toggleItemSelection(recent)
        return true
    }

    override fun onRecentSelected(recent: RecentModel, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(recent)
        } else {
            selectedItems.remove(recent)
        }
        updateSelectionMode()
    }

    override fun onRemoveRecent(recent: RecentModel) {
        viewModel.removeRecentFile(recent.id)
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        binding.toolbar.menu.findItem(R.id.action_clear_all)?.isVisible = false
        
        binding.toolbar.title = "0 selected"
        binding.bottomAppBar.visibility = View.GONE
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        recentsAdapter.clearSelection()
        
        binding.toolbar.menu.findItem(R.id.action_clear_all)?.isVisible = true
        
        binding.toolbar.title = getString(R.string.recents)
        binding.bottomAppBar.visibility = View.VISIBLE
    }

    private fun toggleItemSelection(recent: RecentModel) {
        val isSelected = selectedItems.contains(recent)
        if (isSelected) {
            selectedItems.remove(recent)
        } else {
            selectedItems.add(recent)
        }
        recentsAdapter.setItemSelected(recent, !isSelected)
        updateSelectionMode()
    }

    private fun updateSelectionMode() {
        binding.toolbar.title = "${selectedItems.size} selected"
        
        if (selectedItems.isEmpty()) {
            exitSelectionMode()
        }
    }

    private fun showContextMenu(recent: RecentModel) {
        ContextMenuDialog(
            item = recent,
            onMoveToBin = { 
                // Recent items don't go to bin, just remove from recents
                viewModel.removeRecentFile(recent.id)
            },
            onRemoveFromFolder = {
                viewModel.removeRecentFile(recent.id)
            }
        ).show(parentFragmentManager, "ContextMenuDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
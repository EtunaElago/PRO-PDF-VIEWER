package com.PRO.propdf.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.PRO.propdf.R
import com.PRO.propdf.data.model.AppConstants
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.FragmentHomeBinding
import com.PRO.propdf.ui.dialogs.ContextMenuDialog
import com.PRO.propdf.ui.dialogs.CreateFolderDialog
import com.PRO.propdf.ui.dialogs.SortDialog
import com.PRO.propdf.ui.dialogs.ThemeDialog
import com.PRO.propdf.ui.dialogs.ViewModeDialog
import com.PRO.propdf.utils.ThemeUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), HomeAdapter.HomeItemListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var homeAdapter: HomeAdapter

    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    // Handle search - will be implemented in search functionality
                    true
                }
                R.id.action_sort -> {
                    showSortDialog()
                    true
                }
                R.id.action_theme -> {
                    showThemeDialog()
                    true
                }
                R.id.action_view_mode -> {
                    showViewModeDialog()
                    true
                }
                else -> false
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            showAddMenu()
        }
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(this)
        binding.recyclerViewHome.apply {
            adapter = homeAdapter
            setHasFixedSize(true)
            updateLayoutManager()
        }
    }

    private fun updateLayoutManager() {
        val viewMode = viewModel.getViewMode()
        binding.recyclerViewHome.layoutManager = if (viewMode == AppConstants.VIEW_MODE_GRID) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.HomeUiState.Loading -> {
                        showLoading(true)
                    }
                    is HomeViewModel.HomeUiState.Success -> {
                        showLoading(false)
                        homeAdapter.submitList(state.items)
                        handleEmptyState(state.items.isEmpty())
                    }
                    is HomeViewModel.HomeUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collectLatest { state ->
                when (state) {
                    is HomeViewModel.OperationState.Success -> {
                        showSnackbar(state.message)
                    }
                    is HomeViewModel.OperationState.Error -> {
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
                    // Already on home
                    true
                }
                R.id.navigation_recents -> {
                    findNavController().navigate(R.id.action_homeFragment_to_recentsFragment)
                    true
                }
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.topAppBar)
        popupMenu.menuInflater.inflate(R.menu.add_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_create_folder -> {
                    showCreateFolderDialog()
                    true
                }
                R.id.action_add_pdf -> {
                    viewModel.addPdf()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showCreateFolderDialog() {
        CreateFolderDialog { folderName ->
            viewModel.createFolder(folderName)
        }.show(parentFragmentManager, "CreateFolderDialog")
    }

    private fun showSortDialog() {
        SortDialog(
            currentSort = viewModel.getSortConfig(),
            onSortSelected = { sortConfig ->
                viewModel.updateSortConfig(sortConfig)
            }
        ).show(parentFragmentManager, "SortDialog")
    }

    private fun showThemeDialog() {
        ThemeDialog(
            currentTheme = viewModel.getCurrentTheme(),
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme)
                ThemeUtils.applyTheme(theme)
            }
        ).show(parentFragmentManager, "ThemeDialog")
    }

    private fun showViewModeDialog() {
        ViewModeDialog(
            currentViewMode = viewModel.getViewMode(),
            onViewModeSelected = { viewMode ->
                viewModel.updateViewMode(viewMode)
                updateLayoutManager()
            }
        ).show(parentFragmentManager, "ViewModeDialog")
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewHome.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // HomeItemListener implementations
    override fun onFolderClicked(folder: FolderModel) {
        val action = HomeFragmentDirections.actionHomeFragmentToFolderFragment(
            folderId = folder.id,
            folderName = folder.name
        )
        findNavController().navigate(action)
    }

    override fun onPdfClicked(pdf: PdfModel) {
        val action = HomeFragmentDirections.actionHomeFragmentToPdfViewerFragment(
            pdfId = pdf.id,
            pdfPath = pdf.path,
            pdfName = pdf.name
        )
        findNavController().navigate(action)
    }

    override fun onItemLongClicked(item: Any) {
        if (!isSelectionMode) {
            enterSelectionMode()
        }
        toggleItemSelection(item)
    }

    override fun onItemSelected(item: Any, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
        updateSelectionMode()
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        binding.topAppBar.menu.findItem(R.id.action_search)?.isVisible = false
        binding.topAppBar.menu.findItem(R.id.action_sort)?.isVisible = false
        binding.topAppBar.menu.findItem(R.id.action_theme)?.isVisible = false
        binding.topAppBar.menu.findItem(R.id.action_view_mode)?.isVisible = false
        
        binding.topAppBar.title = "0 selected"
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        homeAdapter.clearSelection()
        
        binding.topAppBar.menu.findItem(R.id.action_search)?.isVisible = true
        binding.topAppBar.menu.findItem(R.id.action_sort)?.isVisible = true
        binding.topAppBar.menu.findItem(R.id.action_theme)?.isVisible = true
        binding.topAppBar.menu.findItem(R.id.action_view_mode)?.isVisible = true
        
        binding.topAppBar.title = getString(R.string.app_name)
    }

    private fun toggleItemSelection(item: Any) {
        val isSelected = selectedItems.contains(item)
        if (isSelected) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        homeAdapter.setItemSelected(item, !isSelected)
        updateSelectionMode()
    }

    private fun updateSelectionMode() {
        binding.topAppBar.title = "${selectedItems.size} selected"
        
        if (selectedItems.isEmpty()) {
            exitSelectionMode()
        }
    }

    private fun showContextMenu(item: Any) {
        ContextMenuDialog(
            item = item,
            onMoveToBin = { itemToDelete ->
                when (itemToDelete) {
                    is PdfModel -> viewModel.moveToBin(itemToDelete.id)
                    is FolderModel -> viewModel.deleteFolder(itemToDelete.id)
                }
            },
            onRename = { itemToRename ->
                // Implement rename functionality
            },
            onMoveTo = { itemToMove ->
                // Implement move functionality
            }
        ).show(parentFragmentManager, "ContextMenuDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
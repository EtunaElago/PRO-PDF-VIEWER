package com.PRO.propdf.ui.folders

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.PRO.propdf.R
import com.PRO.propdf.data.model.AppConstants
import com.PRO.propdf.data.model.FolderModel
import com.PRO.propdf.data.model.PdfModel
import com.PRO.propdf.databinding.FragmentFolderBinding
import com.PRO.propdf.ui.dialogs.ContextMenuDialog
import com.PRO.propdf.ui.dialogs.CreateFolderDialog
import com.PRO.propdf.ui.home.HomeAdapter
import com.PRO.propdf.utils.ThemeUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderFragment : Fragment(), HomeAdapter.HomeItemListener {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FolderViewModel by viewModels()
    private val args: FolderFragmentArgs by navArgs()

    private lateinit var homeAdapter: HomeAdapter
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Load folder contents
        viewModel.loadFolderContents(args.folderId)
    }

    private fun setupToolbar() {
        binding.toolbar.title = args.folderName
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.inflateMenu(R.menu.folder_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    // Handle search
                    true
                }
                R.id.action_sort -> {
                    showSortDialog()
                    true
                }
                R.id.action_create_folder -> {
                    showCreateFolderDialog()
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
        homeAdapter = HomeAdapter(this)
        binding.recyclerView.apply {
            adapter = homeAdapter
            setHasFixedSize(true)
            updateLayoutManager()
        }
    }

    private fun updateLayoutManager() {
        val viewMode = viewModel.getViewMode()
        binding.recyclerView.layoutManager = if (viewMode == AppConstants.VIEW_MODE_GRID) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.folderState.collectLatest { state ->
                when (state) {
                    is FolderViewModel.FolderState.Loading -> {
                        showLoading(true)
                    }
                    is FolderViewModel.FolderState.Success -> {
                        showLoading(false)
                        val allItems = mutableListOf<Any>().apply {
                            addAll(state.folders)
                            addAll(state.pdfs)
                        }
                        homeAdapter.submitList(allItems)
                        handleEmptyState(allItems.isEmpty())
                    }
                    is FolderViewModel.FolderState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collectLatest { state ->
                when (state) {
                    is FolderViewModel.OperationState.Success -> {
                        showSnackbar(state.message)
                    }
                    is FolderViewModel.OperationState.Error -> {
                        showSnackbar(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            showAddMenu()
        }
    }

    private fun showAddMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.fabAdd)
        popupMenu.menuInflater.inflate(R.menu.add_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_create_folder -> {
                    showCreateFolderDialog()
                    true
                }
                R.id.action_add_pdf -> {
                    viewModel.addPdfToFolder(args.folderId)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showCreateFolderDialog() {
        CreateFolderDialog(
            parentFolderId = args.folderId,
            onCreateFolder = { folderName ->
                viewModel.createFolder(folderName, args.folderId)
            }
        ).show(parentFragmentManager, "CreateFolderDialog")
    }

    private fun showSortDialog() {
        // Implement sort dialog similar to HomeFragment
    }

    private fun selectAllItems() {
        // Implement select all functionality
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.fabAdd.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    // HomeItemListener implementations
    override fun onFolderClicked(folder: FolderModel) {
        val action = FolderFragmentDirections.actionFolderFragmentToSelf(
            folderId = folder.id,
            folderName = folder.name
        )
        findNavController().navigate(action)
    }

    override fun onPdfClicked(pdf: PdfModel) {
        val action = FolderFragmentDirections.actionFolderFragmentToPdfViewerFragment(
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
        binding.toolbar.menu.findItem(R.id.action_search)?.isVisible = false
        binding.toolbar.menu.findItem(R.id.action_sort)?.isVisible = false
        binding.toolbar.menu.findItem(R.id.action_create_folder)?.isVisible = false
        
        binding.toolbar.title = "0 selected"
        binding.fabAdd.hide()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        homeAdapter.clearSelection()
        
        binding.toolbar.menu.findItem(R.id.action_search)?.isVisible = true
        binding.toolbar.menu.findItem(R.id.action_sort)?.isVisible = true
        binding.toolbar.menu.findItem(R.id.action_create_folder)?.isVisible = true
        
        binding.toolbar.title = args.folderName
        binding.fabAdd.show()
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
        binding.toolbar.title = "${selectedItems.size} selected"
        
        if (selectedItems.isEmpty()) {
            exitSelectionMode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
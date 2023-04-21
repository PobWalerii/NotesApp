package com.example.notesapp.ui.listnotes

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentListNotesBinding
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.ui.actionbar.AppActionBar
import com.example.notesapp.utils.ConfirmationDialog.showConfirmationDialog
import com.example.notesapp.utils.MessageNotPossible.showMessageNotPossible
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ListNotesFragment : Fragment() {

    @Inject
    lateinit var appSettings: AppSettings
    @Inject
    lateinit var actionBar: AppActionBar

    private var _binding: FragmentListNotesBinding? = null
    private val binding get() = requireNotNull(_binding)

    private lateinit var adapter: NotesListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemTouchHelper: ItemTouchHelper

    private val viewModel by viewModels<NotesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewForSnack()
        setupActionBar()
        setupRecycler()
        loadData()
        setupButtonAddListener()
        setupItemClickListener()
        observeConnectStatus()
        observeLoadStatus()
        observeDateChanged()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.listNotes.collect {
                binding.visibleInfoText = it.isEmpty()
                if(it.isNotEmpty()) {
                    checkIfScrollingIsNecessary(it[0].id)
                }
                adapter.setList(it)
            }
        }
    }

    private fun checkIfScrollingIsNecessary(firstItemId: Long) {
        val current = viewModel.idInsertOrEdit.value
        if (current != 0L && firstItemId == current) {
            adapter.setCurrentId(current)
            recyclerView.layoutManager?.scrollToPosition(0)
            viewModel.setCurrentIdTo(0L)
        }
    }

    private fun observeLoadStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoad.collect {
                binding.isLoad = it
                binding.firstRun = viewModel.firstRun.value
                showActionButton(it)

            }
        }
    }

    private fun observeConnectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isConnectStatus.collect { isConnect ->
                reactToConnectionStatusChange(isConnect)
            }
        }
    }
    private fun reactToConnectionStatusChange(isConnect: Boolean) {
        binding.floatingActionButton.isEnabled = isConnect
        itemTouchHelper.attachToRecyclerView(
            if(appSettings.deleteIfSwiped.value && isConnect) recyclerView else null
        )
    }

    private fun setupAdapter() {
        adapter = NotesListAdapter(appSettings)
        adapter.setHasStableIds(true)
    }

    private fun setupRecycler() {
        recyclerView = binding.recycler
        recyclerView.adapter = adapter
        setupTouchHelper()
    }

    internal fun deleteNote(position: Int) {
        val note = adapter.getItemFromPosition(position)
        adapter.setCurrentId(note.id)
        viewModel.setCurrentIdTo(note.id)
        showConfirmationDialog(
            R.string.title_delete,
            R.string.text_delete,
            requireContext(),
            onConfirmed = {
                if(viewModel.isConnectStatus.value) {
                    viewModel.deleteNote(note)
                } else {
                    showMessageNotPossible(requireContext())
                    adapter.notifyItemChanged(position)
                }
            },
            onCancelled = {
                adapter.notifyItemChanged(position)
            }
        )
    }

    private fun setupButtonAddListener() {
        binding.floatingActionButton.setOnClickListener {
            if ( appSettings.defaultAddIfClick.value ) {
                viewModel.addNote()
            } else {
                findNavController().navigate(
                    ListNotesFragmentDirections.actionListNotesFragmentToEditNotesFragment(0L)
                )
            }
        }
    }

    private fun setupItemClickListener() {
        adapter.setOnItemClickListener(object : NotesListAdapter.OnItemClickListener {
            override fun onItemClick(currentId: Long) {
                startEditNote(currentId)
            }
        })
    }

    private fun startEditNote(currentId: Long) {
        if(viewModel.isLoad.value && viewModel.idInsertOrEdit.value == currentId) {
            Toast.makeText(context,R.string.please_wait_update,Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setCurrentIdTo(currentId)
            findNavController().navigate(
                ListNotesFragmentDirections.actionListNotesFragmentToEditNotesFragment(currentId)
            )
        }
    }

    private fun setupActionBar() {

        actionBar.initAppbar(
            requireActivity(),
            R.string.app_name,
            viewLifecycleOwner,
            isHomeKey = false,
            isSettings = true
        )

        viewLifecycleOwner.lifecycleScope.launch {
            actionBar.isItemMenuPressed.collect {
                if(it=="settings") {
                    startSettingsFragment()
                }
            }
        }
    }

    private fun setViewForSnack() {
        appSettings.showViewForSnack = binding.recycler
    }

    private fun showActionButton(isLoad: Boolean) {
        if (!isLoad) {
            val behavior =
                (binding.floatingActionButton.layoutParams as CoordinatorLayout.LayoutParams)
                    .behavior as HideBottomViewOnScrollBehavior
            behavior.slideUp(binding.floatingActionButton)
        }
    }

    private fun setupTouchHelper() {
        itemTouchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recycler: RecyclerView,
                    holder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ) = false

                override fun onSwiped(holder: RecyclerView.ViewHolder, dir: Int) {
                    deleteNote(holder.adapterPosition)
                }
            })
    }

    private fun startSettingsFragment() {
        findNavController().navigate(ListNotesFragmentDirections.actionListNotesFragmentToSettingsFragment())
    }

    private fun observeDateChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            appSettings.isDateChanged.collect {
                adapter.refresh()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.example.notesapp.ui.listnotes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DEFAULT_ADD_IF_CLICK
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.constants.KeyConstants.DELETE_IF_SWIPED
import com.example.notesapp.databinding.FragmentListNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.RequestToDelete
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListNotesFragment : Fragment() {

    private var _binding: FragmentListNotesBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var adapter: NotesListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var defaultHeader: String
    private var defaultSpecificationLine: Boolean = true
    private var defaultAddIfClick: Boolean = true
    private var deleteIfSwiped: Boolean = true

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
        setupActionBar()
        loadSettings()
        setSettingsInAdapter()
        setupRecycler()
        loadData()
        loadRequestToDelete()
        setupButtonAddListener()
        setupItemClickListener()
        observeInsertNote()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDatabase().collect {
                adapter.setList(it)
            }
        }
    }

    private fun observeInsertNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insertedIdFlow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context,"$it",Toast.LENGTH_LONG).show()
                }
                if(it!=0L) {
                    adapter.setCurrentId(it)
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter = NotesListAdapter()
        adapter.setHasStableIds(true)
    }

    private fun setSettingsInAdapter() {
        adapter.setSettings(defaultSpecificationLine, defaultHeader)
    }

    private fun setupRecycler() {
        recyclerView = binding.recycler
        recyclerView.adapter = adapter
        if(deleteIfSwiped) {
            itemTouchHelper =
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    override fun onMove(
                        recycler: RecyclerView,
                        holder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder,
                    ) = false

                    override fun onSwiped(holder: RecyclerView.ViewHolder, dir: Int) {
                        deleteNoteRequest(holder.adapterPosition)
                    }
                })
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteNoteRequest(position: Int) {
        val note = adapter.getItemFromPosition(position)
        adapter.setCurrentId(note.id)
        adapter.notifyDataSetChanged()
        RequestToDelete.requestToDelete(requireContext(), position)
    }

    private fun loadRequestToDelete() {
        viewLifecycleOwner.lifecycleScope.launch {
            RequestToDelete.isRequestToDeleteOkFlow.collect { position ->
                if(position!=0) {
                    if (position < 0) {
                        adapter.notifyItemChanged(-position)
                    } else {
                        val note = adapter.getItemFromPosition(position)
                        viewModel.deleteNote(note)
                    }
                }
            }
        }
    }

    private fun setupActionBar() {

        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = getString(R.string.app_name)
        actionBar?.setDisplayHomeAsUpEnabled(false)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save).isVisible = false
                menu.findItem(R.id.delete).isVisible = false
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                startSettingsFragment()
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun startSettingsFragment() {
        findNavController().navigate(ListNotesFragmentDirections.actionListNotesFragmentToSettingsFragment())
    }

    private fun setupButtonAddListener() {
        binding.floatingActionButton.setOnClickListener {
            if ( defaultAddIfClick ) {
                viewModel.addNote()
            } else {
                findNavController().navigate(
                    ListNotesFragmentDirections.actionListNotesFragmentToEditNotesFragment(
                        0L
                    )
                )
            }
        }
    }

    private fun setupItemClickListener() {
        adapter.setOnItemClickListener(object : NotesListAdapter.OnItemClickListener {
            override fun onItemClick(currentId: Long) {
                findNavController().navigate(
                    ListNotesFragmentDirections.actionListNotesFragmentToEditNotesFragment(
                        currentId
                    )
                )
            }
        })
    }


    private fun loadSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        defaultHeader = sPref.getString("defaultHeader", DEFAULT_HEADER).toString()
        defaultSpecificationLine = sPref.getBoolean("specificationLine", DEFAULT_SPECIFICATION_LINE)
        defaultAddIfClick = sPref.getBoolean("defaultAddIfClick", DEFAULT_ADD_IF_CLICK)
        deleteIfSwiped = sPref.getBoolean("deleteIfSwiped", DELETE_IF_SWIPED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
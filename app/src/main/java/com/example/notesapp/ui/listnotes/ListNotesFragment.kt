package com.example.notesapp.ui.listnotes

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
import com.example.notesapp.databinding.FragmentListNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.RequestToDelete.requestToDelete
import dagger.hilt.android.AndroidEntryPoint
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

    private val viewModel by viewModels<NotesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSettings()
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
        setupRecycler()
        loadData()

        setupButtonAddListener()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDatabase().collect {
                adapter.setList(it)
            }
        }
    }

    private fun setupAdapter() {
        adapter = NotesListAdapter(defaultSpecificationLine, defaultHeader)
        adapter.setHasStableIds(true)
    }

    private fun setupRecycler() {
        recyclerView = binding.recycler
        recyclerView.adapter = adapter
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
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun deleteNote(position: Int) {
        if(requestToDelete(requireContext())) {
            val noteItem = adapter.getItemFromPosition(position)
            viewModel.deleteNote(noteItem)
        } else {
            adapter.notifyItemChanged(position)
        }
    }

    private fun setupActionBar() {

        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = getString(R.string.app_name)
        actionBar?.setDisplayHomeAsUpEnabled(false)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save).isVisible = false
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


    private fun loadSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        defaultHeader = sPref.getString("defaultHeader", DEFAULT_HEADER).toString()
        defaultSpecificationLine = sPref.getBoolean("defaultHeader", DEFAULT_SPECIFICATION_LINE)
        defaultAddIfClick = sPref.getBoolean("defaultHeader", DEFAULT_ADD_IF_CLICK)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.example.notesapp.ui.listnotes

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.TextView
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
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.databinding.FragmentListNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.ConnectReceiver
import com.example.notesapp.utils.DateChangedBroadcastReceiver
import com.example.notesapp.utils.RequestToDelete
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ListNotesFragment : Fragment() {

    @Inject
    lateinit var connectReceiver: ConnectReceiver

    private var _binding: FragmentListNotesBinding? = null
    private val binding get() = requireNotNull(_binding)

    private lateinit var adapter: NotesListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var defaultHeader: String
    private var defaultSpecificationLine: Boolean = true
    private var defaultAddIfClick: Boolean = true
    private var deleteIfSwiped: Boolean = true
    private var showMessageInternetOk: Boolean = false

    private lateinit var receiver: DateChangedBroadcastReceiver

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
        observeConnectStatus()
        observeErrorMessages()
        observeLoadStatus()
    }

    private fun observeLoadStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadedFlow.collect {
                if(viewModel.firstDataLoad) {
                    binding.visibleProgressRound = it
                } else {
                    binding.visibleProgressHorizontal = it
                }
            }
        }
    }

    private fun observeConnectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    binding.visibleProgressHorizontal = isConnect
                    binding.floatingActionButton.isEnabled = isConnect
                    if (isConnect) {
                        if(connectReceiver.getShowTextOk() && showMessageInternetOk) {
                            connectReceiver.setShowTextOk()
                            Toast.makeText(context, R.string.text_internet_ok, Toast.LENGTH_LONG)
                                .show()
                        }
                        itemTouchHelper.attachToRecyclerView(recyclerView)
                    } else {
                        if(connectReceiver.getShowTextLost()) {
                            connectReceiver.setShowTextLost()
                            showSnackbar()
                        }
                        itemTouchHelper.attachToRecyclerView(null)
                    }
                }
            }
        }
    }

    private fun observeErrorMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceErrorFlow.collect { message ->
                if(message.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDatabase().collect {
                adapter.setList(it)
                binding.visibleInfoText = it.isEmpty()
                viewModel.firstDataLoad = it.isEmpty()
                if(it.isEmpty()) {
                    viewModel.loadRemoutData()
                } else {
                    viewModel.getInsertedOrEditedIdValue().let { id ->
                        if (id != 0L) {
                            val position = adapter.setCurrentId(id)
                            if (position != -1) {
                                recyclerView.layoutManager?.scrollToPosition(position)
                            }
                            viewModel.setInsertedOrEditedIdNull()
                        }
                    }
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
        }
    }

    private fun deleteNoteRequest(position: Int) {
        val note = adapter.getItemFromPosition(position)
        adapter.setCurrentId(note.id)
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
                        if(connectReceiver.isConnectStatusFlow.value) {
                            viewModel.deleteNote(note)
                        } else {
                            adapter.notifyItemChanged(position)
                        }
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
        showMessageInternetOk = sPref.getBoolean("showMessageInternetOk", SHOW_MESSAGE_INTERNET_OK)
    }


    override fun onResume() {
        super.onResume()
        broadcastDateRegister()
    }

    private fun broadcastDateRegister() {
        receiver= DateChangedBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        //filter.addAction(Intent.ACTION_TIME_TICK)
        activity?.registerReceiver(receiver, filter)
        obserweDateChanged()
    }

    private fun obserweDateChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            receiver.isDateChangedFlow.collect { isDateChanged ->
                if(isDateChanged) {
                    adapter.refresh()
                }
            }
        }
    }

    private fun showSnackbar() {
        val snackbar = Snackbar.make(
            binding.coordinator,
            R.string.text_no_internet,
            Snackbar.LENGTH_LONG
        )
        val snackbarView = snackbar.view
        val textView: TextView =
            snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)
        textView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.warning,
            0,
            0,
            0
        )
        //textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(R.dimen.snackbar_icon_padding))
        //snackbar.setAction("OK") { snackbar.dismiss() }
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(receiver)
        _binding = null
    }

}
package com.example.notesapp.ui.listnotes

import android.content.Intent
import android.content.IntentFilter
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
import com.example.notesapp.utils.DateChangedBroadcastReceiver
import com.example.notesapp.services.BackService
import com.example.notesapp.settings.AppSettings
import com.example.notesapp.utils.AppActionBar
import com.example.notesapp.utils.ConfirmationDialog.showConfirmationDialog
import com.example.notesapp.utils.MessageNotPossible.showMessageNotPossible
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ListNotesFragment : Fragment() {

    @Inject
    lateinit var appSettings: AppSettings

    private var _binding: FragmentListNotesBinding? = null
    private val binding get() = requireNotNull(_binding)

    private lateinit var adapter: NotesListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemTouchHelper: ItemTouchHelper

    //private var showInfoLoad: Boolean = false
    //private var showInfoLoadIfStart: Boolean = false

    private lateinit var receiver: DateChangedBroadcastReceiver

    private val viewModel by viewModels<NotesViewModel>()

    private lateinit var actionBar: AppActionBar

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
        setupRecycler()
        refreshSettings()
        loadData()
        //startRemoteService()
        setupButtonAddListener()
        setupItemClickListener()
        observeLoadStatus()
        observeConnectStatus()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDatabase().collect {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.visibleInfoText = it.isEmpty()
                    adapter.setList(it)


                    /*
                    if (viewModel.isStartApp) {
                        if (viewModel.getInitialDataUpload()) {
                            viewModel.isStartApp = false
                            viewModel.firstDataLoad = false
                            binding.visibleProgressRound = false
                            startRemoteService()
                        } else {
                            viewModel.firstDataLoad = it.isEmpty()
                            if(viewModel.isConnectStatus.value) {
                                viewModel.loadRemoteData()
                            } else {
                                viewModel.setIsLoadCanceled()
                            }
                        }
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

                     */


                }
            }
        }
    }







    private fun observeLoadStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadFlow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    actionBar.isLoadProcess(it)
                    //if (viewModel.firstDataLoad) {
                    //    binding.visibleProgressRound = it
                    //} else {
                    //    binding.visibleProgressHorizontal = it
                    //}
                    binding.isLoad = it
                    binding.firstRun = viewModel.firstRun.value
                    if (!it) {
                        val behavior =
                            (binding.floatingActionButton.layoutParams as CoordinatorLayout.LayoutParams)
                                .behavior as HideBottomViewOnScrollBehavior
                        behavior.slideUp(binding.floatingActionButton)
                    }
                }
            }
        }
    }

    private fun observeConnectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isConnectStatus.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    reactToConnectionStatusChange(isConnect)
                }
            }
        }
    }
    private fun reactToConnectionStatusChange(isConnect: Boolean) {
        binding.floatingActionButton.isEnabled = isConnect
        itemTouchHelper.attachToRecyclerView(
            if(appSettings.deleteIfSwiped.value && isConnect) recyclerView else null
        )
    }



/*
    private fun observeRemoteDatabaseChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRemoteDatabaseChangedFlow.collect { isChanged ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (isChanged) {
                        if (viewModel.isConnectStatus.value) {
                            viewModel.loadRemoteData()
                        } else {
                            viewModel.setIsLoadCanceled()
                        }
                    }
                }
            }
        }
    }

 */

    private fun setupAdapter() {
        adapter = NotesListAdapter()
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
    }

    private fun deleteNote(position: Int) {
        val note = adapter.getItemFromPosition(position)
        adapter.setCurrentId(note.id)

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

    private fun startSettingsFragment() {
        findNavController().navigate(ListNotesFragmentDirections.actionListNotesFragmentToSettingsFragment())
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
                findNavController().navigate(
                    ListNotesFragmentDirections.actionListNotesFragmentToEditNotesFragment(currentId)
                )
            }
        })
    }

    private fun refreshSettings() {

        adapter.setSettings(
            appSettings.specificationLine.value,
            appSettings.defaultHeader.value
        )

        //showConnectStatus.setShowMessageInternetOk(appSettings.showMessageInternetOk.value)

        //val startDelayValue = appSettings.startDelayValue.value
        //val queryDelayValue = appSettings.queryDelayValue.value
        //showInfoLoad = queryDelayValue > 0
        //showInfoLoadIfStart = startDelayValue > 0
    }

    override fun onResume() {
        super.onResume()
        broadcastDateRegister()
        observeCounterDelay()
    }

    private fun broadcastDateRegister() {
        receiver= DateChangedBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        activity?.registerReceiver(receiver, filter)
        observeDateChanged()
    }

    private fun observeDateChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            receiver.isDateChangedFlow.collect { isDateChanged ->
                if(isDateChanged) {
                    adapter.refresh()
                }
            }
        }
    }

    private fun setupActionBar() {
        actionBar = AppActionBar(
            requireActivity(),
            requireContext(),
            R.string.app_name,
            viewLifecycleOwner,
            isHomeKey = false,
            isSettings = true,
        )
        viewLifecycleOwner.lifecycleScope.launch {
            actionBar.isItemMenuPressedFlow.collect {
                if(it=="settings") {
                    startSettingsFragment()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(receiver)
        _binding = null
    }

    private fun observeCounterDelay() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.counterDelay.collect { start ->
                CoroutineScope(Dispatchers.Main).launch {
                    actionBar.startCounter(start)
                }
            }
        }
    }

}
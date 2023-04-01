package com.example.notesapp.ui.listnotes

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants
import com.example.notesapp.constants.KeyConstants.DEFAULT_ADD_IF_CLICK
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.constants.KeyConstants.DELETE_IF_SWIPED
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.databinding.FragmentListNotesBinding
import com.example.notesapp.servicesandreceivers.ConnectReceiver
import com.example.notesapp.utils.DateChangedBroadcastReceiver
import com.example.notesapp.servicesandreceivers.RemoteService
import com.example.notesapp.data.database.entitys.Notes
import com.example.notesapp.utils.AppActionBar
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private var defaultAddIfClick: Boolean = true
    private var deleteIfSwiped: Boolean = true
    private var showMessageInternetOk: Boolean = false
    private var showInfoLoad: Boolean = false
    private var showInfoLoadIfStart: Boolean = false

    private lateinit var receiver: DateChangedBroadcastReceiver

    private var counter: Job? = null

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
        observeConnectStatus()
        loadAndRefreshSettings()
        loadData()
        startRemoteService()
        setupButtonAddListener()
        setupItemClickListener()
        observeErrorMessages()
        observeLoadStatus()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadDatabase().collect {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.visibleInfoText = it.isEmpty()
                    adapter.setList(it)
                    if (viewModel.isStartApp) {
                        if (viewModel.getInitialDataUpload()) {
                            viewModel.isStartApp = false
                            viewModel.firstDataLoad = false
                            binding.visibleProgressRound = false
                            startRemoteService()
                        } else {
                            viewModel.firstDataLoad = it.isEmpty()
                            if(connectReceiver.isConnectStatusFlow.value) {
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
                }
            }
        }
    }

    private fun observeCounterDelay() {
        counter = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.counterDelayFlow.collect { seconds ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (!viewModel.isLoadedFlow.value) {
                        actionBar.setSpannableTitle(if (seconds > 0) {getString(R.string.text_wait)+" $seconds"} else "")
                    }
                }
            }
        }
    }

    private fun startRemoteService() {
        if (!viewModel.isStartApp) {
            val serviceIntent = Intent(context, RemoteService::class.java)
            context?.startService(serviceIntent)
            observeRemoteDatabaseChanged()
        }
    }

    private fun observeLoadStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadedFlow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    if ((showInfoLoad && !viewModel.isStartApp) || (showInfoLoadIfStart && viewModel.isStartApp)) {
                        actionBar.setSpannableTitle(
                            if (it) getString(R.string.text_load)  else ""
                        )
                    }
                    if (viewModel.firstDataLoad) {
                        binding.visibleProgressRound = it
                    } else {
                        binding.visibleProgressHorizontal = it
                    }
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
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    reactToConnectionStatusChange(isConnect)
                }
            }
        }
    }

    private fun reactToConnectionStatusChange(isConnect: Boolean) {
        binding.floatingActionButton.isEnabled = isConnect
        if (isConnect) {
            if(showMessageInternetOk && !viewModel.lastConnectionStatus) {
                Toast.makeText(context, R.string.text_internet_ok, Toast.LENGTH_LONG).show()
            }
            viewModel.restartLoadRemoteData()
        } else {
            if (viewModel.lastConnectionStatus) {
                if (viewModel.isStartApp) {
                    Toast.makeText(context, R.string.text_no_internet, Toast.LENGTH_LONG).show()
                } else {
                    showSnack()
                }
            }
            viewModel.stopLoadRemoteData()
        }
        viewModel.lastConnectionStatus = isConnect
        attachTouchHelper(isConnect)
    }

    private fun observeErrorMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceErrorFlow.collect { message ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (message.isNotEmpty()) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        viewModel.clearServiceErrorMessage()
                    }
                }
            }
        }
    }

    private fun observeRemoteDatabaseChanged() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRemoteDatabaseChangedFlow.collect { isChanged ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (isChanged) {
                        if (connectReceiver.isConnectStatusFlow.value) {
                            viewModel.loadRemoteData()
                        } else {
                            viewModel.setIsLoadCanceled()
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
                    deleteNoteRequest(holder.adapterPosition)
                }
            })
    }

    private fun deleteNoteRequest(position: Int) {
        val note = adapter.getItemFromPosition(position)
        adapter.setCurrentId(note.id)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_delete)
            .setIcon(R.drawable.warning)
            .setMessage(R.string.text_delete)
            .setPositiveButton(R.string.but_yes_txt) { _, _ ->
                deleteNote(position, note)
            }
            .setNegativeButton(R.string.but_no_txt) { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .create()
        dialog.setOnCancelListener {
            adapter.notifyItemChanged(position)
        }
        dialog.show()
    }

    private fun deleteNote(position: Int, note: Notes) {
        if(connectReceiver.isConnectStatusFlow.value) {
            viewModel.deleteNote(note)
        } else {
            Toast.makeText(context,R.string.operation_not_possible,Toast.LENGTH_LONG).show()
            adapter.notifyItemChanged(position)
        }
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

    private fun loadAndRefreshSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)

        val defaultHeader = sPref.getString("defaultHeader", DEFAULT_HEADER).toString()
        val defaultSpecificationLine = sPref.getBoolean("specificationLine", DEFAULT_SPECIFICATION_LINE)
        adapter.setSettings(defaultSpecificationLine, defaultHeader)

        defaultAddIfClick = sPref.getBoolean("defaultAddIfClick", DEFAULT_ADD_IF_CLICK)
        deleteIfSwiped = sPref.getBoolean("deleteIfSwiped", DELETE_IF_SWIPED)
        showMessageInternetOk = sPref.getBoolean("showMessageInternetOk", SHOW_MESSAGE_INTERNET_OK)

        val startDelayValue = sPref.getInt("startDelayValue", KeyConstants.TIME_DELAY_START)
        val queryDelayValue = sPref.getInt("queryDelayValue", KeyConstants.TIME_DELAY_QUERY)
        val requestIntervalValue = sPref.getInt("requestIntervalValue", KeyConstants.INTERVAL_REQUESTS)
        val operationDelayValue = sPref.getInt("operationDelayValue", KeyConstants.TIME_DELAY_OPERATION)
        viewModel.refreshRepoSettings(startDelayValue, queryDelayValue, requestIntervalValue, operationDelayValue)
        showInfoLoad = queryDelayValue > 0
        showInfoLoadIfStart = startDelayValue > 0
    }

    private fun attachTouchHelper(isConnect: Boolean) {
        if(deleteIfSwiped && isConnect) {
            itemTouchHelper.attachToRecyclerView(recyclerView)
        } else {
            itemTouchHelper.attachToRecyclerView(null)
        }
    }

    override fun onResume() {
        super.onResume()
        connectReceiver.initReceiver()
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

    private fun showSnack() {
        val snack = Snackbar.make(
            binding.coordinator,
            R.string.text_no_internet,
            Snackbar.LENGTH_LONG
        )
        val snackView = snack.view
        val textView: TextView =
            snackView.findViewById(com.google.android.material.R.id.snackbar_text)
        textView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.warning,
            0,
            0,
            0
        )
        textView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
        snack.show()
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

    override fun onPause() {
        super.onPause()
        counter?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(receiver)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(context, RemoteService::class.java)
        context?.stopService(serviceIntent)
    }

}
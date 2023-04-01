package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DATE_CHANGE_WHEN_CONTENT
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.servicesandreceivers.ConnectReceiver
import com.example.notesapp.utils.AppActionBar
import com.example.notesapp.utils.HideKeyboard.hideKeyboardFromView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

    @Inject
    lateinit var connectReceiver: ConnectReceiver

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditViewModel>()

    private val args: EditNotesFragmentArgs by navArgs()

    private var counter: Job? = null

    private var showMessageInternetOk = false

    private lateinit var actionBar: AppActionBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        loadSettings()
        setListenersDataChanged()
        observeErrorMessages()
        setupActionBar()
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
        if (isConnect) {
            if (showMessageInternetOk && !viewModel.lastConnectionStatus) {
                Toast.makeText(context, R.string.text_internet_ok, Toast.LENGTH_LONG).show()
            }
        } else {
            if (viewModel.lastConnectionStatus) {
                Toast.makeText(context, R.string.text_no_internet, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.lastConnectionStatus = isConnect
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

    private fun setListenersDataChanged() {
        binding.titleNoteText.addTextChangedListener {
            definitionOfChange()
        }
        binding.textNoteText.addTextChangedListener {
            definitionOfChange()
        }
    }

    private fun definitionOfChange() {
        actionBar.setButtonVisible("save",
            viewModel.currentNoteName != binding.titleNoteText.text.toString() ||
            viewModel.currentNoteSpecification != binding.textNoteText.text.toString()
        )
    }

    private fun loadData() {
        viewModel.currentId = args.idNote
        viewModel.setStartFlowParameters()
        if (args.idNote != 0L) {
            lifecycle.coroutineScope.launch {
                viewModel.getNoteById(args.idNote).collect { note ->
                    if (note != null) {
                        viewModel.currentNote = note
                        viewModel.currentNoteName = note.noteName
                        viewModel.currentNoteSpecification = note.noteSpecification
                        viewModel.currentNoteDate = note.noteDate
                        setDataForEdit()
                    }
                }
            }
        }
    }

    private fun setDataForEdit() {
        binding.noteName = viewModel.currentNoteName
        binding.noteSpecification = viewModel.currentNoteSpecification
    }

    private fun loadSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        viewModel.dateChangedStrategy = sPref.getBoolean("dateChanged",
            DATE_CHANGE_WHEN_CONTENT
        )
        showMessageInternetOk = sPref.getBoolean("showMessageInternetOk",
            SHOW_MESSAGE_INTERNET_OK
        )
    }

    private fun setupActionBar() {

        actionBar = AppActionBar(
            requireActivity(),
            requireContext(),
            if (args.idNote == 0L) R.string.add_note else R.string.edit_note,
            viewLifecycleOwner,
            isDelete = args.idNote != 0L,
        )

        viewLifecycleOwner.lifecycleScope.launch {
            actionBar.isItemMenuPressedFlow.collect {
                if(it=="save") {
                    saveNote()
                } else if(it=="delete") {
                    deleteNoteRequest()
                }
            }
        }
    }

    private fun deleteNoteRequest() {
        hideKeyboardFromView(requireContext(), requireView())
        if(connectReceiver.isConnectStatusFlow.value) {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_delete)
                .setIcon(R.drawable.warning)
                .setMessage(R.string.text_delete)
                .setPositiveButton(R.string.but_yes_txt) { _, _ ->
                    deleteNote()
                }
                .setNegativeButton(R.string.but_no_txt) { _, _ -> }
                .create()
            dialog.show()
        } else {
            showMessageNoConnect()
        }
    }

    private fun deleteNote() {
        if(connectReceiver.isConnectStatusFlow.value) {
            viewModel.deleteNote()
            observeEditNote()
        } else {
            showMessageNoConnect()
        }
    }

    private fun saveNote() {
        hideKeyboardFromView(requireContext(), requireView())
        if(connectReceiver.isConnectStatusFlow.value) {
            viewModel.saveNote(
                binding.titleNoteText.text.toString(),
                binding.textNoteText.text.toString()
            )
            observeEditNote()
        } else {
            showMessageNoConnect()
        }
    }

    private fun showMessageNoConnect() {
        Toast.makeText(context,R.string.operation_not_possible,Toast.LENGTH_LONG).show()
    }

    private fun observeEditNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isNoteEditedFlow.collect {
                if(it) {
                    (activity as MainActivity).onSupportNavigateUp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeConnectStatus()
        observeCounterDelay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        counter?.cancel()
        showCount(0)
    }

    private fun observeCounterDelay() {
        counter = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.counterDelayFlow.collect { seconds ->
                CoroutineScope(Dispatchers.Main).launch {
                    showCount(seconds)
                }
            }
        }
    }

    private fun showCount(seconds: Int) {
        actionBar.setSpannableTitle(
            if (seconds > 0) {
                getString(R.string.text_wait) + " $seconds"
            } else ""
        )
    }



}
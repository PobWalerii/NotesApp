package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DATE_CHANGE_WHEN_CONTENT
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.servicesandreceivers.ConnectReceiver
import com.example.notesapp.utils.AppActionBar
import com.example.notesapp.utils.ConfirmationDeleteDialog.showConfirmationDeleteDialog
import com.example.notesapp.utils.ConfirmationDeleteDialog.showMessageNotPossible
import com.example.notesapp.utils.HideKeyboard.hideKeyboardFromView
import com.example.notesapp.utils.ShowConnectStatus
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
    @Inject
    lateinit var showConnectStatus: ShowConnectStatus

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditViewModel>()

    private val args: EditNotesFragmentArgs by navArgs()

    private var counter: Job? = null

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
        setupActionBar()
    }

    private fun observeConnectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                CoroutineScope(Dispatchers.Main).launch {
                    showConnectStatus.showStatus(isConnect)
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
                CoroutineScope(Dispatchers.Main).launch {
                    hideKeyboardFromView(requireContext(), requireView())
                    if (it == "save") {
                        saveNote()
                    } else if (it == "delete") {
                        deleteNote()
                    }
                }
            }
        }
    }

    private fun deleteNote() {
        if (connectReceiver.isConnectStatusFlow.value) {
            showConfirmationDeleteDialog(
                requireContext(),
                onConfirmed = {
                    if (connectReceiver.isConnectStatusFlow.value) {
                        viewModel.deleteNote()
                        observeEditNote()
                    } else {
                        showMessageNotPossible(requireContext())
                    }
                },
                onCancelled = { }
            )
        } else {
            showMessageNotPossible(requireContext())
        }
    }

    private fun saveNote() {
        if(connectReceiver.isConnectStatusFlow.value) {
            viewModel.saveNote(
                binding.titleNoteText.text.toString(),
                binding.textNoteText.text.toString()
            )
            observeEditNote()
        } else {
            showMessageNotPossible(requireContext())
        }
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
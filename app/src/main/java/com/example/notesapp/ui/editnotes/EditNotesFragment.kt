package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.AppActionBar
import com.example.notesapp.utils.ConfirmationDialog.showConfirmationDialog
import com.example.notesapp.utils.HideKeyboard.hideKeyboardFromView
import com.example.notesapp.utils.MessageNotPossible.showMessageNotPossible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

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
        setListenersDataChanged()
        setupActionBar()
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
        actionBar.setButtonVisible("save",viewModel.isChangedNote(
            binding.titleNoteText.text.toString(),
            binding.textNoteText.text.toString()
        ))
    }

    private fun loadData() {
        viewModel.currentId = args.idNote
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
                    observeEditNote()
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
        if(viewModel.isConnectStatus.value) {
            showConfirmationDialog(
                R.string.title_delete,
                R.string.text_delete,
                requireContext(),
                onConfirmed = {
                    if(viewModel.isConnectStatus.value) {
                        viewModel.deleteNote()
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
        if(viewModel.isConnectStatus.value) {
            viewModel.saveNote(
                binding.titleNoteText.text.toString(),
                binding.textNoteText.text.toString()
            )
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
        observeEditNote()
        observeCounterDelay()
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            definitionOfChange()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        hideKeyboardFromView(requireContext(),requireView())
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
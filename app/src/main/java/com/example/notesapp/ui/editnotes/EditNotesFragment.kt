package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.actionbar.AppActionBar
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.ConfirmationDialog.showConfirmationDialog
import com.example.notesapp.utils.HideKeyboard.hideKeyboardFromView
import com.example.notesapp.utils.MessageNotPossible.showMessageNotPossible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditViewModel>()

    private val args: EditNotesFragmentArgs by navArgs()

    @Inject
    lateinit var actionBar: AppActionBar

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

    private fun loadData() {
        if (args.idNote != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getNoteById(args.idNote)
                viewModel.isLoadedNote.collect {
                    setDataForEdit()
                }
            }
        }
    }

    private fun setDataForEdit() {
        binding.noteName = viewModel.currentNoteName
        binding.noteSpecification = viewModel.currentNoteSpecification
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

    private fun setupActionBar() {

        actionBar.initAppbar(
            requireActivity(),
            if (args.idNote == 0L) R.string.add_note else R.string.edit_note,
            viewLifecycleOwner,
            isDelete = args.idNote != 0L
        )

        viewLifecycleOwner.lifecycleScope.launch {
            actionBar.isItemMenuPressed.collect {
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
    }

}
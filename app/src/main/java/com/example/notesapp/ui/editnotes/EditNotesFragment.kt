package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.RequestToDelete
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditViewModel>()

    private val args: EditNotesFragmentArgs by navArgs()

    lateinit var appbarMenu: Menu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        loadData()
        loadSettings()
        setListenersDataChanged()
        loadRequestToDelete()
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
        appbarMenu.findItem(R.id.save).isVisible =
            viewModel.currentNoteName != binding.titleNoteText.text.toString() ||
            viewModel.currentNoteSpecification != binding.textNoteText.text.toString()
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
        viewModel.dateChangetStrategy = sPref.getBoolean("dateChanget",
            KeyConstants.DATE_CHANGE_WHEN_CONTENT
        )
    }

    private fun setupActionBar() {
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = getString(R.string.app_name) + ". " +
                if(args.idNote == 0L) getString(R.string.add_note) else getString(R.string.edit_note)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                appbarMenu = menu
                menu.findItem(R.id.save).isVisible = false
                menu.findItem(R.id.settings).isVisible = false
                menu.findItem(R.id.delete).isVisible = args.idNote != 0L
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    android.R.id.home -> {
                        (activity as MainActivity).onSupportNavigateUp()
                    }
                    R.id.delete -> {
                        RequestToDelete.requestToDelete(requireContext(), -100)
                    }
                    R.id.save -> {
                        saveNote()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadRequestToDelete() {
        viewLifecycleOwner.lifecycleScope.launch {
            RequestToDelete.isRequestToDeleteOkFlow.collect { position ->
                if(position==-100) {
                    viewModel.deleteNote()
                    observeEditNote()
                }
            }
        }
    }

    private fun saveNote() {
        viewModel.saveNote(
            binding.titleNoteText.text.toString(),
            binding.textNoteText.text.toString()
        )
        observeEditNote()
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

     override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
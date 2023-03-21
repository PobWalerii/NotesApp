package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.listnotes.NotesViewModel
import com.example.notesapp.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<EditViewModel>()

    private val args: EditNotesFragmentArgs by navArgs()

    lateinit var appbarMenu: Menu

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
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
        setListenersDataChanged()
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
        lifecycle.coroutineScope.launch {
            val note = viewModel.getNoteById(args.idNote)
            if( note == null ) {
                (activity as MainActivity).onSupportNavigateUp()
            } else {
                viewModel.currentNoteName = note.noteName
                viewModel.currentNoteSpecification = note.noteSpecification
                viewModel.currentNoteDate = note.noteDate
                binding.noteName = note.noteName
                binding.noteSpecification = note.noteSpecification
            }
        }
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
                if(menuItem.itemId == android.R.id.home) (activity as MainActivity).onSupportNavigateUp()
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setVisibleSaveButton(visible: Boolean) {
        appbarMenu.findItem(R.id.save).isVisible = visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
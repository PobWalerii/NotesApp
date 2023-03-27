package com.example.notesapp.ui.editnotes

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import com.example.notesapp.constants.KeyConstants.DATE_CHANGE_WHEN_CONTENT
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.ui.main.MainActivity
import com.example.notesapp.utils.ConnectReceiver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private lateinit var appbarMenu: Menu

    private var showMessageInternetOk = false

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
        observeConnectStatus()
        observeErrorMessages()
        setupActionBar()
    }

    private fun observeConnectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            connectReceiver.isConnectStatusFlow.collect { isConnect ->
                if(isConnect) {
                    if(showMessageInternetOk) {
                        if (connectReceiver.getShowTextOk()) {
                            connectReceiver.setShowTextOk()
                            Toast.makeText(context, R.string.text_internet_ok, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    if(connectReceiver.getShowTextLost()) {
                        connectReceiver.setShowTextLost()
                        Toast.makeText(context, R.string.text_no_internet, Toast.LENGTH_LONG).show()
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
        viewModel.dateChangedStrategy = sPref.getBoolean("dateChanged",
            DATE_CHANGE_WHEN_CONTENT
        )
        showMessageInternetOk = sPref.getBoolean("showMessageInternetOk",
            SHOW_MESSAGE_INTERNET_OK
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
                menu.findItem(R.id.settings).isVisible = false
                menu.findItem(R.id.delete).isVisible = args.idNote != 0L
                definitionOfChange()
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
                        deleteNoteRequest()
                    }
                    R.id.save -> {
                        saveNote()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun deleteNoteRequest() {
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
        hideKeyboardFromView(requireContext(), requireView())
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

    private fun hideKeyboardFromView(context: Context, view: View) {
        val manager: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

     override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
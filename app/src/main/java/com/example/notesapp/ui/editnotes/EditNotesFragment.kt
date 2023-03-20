package com.example.notesapp.ui.editnotes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentEditNotesBinding
import com.example.notesapp.databinding.FragmentSettingsBinding
import com.example.notesapp.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditNotesFragment : Fragment() {

    private var _binding: FragmentEditNotesBinding? = null
    private val binding get() = _binding!!

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
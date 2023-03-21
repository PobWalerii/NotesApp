package com.example.notesapp.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DEFAULT_ADD_IF_CLICK
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.constants.KeyConstants.DELETE_IF_SWIPED
import com.example.notesapp.databinding.FragmentSettingsBinding
import com.example.notesapp.ui.main.MainActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SettingsViewModel>()

    lateinit var appbarMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        loadSettings()
        setListenersSettingsChanged()
    }

    private fun setupActionBar() {
        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.title = getString(R.string.app_name) + ". " + getString(R.string.settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        //requireActivity().onBackPressedDispatcher.addCallback(this) {
        //    (activity as MainActivity).onSupportNavigateUp()
        //}
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                appbarMenu = menu
                menu.findItem(R.id.save).isVisible = false
                menu.findItem(R.id.settings).isVisible = false
                menu.findItem(R.id.delete).isVisible = false
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.appbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    android.R.id.home -> {
                        (activity as MainActivity).onSupportNavigateUp()
                    }
                    R.id.save -> {
                        saveSettings()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        sPref.getString("defaultHeader", DEFAULT_HEADER).toString().apply {
            binding.defaultHeader = this
            viewModel.defaultHeader = this
        }
        sPref.getBoolean("specificationLine", DEFAULT_SPECIFICATION_LINE).apply {
            binding.specificationLine = this
            viewModel.specificationLine = this
        }
        sPref.getBoolean("defaultAddIfClick", DEFAULT_ADD_IF_CLICK).apply {
            binding.defaultAddIfClick = this
            viewModel.defaultAddIfClick = this
        }
        sPref.getBoolean("deleteIfSwiped", DELETE_IF_SWIPED).apply {
            binding.deleteIfSwiped = this
            viewModel.deleteIfSwiped = this
        }

    }

    private fun setListenersSettingsChanged() {
        binding.header.addTextChangedListener {
            definitionOfChange()
        }
        binding.switch1.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
        }
        binding.switch2.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
        }
        binding.switch3.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
        }
    }

    private fun definitionOfChange() {
        appbarMenu.findItem(R.id.save).isVisible =
            viewModel.defaultHeader != binding.header.text.toString() ||
            viewModel.specificationLine != binding.switch1.isChecked ||
            viewModel.defaultAddIfClick != binding.switch2.isChecked ||
            viewModel.deleteIfSwiped != binding.switch3.isChecked
    }

    private fun saveSettings() {
        val sPref = requireActivity().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putString("defaultHeader", binding.header.text.toString())
        ed.putBoolean("specificationLine", binding.switch1.isChecked)
        ed.putBoolean("defaultAddIfClick", binding.switch2.isChecked)
        ed.putBoolean("deleteIfSwiped", binding.switch3.isChecked)
        ed.apply()
        Toast.makeText(context,getString(R.string.settings_is_saved),Toast.LENGTH_SHORT).show()
        appbarMenu.findItem(R.id.save).isVisible = false
        loadSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
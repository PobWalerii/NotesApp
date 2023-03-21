package com.example.notesapp.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                definitionOfChange()
            }
        }
        binding.switch2.addTextChangedListener{
            definitionOfChange()
        }
        binding.switch3.addTextChangedListener{
            definitionOfChange()
        }
    }

    private fun definitionOfChange() {
        //binding.buttonSave.visibility = if(
        val isEdited =
            viewModel.defaultHeader != binding.header.text.toString() ||
                    viewModel.specificationLine != binding.switch1.isChecked ||
                    viewModel.defaultAddIfClick != binding.switch2.isChecked ||
                    viewModel.deleteIfSwiped != binding.switch3.isChecked
Toast.makeText(context,"$isEdited",Toast.LENGTH_LONG).show()
        //) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
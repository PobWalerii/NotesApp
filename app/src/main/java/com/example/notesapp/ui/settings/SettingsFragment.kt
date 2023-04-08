package com.example.notesapp.ui.settings

import android.os.Bundle
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentSettingsBinding
import com.example.notesapp.ui.MyActionBar.MyActionBar
import com.example.notesapp.utils.AppActionBar
import com.example.notesapp.utils.ConfirmationDialog.showConfirmationDialog
import com.example.notesapp.utils.HideKeyboard.hideKeyboardFromView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SettingsViewModel>()

    @Inject
    lateinit var actionBar: MyActionBar

    //private lateinit var actionBar: AppActionBar

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
        observeSettingsLoaded()
        setListenersSettingsChanged()
    }

    private fun observeSettingsLoaded() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadedPreferences.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    if (it) {
                        showSettings()
                    }
                }
            }
        }
    }

    private fun setupActionBar() {

        actionBar.init(
            requireActivity(),
            R.string.settings,
            viewLifecycleOwner,
            toDefault = true)


/*
        actionBar = AppActionBar(
            requireActivity(),
            requireContext(),
            R.string.settings,
            viewLifecycleOwner,
            toDefault = true
        )

 */

        viewLifecycleOwner.lifecycleScope.launch {
            actionBar.isItemMenuPressedFlow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    hideKeyboardFromView(requireActivity(), requireView())
                    if (it == "save") {
                        saveSettings()
                    } else if (it == "todefault") {
                        setSettingsToDefault()
                    }
                }
            }
        }
    }

    private fun setSettingsToDefault() {
        showConfirmationDialog(
            R.string.restoring_settings,
            R.string.text_restoring,
            requireContext(),
            onConfirmed = {
                viewModel.setDefaultPreferences()
            },
            onCancelled = { }
        )
    }
    private fun showSettings() {
        binding.firstRun = viewModel.firstRun.value
        binding.defaultHeader = viewModel.defaultHeader.value
        binding.specificationLine = viewModel.specificationLine.value
        binding.defaultAddIfClick = viewModel.defaultAddIfClick.value
        binding.deleteIfSwiped = viewModel.deleteIfSwiped.value
        binding.dateChanged = viewModel.dateChanged.value
        binding.showMessageInternetOk = viewModel.showMessageInternetOk.value
        binding.startDelayValue = viewModel.startDelayValue.value
        binding.queryDelayValue = viewModel.queryDelayValue.value
        binding.requestIntervalValue = viewModel.requestIntervalValue.value
        binding.operationDelayValue = viewModel.operationDelayValue.value
    }

    private fun setListenersSettingsChanged() {
        binding.switch6.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.header.addTextChangedListener {
            definitionOfChange()
        }
        binding.switch1.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.switch2.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.switch3.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.switch4.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.switch5.setOnCheckedChangeListener { _, _ ->
            definitionOfChange()
            hideKeyboardFromView(requireActivity(), requireView())
        }
        binding.startDelay.addTextChangedListener {
            definitionOfChange()
        }
        binding.queryDelay.addTextChangedListener {
            definitionOfChange()
        }
        binding.requestInterval.addTextChangedListener {
            definitionOfChange()
        }
        binding.operationDelay.addTextChangedListener {
            definitionOfChange()
        }
    }

    private fun definitionOfChange() {
        CoroutineScope(Dispatchers.Main).launch {
            actionBar.setButtonVisible("save", getIsChange())
        }
    }

    private fun getIsChange(): Boolean =
        viewModel.isChange(
            binding.switch6.isChecked,
            binding.header.text.toString(),
            binding.switch1.isChecked,
            binding.switch2.isChecked,
            binding.switch3.isChecked,
            binding.switch4.isChecked,
            binding.switch5.isChecked,
            binding.startDelay.text.toString(),
            binding.queryDelay.text.toString(),
            binding.requestInterval.text.toString(),
            binding.operationDelay.text.toString()
        )

    private fun saveSettings() {
        viewModel.savePreferences(
            binding.switch6.isChecked,
            binding.header.text.toString(),
            binding.switch1.isChecked,
            binding.switch2.isChecked,
            binding.switch3.isChecked,
            binding.switch4.isChecked,
            binding.switch5.isChecked,
            startDelayValue = binding.startDelay.text.toString().toIntOrNull() ?: 0,
            queryDelayValue = binding.queryDelay.text.toString().toIntOrNull() ?: 0,
            requestIntervalValue = binding.requestInterval.text.toString().toIntOrNull()
                ?: 0,
            operationDelayValue = binding.operationDelay.text.toString().toIntOrNull()
                ?: 0
        )
        definitionOfChange()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromView(requireContext(),requireView())
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            definitionOfChange()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
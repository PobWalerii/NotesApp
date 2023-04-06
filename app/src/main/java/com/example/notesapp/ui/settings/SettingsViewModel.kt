package com.example.notesapp.ui.settings

import androidx.lifecycle.ViewModel
import com.example.notesapp.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings
): ViewModel() {

    val firstRun: StateFlow<Boolean> = appSettings.firstRun
    val defaultHeader: StateFlow<String> = appSettings.defaultHeader
    val specificationLine: StateFlow<Boolean> = appSettings.specificationLine
    val defaultAddIfClick: StateFlow<Boolean> = appSettings.defaultAddIfClick
    val deleteIfSwiped: StateFlow<Boolean> = appSettings.deleteIfSwiped
    val dateChanged: StateFlow<Boolean> = appSettings.dateChanged
    val showMessageInternetOk: StateFlow<Boolean> = appSettings.showMessageInternetOk
    val startDelayValue: StateFlow<Int> = appSettings.startDelayValue
    val queryDelayValue: StateFlow<Int> = appSettings.queryDelayValue
    val requestIntervalValue: StateFlow<Int> = appSettings.requestIntervalValue
    val operationDelayValue: StateFlow<Int> = appSettings.operationDelayValue

    var isLoadedPreferences: StateFlow<Boolean> = appSettings.isLoadedPreferences

    fun setDefaultPreferences() {
        appSettings.setDefaultPreferences()
    }

    fun savePreferences(
        firstRun: Boolean,
        defaultHeader: String,
        specificationLine: Boolean,
        defaultAddIfClick: Boolean,
        deleteIfSwiped: Boolean,
        dateChanged: Boolean,
        showMessageInternetOk: Boolean,
        startDelayValue: Int,
        queryDelayValue: Int,
        requestIntervalValue: Int,
        operationDelayValue: Int
    ) {
        appSettings.savePreferences(
            firstRun,
            defaultHeader,
            specificationLine,
            defaultAddIfClick,
            deleteIfSwiped,
            dateChanged,
            showMessageInternetOk,
            startDelayValue,
            queryDelayValue,
            requestIntervalValue,
            operationDelayValue
        )
    }

    fun isChange(
        _firstRun: Boolean,
        _defaultHeader: String,
        _specificationLine: Boolean,
        _defaultAddIfClick: Boolean,
        _deleteIfSwiped: Boolean,
        _dateChanged: Boolean,
        _showMessageInternetOk: Boolean,
        _startDelayValue: String,
        _queryDelayValue: String,
        _requestIntervalValue: String,
        _operationDelayValue: String,
    ): Boolean {
        return  firstRun.value != _firstRun ||
                defaultHeader.value != _defaultHeader ||
                specificationLine.value != _specificationLine ||
                defaultAddIfClick.value != _defaultAddIfClick ||
                deleteIfSwiped.value != _deleteIfSwiped ||
                dateChanged.value != _dateChanged ||
                showMessageInternetOk.value != _showMessageInternetOk ||
                startDelayValue.value.toString() != _startDelayValue ||
                queryDelayValue.value.toString() != _queryDelayValue ||
                requestIntervalValue.value.toString() != _requestIntervalValue ||
                operationDelayValue.value.toString() != _operationDelayValue
    }



}
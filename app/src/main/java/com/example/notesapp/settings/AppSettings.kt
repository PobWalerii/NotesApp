package com.example.notesapp.settings

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.DATE_CHANGE_WHEN_CONTENT
import com.example.notesapp.constants.KeyConstants.DEFAULT_ADD_IF_CLICK
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.constants.KeyConstants.DELETE_IF_SWIPED
import com.example.notesapp.constants.KeyConstants.INTERVAL_REQUESTS
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.constants.KeyConstants.SING_OF_FIRST_RUN
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_OPERATION
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_QUERY
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_START
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppSettings(
    private val context: Context
) {
    private val _firstLoad = MutableStateFlow(false)
    val firstLoad: StateFlow<Boolean> = _firstLoad.asStateFlow()

    private val _firstRun = MutableStateFlow(SING_OF_FIRST_RUN)
    val firstRun: StateFlow<Boolean> = _firstRun.asStateFlow()

    private val _defaultHeader = MutableStateFlow(DEFAULT_HEADER)
    val defaultHeader: StateFlow<String> = _defaultHeader.asStateFlow()

    private val _specificationLine = MutableStateFlow(DEFAULT_SPECIFICATION_LINE)
    val specificationLine: StateFlow<Boolean> = _specificationLine.asStateFlow()

    private val _defaultAddIfClick = MutableStateFlow(DEFAULT_ADD_IF_CLICK)
    val defaultAddIfClick: StateFlow<Boolean> = _defaultAddIfClick.asStateFlow()

    private val _deleteIfSwiped = MutableStateFlow(DELETE_IF_SWIPED)
    val deleteIfSwiped: StateFlow<Boolean> = _deleteIfSwiped.asStateFlow()

    private val _dateChanged = MutableStateFlow(DATE_CHANGE_WHEN_CONTENT)
    val dateChanged: StateFlow<Boolean> = _dateChanged.asStateFlow()

    private val _showMessageInternetOk = MutableStateFlow(SHOW_MESSAGE_INTERNET_OK)
    val showMessageInternetOk: StateFlow<Boolean> = _showMessageInternetOk.asStateFlow()

    private val _startDelayValue = MutableStateFlow(TIME_DELAY_START)
    val startDelayValue: StateFlow<Int> = _startDelayValue.asStateFlow()

    private val _queryDelayValue = MutableStateFlow(TIME_DELAY_QUERY)
    val queryDelayValue: StateFlow<Int> = _queryDelayValue.asStateFlow()

    private val _requestIntervalValue = MutableStateFlow(INTERVAL_REQUESTS)
    val requestIntervalValue: StateFlow<Int> = _requestIntervalValue.asStateFlow()

    private val _operationDelayValue = MutableStateFlow(TIME_DELAY_OPERATION)
    val operationDelayValue: StateFlow<Int> = _operationDelayValue.asStateFlow()

    private var sPref: SharedPreferences = context.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)

    private val _isLoadedPreferences = MutableStateFlow(false)
    val isLoadedPreferences: StateFlow<Boolean> = _isLoadedPreferences.asStateFlow()

    init {
        getPreferences()
    }

    fun setFromAppFirstRun() {
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putBoolean("firstRun", false)
        ed.apply()
        _firstRun.value = false
    }

    fun setFirstLoad() {
        _firstLoad.value = true
    }

    private fun getPreferences() {
        CoroutineScope(Dispatchers.Main).launch {
            _isLoadedPreferences.value = false
            _firstRun.value = sPref.getBoolean("firstRun", SING_OF_FIRST_RUN)
            _defaultHeader.value = sPref.getString("defaultHeader", DEFAULT_HEADER).toString()
            _specificationLine.value =
                sPref.getBoolean("specificationLine", DEFAULT_SPECIFICATION_LINE)
            _defaultAddIfClick.value = sPref.getBoolean("defaultAddIfClick", DEFAULT_ADD_IF_CLICK)
            _deleteIfSwiped.value = sPref.getBoolean("deleteIfSwiped", DELETE_IF_SWIPED)
            _dateChanged.value = sPref.getBoolean("dateChanged", DATE_CHANGE_WHEN_CONTENT)
            _showMessageInternetOk.value =
                sPref.getBoolean("showMessageInternetOk", SHOW_MESSAGE_INTERNET_OK)
            _startDelayValue.value = sPref.getInt("startDelayValue", TIME_DELAY_START)
            _queryDelayValue.value = sPref.getInt("queryDelayValue", TIME_DELAY_QUERY)
            _requestIntervalValue.value = sPref.getInt("requestIntervalValue", INTERVAL_REQUESTS)
            _operationDelayValue.value = sPref.getInt("operationDelayValue", TIME_DELAY_OPERATION)
            _isLoadedPreferences.value = true
        }
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
        operationDelayValue: Int,
        getDefault: Boolean = false
    ) {
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putBoolean("firstRun", firstRun)
        ed.putString("defaultHeader", defaultHeader.ifEmpty { DEFAULT_HEADER })
        ed.putBoolean("specificationLine", specificationLine)
        ed.putBoolean("defaultAddIfClick", defaultAddIfClick)
        ed.putBoolean("deleteIfSwiped", deleteIfSwiped)
        ed.putBoolean("dateChanged", dateChanged)
        ed.putBoolean("showMessageInternetOk", showMessageInternetOk)
        ed.putInt("startDelayValue", startDelayValue)
        ed.putInt("queryDelayValue", queryDelayValue)
        ed.putInt("requestIntervalValue", if(requestIntervalValue>0) requestIntervalValue else INTERVAL_REQUESTS)
        ed.putInt("operationDelayValue", operationDelayValue)
        ed.apply()
        Toast.makeText(
            context, context.getString(
                if (getDefault) R.string.settings_to_default else R.string.settings_is_saved
            ), Toast.LENGTH_SHORT
        ).show()
        getPreferences()
    }

    fun setDefaultPreferences() {
        savePreferences(
            SING_OF_FIRST_RUN,
            DEFAULT_HEADER,
            DEFAULT_SPECIFICATION_LINE,
            DEFAULT_ADD_IF_CLICK,
            DELETE_IF_SWIPED,
            DATE_CHANGE_WHEN_CONTENT,
            SHOW_MESSAGE_INTERNET_OK,
            TIME_DELAY_START,
            TIME_DELAY_QUERY,
            INTERVAL_REQUESTS,
            TIME_DELAY_OPERATION,
            getDefault = true
        )
    }


}
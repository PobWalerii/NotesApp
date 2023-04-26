package com.example.notesapp.settings

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.R
import com.example.notesapp.constants.KeyConstants.CREATE_RECORDS_IN_BACKGROUND
import com.example.notesapp.constants.KeyConstants.DATE_CHANGE_WHEN_CONTENT
import com.example.notesapp.constants.KeyConstants.DEFAULT_ADD_IF_CLICK
import com.example.notesapp.constants.KeyConstants.DEFAULT_HEADER
import com.example.notesapp.constants.KeyConstants.DEFAULT_SPECIFICATION_LINE
import com.example.notesapp.constants.KeyConstants.DELETE_IF_SWIPED
import com.example.notesapp.constants.KeyConstants.INTERVAL_BACKGROUND_CREATE
import com.example.notesapp.constants.KeyConstants.INTERVAL_REQUESTS
import com.example.notesapp.constants.KeyConstants.MIN_INTERVAL_BACKGROUND_CREATE
import com.example.notesapp.constants.KeyConstants.SHOW_MESSAGE_INTERNET_OK
import com.example.notesapp.constants.KeyConstants.SING_OF_FIRST_RUN
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_OPERATION
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_QUERY
import com.example.notesapp.constants.KeyConstants.TIME_DELAY_START
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class AppSettings(
    private val applicationContext: Context
) {
    private val _firstLoad = MutableStateFlow(true)
    val firstLoad: StateFlow<Boolean> = _firstLoad.asStateFlow()

    private val _isBackService = MutableStateFlow(false)
    val isBackService: StateFlow<Boolean> = _isBackService.asStateFlow()

    private val _isRemoteService = MutableStateFlow(false)
    val isRemoteService: StateFlow<Boolean> = _isRemoteService.asStateFlow()

    private val _isConnectStatus = MutableStateFlow(true)
    val isConnectStatus: StateFlow<Boolean> = _isConnectStatus.asStateFlow()

    private val _isDateChanged = MutableStateFlow(false)
    val isDateChanged: StateFlow<Boolean> = _isDateChanged.asStateFlow()


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

    private val _createBackgroundRecords = MutableStateFlow(CREATE_RECORDS_IN_BACKGROUND)
    val createBackgroundRecords: StateFlow<Boolean> = _createBackgroundRecords.asStateFlow()

    private val _intervalCreateRecords = MutableStateFlow(INTERVAL_BACKGROUND_CREATE)
    val intervalCreateRecords: StateFlow<Int> = _intervalCreateRecords.asStateFlow()

    private var sPref: SharedPreferences = applicationContext.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)

    private val _isLoadedPreferences = MutableStateFlow(false)
    val isLoadedPreferences: StateFlow<Boolean> = _isLoadedPreferences.asStateFlow()

    var showViewForSnack: View? = null

    fun init() {
        getPreferences()
        setIsBackService(false)
        setIsRemoteService(false)
    }

    fun close() {
        setFirstLoad(true)
    }

    fun setAppFirstRun() {
        val ed: SharedPreferences.Editor = sPref.edit()
        ed.putBoolean("firstRun", false)
        ed.apply()
        _firstRun.value = false
    }

    fun setFirstLoad(isStart: Boolean = false) {
        _firstLoad.value = isStart
    }

    fun setIsBackService(state: Boolean) {
        _isBackService.value = state
    }

    fun setIsRemoteService(state: Boolean) {
        _isRemoteService.value = state
    }

    fun setIsConnectStatus(state: Boolean) {
        _isConnectStatus.value = state
    }

    fun setIsDateChanged(state: Boolean) {
        _isDateChanged.value = state
    }

    private fun getPreferences() {
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
        _createBackgroundRecords.value =
            sPref.getBoolean("createBackgroundRecords", CREATE_RECORDS_IN_BACKGROUND)
        _intervalCreateRecords.value =
            sPref.getInt("intervalCreateRecords", INTERVAL_BACKGROUND_CREATE)

        _isLoadedPreferences.value = true
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
        createBackgroundRecords: Boolean,
        intervalCreateRecords: Int,
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
        ed.putBoolean("createBackgroundRecords", createBackgroundRecords)
        ed.putInt("intervalCreateRecords", if(intervalCreateRecords>=MIN_INTERVAL_BACKGROUND_CREATE) intervalCreateRecords else MIN_INTERVAL_BACKGROUND_CREATE)
        ed.apply()
        Toast.makeText(
            applicationContext, applicationContext.getString(
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
            CREATE_RECORDS_IN_BACKGROUND,
            INTERVAL_BACKGROUND_CREATE,
            getDefault = true
        )
    }


}
package com.example.notesapp.ui.settings

import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {
    var defaultHeader: String = ""
    var specificationLine: Boolean = true
    var defaultAddIfClick: Boolean = true
    var deleteIfSwiped: Boolean = true
    var dateChanged: Boolean = true
    var showMessageInternetOk: Boolean = false
    var startDelayValue: Int = 0
    var queryDelayValue: Int = 0
    var requestIntervalValue: Int = 0
}
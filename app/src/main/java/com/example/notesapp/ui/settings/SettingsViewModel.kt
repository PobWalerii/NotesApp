package com.example.notesapp.ui.settings

import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {
    var defaultHeader: String = ""
    var specificationLine: Boolean = true
    var defaultAddIfClick: Boolean = true
    var deleteIfSwiped: Boolean = true
    var dateChanget: Boolean = true
    var showMessageInternetOk: Boolean = false
}
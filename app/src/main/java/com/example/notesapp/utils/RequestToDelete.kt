package com.example.notesapp.utils

import android.content.Context
import com.example.notesapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RequestToDelete {
    companion object {

        private val isRequestToDeleteOk = MutableStateFlow(0)
        val isRequestToDeleteOkFlow: StateFlow<Int> = isRequestToDeleteOk.asStateFlow()

        fun requestToDelete(context: Context, position: Int) {
            isRequestToDeleteOk.value = 0
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_delete)
                .setIcon(R.drawable.warning)
                .setMessage(R.string.text_delete)
                .setPositiveButton(R.string.but_yes_txt) { _, _ ->
                    isRequestToDeleteOk.value = position
                }
                .setNegativeButton(R.string.but_no_txt) { _, _ ->
                    isRequestToDeleteOk.value = -position
                }
                .create()
            dialog.show()
        }
    }


}
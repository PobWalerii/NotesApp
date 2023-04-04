package com.example.notesapp.utils

import android.content.Context
import android.widget.Toast
import com.example.notesapp.R

object MessageNotPossible {

    fun showMessageNotPossible(context: Context) {
        Toast.makeText(context, R.string.operation_not_possible, Toast.LENGTH_LONG).show()
    }
}
package com.example.notesapp.utils

import android.app.AlertDialog
import android.content.Context
import com.example.notesapp.R

object ConfirmationDialog {

    fun showConfirmationDialog(
        title: Int,
        message: Int,
        context: Context,
        onConfirmed: () -> Unit,
        onCancelled: () -> Unit
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.warning)
            .setPositiveButton(R.string.but_yes_txt) { _, _ ->
                onConfirmed()
            }
            .setNegativeButton(R.string.but_no_txt) { _, _ ->
                onCancelled()
            }
        val dialog = builder.create()
        dialog.setOnCancelListener {
            onCancelled()
        }
        dialog.show()
        return dialog
    }




}
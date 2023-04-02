package com.example.notesapp.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.example.notesapp.R

object ConfirmationDeleteDialog {

    fun showConfirmationDeleteDialog(
        context: Context,
        onConfirmed: () -> Unit,
        onCancelled: () -> Unit
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.title_delete)
            .setMessage(R.string.text_delete)
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

    fun showMessageNotPossible(context: Context) {
        Toast.makeText(context,R.string.operation_not_possible, Toast.LENGTH_LONG).show()
    }


}
package com.example.notesapp.utils

import android.content.Context
import com.example.notesapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object RequestToDelete {
    fun requestToDelete(context: Context): Boolean {
        var result = false
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_delete)
            .setIcon(R.drawable.warning)
            .setMessage(R.string.text_delete)
            .setPositiveButton(R.string.but_yes_txt) { dialog, which ->
                dialog.dismiss()
                result = true
            }
            .setNegativeButton(R.string.but_no_txt) { dialog, which ->
                dialog.dismiss()
                result = false
            }
        val dialog = builder.create()
        dialog.show()
        return result
    }
}
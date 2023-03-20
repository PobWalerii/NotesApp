package com.example.notesapp.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

object NotesBindingAdapter {

    @JvmStatic
    @BindingAdapter("dateToString")
    fun dateToString(textView: TextView, dateNote: Long) {
        var sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        if(sdf.format(dateNote) == sdf.format(Date().time)) sdf = SimpleDateFormat("kk.mm", Locale.getDefault())
        textView.text = sdf.format(dateNote).toString()
    }

}
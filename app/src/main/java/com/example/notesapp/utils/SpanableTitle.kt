package com.example.notesapp.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.example.notesapp.R

object SpanableTitle {

    fun setSpanableTitle(
        actionBar: ActionBar?,
        context: Context,
        title: String,
        text: String
    ) {
        if(actionBar == null) return
        val spannableString = SpannableString(title+text)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
            title.length,
            title.length+text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        actionBar.title = spannableString
    }

}
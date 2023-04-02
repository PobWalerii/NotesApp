package com.example.notesapp.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.notesapp.R
import com.google.android.material.snackbar.Snackbar

class ShowConnectStatus(
    private val context: Context
) {

    private var lastConnectionStatus = true
    private var showMessageInternetOk = true

    fun setShowMessageInternetOk(isShow: Boolean) {
        showMessageInternetOk = isShow
    }

    fun showStatus(
        isConnect: Boolean,
        view: View? = null,
        isStartApp: Boolean = false,
    ) {
        if ((isConnect && showMessageInternetOk && !lastConnectionStatus) ||
            (!isConnect && (lastConnectionStatus || (!isStartApp && view != null)))
        ) {
            if (!isConnect && !isStartApp && view != null) {
                showSnack(view)
            } else {
                Toast.makeText(
                    context,
                    if (isConnect) R.string.text_internet_ok else R.string.text_no_internet,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        lastConnectionStatus = isConnect
    }

    fun showSnack(view: View) {
        val snack = Snackbar.make(
            view,
            R.string.text_no_internet,
            Snackbar.LENGTH_LONG
        )
        val snackView = snack.view
        val textView: TextView =
            snackView.findViewById(com.google.android.material.R.id.snackbar_text)
        textView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.warning,
            0,
            0,
            0
        )
        textView.compoundDrawablePadding = view.resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
        snack.show()
    }
}
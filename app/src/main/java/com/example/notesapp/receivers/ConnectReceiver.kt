package com.example.notesapp.receivers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.notesapp.R
import com.example.notesapp.settings.AppSettings
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class ConnectReceiver(
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private val connectivityManager: ConnectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            setConnectStatus(true)
        }
        override fun onLost(network: Network) {
            setConnectStatus(false)
        }
    }

    fun init() {
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
        setConnectStatus(connectivityManager.activeNetwork != null)
        Toast.makeText(applicationContext,"ConnectReceiver init ok", Toast.LENGTH_SHORT).show()
    }
    fun close() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        Toast.makeText(applicationContext,"ConnectReceiver close ok", Toast.LENGTH_SHORT).show()
    }

    private fun setConnectStatus(status: Boolean) {
        appSettings.setIsConnectStatus(status)
        showStatus(status)
    }

    private fun showStatus(
        isConnect: Boolean,
    ) {
        if (isConnect) {
            if (appSettings.showMessageInternetOk.value && !appSettings.firstLoad.value) {
                Toast.makeText(applicationContext, R.string.text_internet_ok, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            val showView: View? = appSettings.showView
            if (!appSettings.firstLoad.value && showView != null) {
                showSnack(showView)
            } else {
                Toast.makeText(applicationContext, R.string.text_no_internet, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun showSnack(showView: View) {
        val snack = Snackbar.make(
            showView,
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
        textView.compoundDrawablePadding =
            showView.resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
        snack.show()
    }

}

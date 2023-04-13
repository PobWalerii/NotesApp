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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class ConnectReceiver(
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {

    private val connectivityManager: ConnectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val isConnectStatus = MutableStateFlow(connectivityManager.activeNetwork != null)
    val isConnectStatusFlow: StateFlow<Boolean> = isConnectStatus.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnectStatus.value = true
        }
        override fun onLost(network: Network) {
            isConnectStatus.value = false
        }
    }

    fun init() {
        coroutineScope.launch {
            observeStatusConnect()
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback
            )
        }
    }

    fun closeObserve() {
        coroutineScope.cancel()
    }

    private fun observeStatusConnect() {
        coroutineScope.launch {
            isConnectStatusFlow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    showStatus(it)
                }
            }
        }
    }

    private fun showStatus(
        isConnect: Boolean,
    ) {
        if (isConnect) {
            if (appSettings.showMessageInternetOk.value && !appSettings.firstLoad.value) {
                Toast.makeText(applicationContext, R.string.text_internet_ok, Toast.LENGTH_LONG)
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

package com.example.notesapp.receivers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.notesapp.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class ConnectReceiver(
    private val applicationContext: Context
) {

    private val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val isConnectStatus = MutableStateFlow(true)
    val isConnectStatusFlow: StateFlow<Boolean> = isConnectStatus.asStateFlow()

    init {
        connectivityManager.addDefaultNetworkActiveListener {
            changeNetwork()
        }
        initReceiver()
        observeStatusConnect()
    }

    private fun initReceiver() {
        setStatus(connectivityManager.activeNetwork != null)
    }

    private fun changeNetwork() {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                setStatus(true)
            }
            override fun onLost(network: Network) {
                setStatus(false)
            }
        })
    }

    private fun setStatus(status: Boolean) {
        isConnectStatus.value = status
    }

    private var lastConnectionStatus = true
    private var showMessageInternetOk = true
    private var showView: View? = null
    private val isStartApp: Boolean = false

    //fun setShowMessageInternetOk(isShow: Boolean) {
    //    showMessageInternetOk = isShow
    //}

    private fun observeStatusConnect() {
        CoroutineScope(Dispatchers.Default).launch {
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
        if ((isConnect && showMessageInternetOk && !lastConnectionStatus) ||
            (!isConnect && (lastConnectionStatus || (!isStartApp && showView != null)))
        ) {
            if (!isConnect && !isStartApp && showView != null) {
                showSnack(showView)
            } else {
                Toast.makeText(
                    applicationContext,
                    if (isConnect) R.string.text_internet_ok else R.string.text_no_internet,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        lastConnectionStatus = isConnect
    }

    private fun showSnack(view: View?) {
        if(view == null) return
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

package com.example.notesapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectReceiver(
    applicationContext: Context
) {

    private var connectivityManager: ConnectivityManager

    private val isConnectStatus = MutableStateFlow(false)
    val isConnectStatusFlow: StateFlow<Boolean> = isConnectStatus.asStateFlow()

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnectStatus.value = true
        }
        override fun onLost(network: Network) {
            isConnectStatus.value = false
        }
    }

    init {
        connectivityManager =
            applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
        connectivityManager.addDefaultNetworkActiveListener {
            changeNetwork()
        }
        isConnectStatus.value =connectivityManager.activeNetwork != null
    }

    private fun changeNetwork() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

}
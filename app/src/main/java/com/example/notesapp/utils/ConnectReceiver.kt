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

    private val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val isConnectStatus = MutableStateFlow(false)
    val isConnectStatusFlow: StateFlow<Boolean> = isConnectStatus.asStateFlow()

    init {
        isConnectStatus.value =connectivityManager.activeNetwork != null
        
        connectivityManager.addDefaultNetworkActiveListener {
            changeNetwork()
        }
    }

    private fun changeNetwork() {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnectStatus.value = true
            }
            override fun onLost(network: Network) {
                isConnectStatus.value = false
            }
        })
    }

}
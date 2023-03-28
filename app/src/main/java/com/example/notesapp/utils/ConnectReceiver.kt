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

    private var showTextOk = false
    private var showTextLost = true

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
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnectStatus.value = true
            }
            override fun onLost(network: Network) {
                isConnectStatus.value = false
            }
        })
        showTextOk = true
        showTextLost = true
    }

    fun getShowTextOk(): Boolean = showTextOk
    fun setShowTextOk() {
        showTextOk = false
    }
    fun getShowTextLost(): Boolean = showTextLost
    fun setShowTextLost() {
        showTextLost = false
    }


}
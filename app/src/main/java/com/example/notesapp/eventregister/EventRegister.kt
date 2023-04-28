package com.example.notesapp.eventregister

import android.content.Context
import com.appsflyer.AppsFlyerLib
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRegister @Inject constructor(
    private val context: Context,
) {

    fun registerEvent(event: String, itemId: Long) {
        val eventValues: MutableMap<String, Any> = HashMap()
        eventValues["item_id"] = itemId
        AppsFlyerLib.getInstance().logEvent(context, event, eventValues)
    }

}
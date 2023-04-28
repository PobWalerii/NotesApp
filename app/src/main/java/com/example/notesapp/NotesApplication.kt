package com.example.notesapp

import android.app.Application
import com.appsflyer.AppsFlyerLib
import com.example.notesapp.constants.KeyConstants.AF_DEV_KEY
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //AppsFlyerLib.getInstance().setDebugLog(true)
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, null, this)
        AppsFlyerLib.getInstance().start(this)

    }

}


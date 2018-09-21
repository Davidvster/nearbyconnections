package com.nearby.messages.nearbyconnection

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nearby.messages.nearbyconnection.arch.AppModule

open class BaseApp : Application() {
    override fun onCreate() {
        AppModule.application = this
        super.onCreate()

        if (BuildConfig.DEBUG) {
        }
        AndroidThreeTen.init(this)
    }

}
package com.nearby.messages.nearbyconnection.arch

import com.nearby.messages.nearbyconnection.data.managers.RecognizeRequestManagerImpl

object DataModule {

    val textRequestManager by lazy {
        RecognizeRequestManagerImpl()
    }

}
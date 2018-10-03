package com.nearby.messages.nearbyconnection.arch

import com.nearby.messages.nearbyconnection.data.managers.TextRequestManagerImpl
import com.nearby.messages.nearbyconnection.data.managers.contract.TextRequestManager

object DataModule {

    val textRequestManager by lazy {
        TextRequestManagerImpl()
    }

}
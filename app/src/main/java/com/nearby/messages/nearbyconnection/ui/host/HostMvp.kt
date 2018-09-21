package com.nearby.messages.nearbyconnection.ui.host

import com.nearby.messages.nearbyconnection.arch.BaseMvp

interface HostMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init()
        fun startAdvertising(username: String, packageName: String)
        fun stopAdvertising()
        fun addMessage(message: Pair<Pair<String, String>, Int>)
        fun stopAllConnections()
    }
}
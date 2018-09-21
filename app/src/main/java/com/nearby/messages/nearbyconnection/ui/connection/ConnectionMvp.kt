package com.nearby.messages.nearbyconnection.ui.connection

import android.content.Context
import com.nearby.messages.nearbyconnection.arch.BaseMvp

interface ConnectionMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>)
        fun setDisconnected()
        fun setConnected()
        fun showConnectionDialog(user: String, endpointId: String)
        fun setChattiningTitle(guestNames: HashMap<String, String>)
        fun showAvaibleDevicesDialog(avaibleGuests: HashMap<String, String>)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init()
        fun stopDiscovery()
        fun stopAdvertising()
        fun startDiscovery(packageName: String)
        fun startAdvertising(username: String, packageName: String)
        fun sendMessage(message: String)
        fun addMessage(message: Pair<Pair<String, String>, Int>)
        fun stopAllConnections()
        fun acceptConnection(user: String, endpointId: String)
        fun getAvaibleGuests(): HashMap<String, String>
        fun requestConnection(endpointId: String)
    }
}
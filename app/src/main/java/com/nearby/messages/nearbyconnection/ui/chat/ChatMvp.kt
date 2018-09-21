package com.nearby.messages.nearbyconnection.ui.chat

import com.nearby.messages.nearbyconnection.arch.BaseMvp

interface ChatMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setChattiningTitle(guestNames: HashMap<String, String>)
        fun updateConnectionList(availableRooms: MutableList<String>)
        fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init()
        fun addMessage(message: Pair<Pair<String, String>, Int>)
        fun sendMessage(message: String)
        fun stopDiscovery()
        fun startDiscovery(packageName: String)
        fun stopAllConnections()
        fun acceptConnection(user: String, endpointId: String)
        fun getAvaibleGuests(): HashMap<String, String>
        fun requestConnection(endpointId: String)
    }
}
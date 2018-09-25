package com.nearby.messages.nearbyconnection.ui.chat

import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.ChatMessage

interface ChatMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setChattingTitle(guestNames: List<String>)
        fun updateConnectionList(availableRooms: MutableList<Pair<String, String>>)
        fun setMessages(messageList: List<Pair<ChatMessage, Int>>)
        fun setChatRoom()
        fun setConnectionRoom()
        fun setProgressVisible(visible: Boolean)
        fun setToolbarTitle(title: String)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init(username: String, packageName: String, cardColor: Int)
        fun addMessage(message: Pair<ChatMessage, Int>)
        fun sendMessage(message: String)
        fun stopDiscovery()
        fun startDiscovery()
        fun stopAllConnections()
        fun acceptConnection(user: String, endpointId: String)
        fun getAvaibleGuests(): HashMap<String, String>
        fun requestConnection(endpointId: String)
        fun isConnected(): Boolean
    }
}
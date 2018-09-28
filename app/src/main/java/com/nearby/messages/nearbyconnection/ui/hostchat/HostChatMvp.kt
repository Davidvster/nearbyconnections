package com.nearby.messages.nearbyconnection.ui.hostchat

import com.google.android.gms.nearby.connection.Payload
import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.ChatMessage

interface HostChatMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>)
        fun showJoinDialog(user: String, endpointId: String)
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>, position: Int)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init(username: String, packageName: String, cardColor: Int)
        fun startAdvertising()
        fun stopAdvertising()
        fun addMessage(message: Pair<ChatMessage, Int>)
        fun stopAllConnections()
        fun sendMessage(message: String, endpointId: String = "")
        fun sendFile(filePayload: Payload, endpointId: String = "")
        fun acceptConnection(user: String, endpointId: String)
        fun rejectConnection(endpointId: String)
        fun getGuestList(): List<String>
    }
}
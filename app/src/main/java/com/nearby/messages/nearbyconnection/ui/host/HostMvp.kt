package com.nearby.messages.nearbyconnection.ui.host

import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.ChatMessage

interface HostMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setMessages(messageList: List<Pair<ChatMessage, Int>>)
        fun setChattiningTitle(guestNames: HashMap<String, String>)
        fun showConnectionDialog(user: String, endpointId: String)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init(username: String, packageName: String, cardColor: Int)
        fun startAdvertising()
        fun stopAdvertising()
        fun addMessage(message: Pair<ChatMessage, Int>)
        fun stopAllConnections()
        fun sendMessage(message: String, endpointId: String = "")
        fun acceptConnection(user: String, endpointId: String)
        fun rejectConnection(endpointId: String)
    }
}
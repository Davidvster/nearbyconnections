package com.nearby.messages.nearbyconnection.ui.hostchat

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.google.android.gms.nearby.connection.Payload
import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.ChatMessage

interface HostChatMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>)
        fun showJoinDialog(user: String, endpointId: String)
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>, position: Int)
        fun startCameraActivity(takePictureIntent: Intent)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init(username: String, packageName: String, cardColor: Int)
        fun startAdvertising()
        fun stopAdvertising()
        fun addMessage(message: Pair<ChatMessage, Int>)
        fun stopAllConnections()
        fun sendReceivedMessage(message: String, endpointId: String)
        fun sendMessage(message: String)
        fun sendReceivedFile(filePayload: Payload, endpointId: String)
        fun acceptConnection(user: String, endpointId: String)
        fun rejectConnection(endpointId: String)
        fun getGuestList(): List<String>
        fun attachImage(takePictureIntent: Intent, componentName: ComponentName)
        fun sendFile(uri: Uri? = null)
    }
}
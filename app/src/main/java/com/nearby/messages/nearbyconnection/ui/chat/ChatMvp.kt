package com.nearby.messages.nearbyconnection.ui.chat

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.google.android.gms.nearby.connection.Payload
import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.ChatMessage

interface ChatMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun updateConnectionList(availableRooms: List<Pair<String, String>>)
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>)
        fun setChatRoom()
        fun setConnectionRoom()
        fun setProgressVisible(visible: Boolean)
        fun setToolbarTitle(roomTitle: String)
        fun stopRefreshConnectionList()
        fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>, position: Int)
        fun startCameraActivity(takePictureIntent: Intent)
        fun showImageDescriptionDialog(title: String, desc: String)
    }

    interface Presenter : BaseMvp.Presenter {
        fun init(username: String, packageName: String, cardColor: Int)
        fun addMessage(message: Pair<ChatMessage, Int>)
        fun sendMessage(message: String)
        fun stopDiscovery()
        fun startDiscovery()
        fun stopAllConnections()
        fun acceptConnection(user: String, endpointId: String)
        fun requestConnection(endpointId: String)
        fun isConnected(): Boolean
        fun getGuestList(): List<String>
        fun getHostUsername(): String
        fun refreshConnectionList()
        fun sendFile(uri: Uri? = null)
        fun attachImage(takePictureIntent: Intent, componentName: ComponentName)
        fun getMainLanguage(): String
        fun getMainTopic(): List<String>
        fun recognizeImage(imageUri: Uri)
    }
}
//package com.nearby.messages.nearbyconnection.ui.chat
//
//import com.google.android.gms.nearby.Nearby
//import com.nearby.messages.nearbyconnection.arch.BasePresenter
//
//class ChatPresenter constructor(chatView: ChatMvp.View) : BasePresenter<ChatMvp.View>(chatView), ChatMvp.Presenter {
//
//    override fun init() {
//        connectionsClient = Nearby.getConnectionsClient(context)
//    }
//
//    override fun addMessage(message: Pair<Pair<String, String>, Int>) {
//        messageList.add(message)
//        view?.setMessages(messageList)
//    }
//}
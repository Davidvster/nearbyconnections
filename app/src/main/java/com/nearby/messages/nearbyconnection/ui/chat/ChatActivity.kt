//package com.nearby.messages.nearbyconnection.ui.chat
//
//import com.nearby.messages.nearbyconnection.arch.BaseActivity
//import android.os.Bundle
//import com.nearby.messages.nearbyconnection.R
//
//class ChatActivity : BaseActivity<ChatMvp.Presenter>(), ChatMvp.View {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        presenter = ChatPresenter(this)
//        setContentView(R.layout.activity_chat)
//
//        messages_send.setOnClickListener {
//            presenter.sendMessage(messages_input.text.toString())
//            presenter.addMessage(Pair(Pair(messages_input.text.toString(), messages_user.text.toString()), 1))
//        }
//    }
//
//    override fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>) {
//        connectionAdapter.messagesList = messageList.toMutableList()
//        connectionAdapter.notifyDataSetChanged()
//        messages_input.text = null
//        messages_content.scrollToPosition(messageList.size - 1)
//    }
//
//}

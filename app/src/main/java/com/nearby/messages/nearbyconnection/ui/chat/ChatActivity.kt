package com.nearby.messages.nearbyconnection.ui.chat

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_connection.*

class ChatActivity : BaseActivity<ChatMvp.Presenter>(), ChatMvp.View {

    lateinit var username: String

    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChatPresenter(this)
        setContentView(R.layout.activity_chat)

        presenter.init()
        presenter.startDiscovery(packageName)

        username = intent.getStringExtra(ARG_MY_USER_NAME)

        connectionAdapter = ConnectionAdapter(this)
        connection_content.layoutManager = LinearLayoutManager(this)
        connection_content.adapter = connectionAdapter

        chatAdapter = ChatAdapter(this)
        messages_content.layoutManager = LinearLayoutManager(this)
        messages_content.adapter = chatAdapter

        connectionAdapter.onRoomClicked =  {
            presenter.stopDiscovery()
            ChatActivity.start(this, connection_user.text.toString())
        }

        messages_send.setOnClickListener {
            presenter.sendMessage(messages_input.text.toString())
            presenter.addMessage(Pair(Pair(messages_input.text.toString(), username), 1))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAllConnections()
    }

    override fun updateConnectionList(availableRooms: MutableList<String>) {
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyDataSetChanged()
        connection_content.scrollToPosition(availableRooms.size - 1)
    }

    override fun setChattiningTitle(guestNames: HashMap<String, String>) {
        messages_guest_name.text = "Chatting with: " + guestNames
    }

    override fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyDataSetChanged()
        messages_input.text = null
        messages_content.scrollToPosition(messageList.size - 1)
    }

    companion object {
        val ARG_MY_USER_NAME = "username.string"

        @JvmStatic
        fun start(context: Activity, username: String) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            context.startActivity(intent)
        }
    }

}

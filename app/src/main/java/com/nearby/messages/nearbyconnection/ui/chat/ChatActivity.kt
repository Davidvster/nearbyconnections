package com.nearby.messages.nearbyconnection.ui.chat

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.Date

class ChatActivity : BaseActivity<ChatMvp.Presenter>(), ChatMvp.View {

    lateinit var username: String
    private var cardColor: Int = -1

    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChatPresenter(this)
        setContentView(R.layout.activity_chat)

        title = "Connect to a Chat Room"
        setSupportActionBar(chat_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        username = intent.getStringExtra(ARG_MY_USER_NAME)
        cardColor = intent.getIntExtra(ARG_CARD_BACKGROUND_COLOR, -1)

        presenter.init(username, packageName, cardColor)
        presenter.startDiscovery()

        connectionAdapter = ConnectionAdapter(this)
        connection_content.layoutManager = LinearLayoutManager(this)
        connection_content.adapter = connectionAdapter

        chatAdapter = ChatAdapter(this)
        chat_content.layoutManager = LinearLayoutManager(this)
        chat_content.adapter = chatAdapter

        connectionAdapter.onRoomClicked =  {
//            presenter.stopDiscovery()
            setProgressVisible(true)
            presenter.requestConnection(it)
//            ChatActivity.start(this, connection_user.text.toString())
        }

        chat_send.setOnClickListener {
            if (!chat_input.text.toString().isNullOrEmpty() && chat_input.text.toString() != "") {
                presenter.sendMessage(chat_input.text.toString())
                val chatMessage = ChatMessage(username, chat_input.text.toString(), Date().toString(), cardColor)
                presenter.addMessage(Pair(chatMessage, 1))
            }
        }
    }

    override fun setToolbarTitle(newTitle: String) {
        supportActionBar!!.title = newTitle
    }

    override fun setChatRoom() {
        chat_room_layout.visibility = View.VISIBLE
        connection_layout.visibility = View.GONE
        connectionAdapter.connectionList = mutableListOf()
    }

    override fun setConnectionRoom() {
        setProgressVisible(false)
        chat_room_layout.visibility = View.GONE
        connection_layout.visibility = View.VISIBLE
        chatAdapter.messagesList = mutableListOf()
        supportActionBar!!.title = "Connect to a Chat Room"
        presenter.startDiscovery()

    }

    override fun setProgressVisible(visible: Boolean) {
        if (visible) {
            connection_progress.visibility = View.VISIBLE
        } else {
            connection_progress.visibility = View.GONE
        }
    }

    override fun updateConnectionList(availableRooms: MutableList<Pair<String, String>>) {
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyItemInserted(availableRooms.size-1)
//        connection_content.scrollToPosition(availableRooms.size - 1)
    }

    override fun setParitipantsList(guestNames: List<String>) {
        chat_guest_name.text = "Chatting with: " + guestNames
    }

    override fun setMessages(messageList: List<Pair<ChatMessage, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyItemInserted(messageList.size-1)
        chat_input.text = null
        chat_content.scrollToPosition(messageList.size - 1)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (presenter.isConnected()) {
                    presenter.stopDiscovery()
                    presenter.stopAllConnections()
                    setConnectionRoom()
                } else {
                    this.finish()
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (presenter.isConnected()) {
            presenter.stopDiscovery()
            presenter.stopAllConnections()
            setConnectionRoom()
        } else {
            this.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopDiscovery()
        presenter.stopAllConnections()
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }

}

package com.nearby.messages.nearbyconnection.ui.chat

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.ui.views.GuestListDialog
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.Date

class ChatActivity : BaseActivity<ChatMvp.Presenter>(), ChatMvp.View {

    lateinit var username: String
    private var cardColor: Int = -1

    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var guestListMenu: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChatPresenter(this)
        setContentView(R.layout.activity_chat)

        title = resources.getString(R.string.chat_connect_room_title)
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
            setProgressVisible(true)
            presenter.requestConnection(it)
        }

        chat_send.setOnClickListener {
            if (!chat_input.text.toString().isNullOrEmpty() && chat_input.text.toString() != "" && chat_input.text.toString().replace("\\s".toRegex(), "").isNotEmpty()) {
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
        guestListMenu.isVisible = true
    }

    override fun setConnectionRoom() {
        setProgressVisible(false)
        chat_room_layout.visibility = View.GONE
        connection_layout.visibility = View.VISIBLE
        chatAdapter.messagesList = mutableListOf()
        supportActionBar!!.title = resources.getString(R.string.chat_connect_room_title)
        presenter.startDiscovery()
        guestListMenu.isVisible = false
        Toast.makeText(this, resources.getString(R.string.connection_ended), Toast.LENGTH_SHORT).show()
    }

    override fun setProgressVisible(visible: Boolean) {
        if (visible) {
            connection_progress.visibility = View.VISIBLE
            connectionAdapter.isClickable = false
        } else {
            connection_progress.visibility = View.GONE
            connectionAdapter.isClickable = true
        }
    }

    override fun updateConnectionList(availableRooms: MutableList<Pair<String, String>>) {
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyDataSetChanged()
    }

    override fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyItemInserted(messageList.size-1)
        chat_input.text = null
        chat_content.scrollToPosition(messageList.size - 1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_guests, menu)
        guestListMenu = menu.findItem(R.id.guests_list)
        guestListMenu.isVisible = false
        return true
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
            R.id.guests_list -> {
                if (presenter.getGuestList().isNotEmpty()) {
                    GuestListDialog(this).init(presenter.getGuestList())
                            .setPositiveButton { dialog ->
                                dialog.dismiss()
                            }
                            .setTitleText(resources.getString(R.string.chat_guest_list_room_host, presenter.getHostUsername()))
                            .show()
                } else{
                    Toast.makeText(this, resources.getString(R.string.guest_list_only_two_participants), Toast.LENGTH_LONG).show()
                }
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

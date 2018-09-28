package com.nearby.messages.nearbyconnection.ui.chat

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.ui.views.GuestListDialog
import kotlinx.android.synthetic.main.activity_chat.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.google.android.gms.nearby.connection.Payload


class ChatActivity : BaseActivity<ChatMvp.Presenter>(), ChatMvp.View {

    private val READ_REQUEST_CODE = 135

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
                val format = DateTimeFormat.forPattern("HH:mm - d.MM.yyyy")
                val formattedDate = format.print(DateTime.now())
                val chatMessage = ChatMessage(username, chat_input.text.toString(), formattedDate, cardColor, 1)
                presenter.addMessage(Pair(chatMessage, 1))
            }
        }

        chat_add.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        connection_content_refresh.setOnRefreshListener {
            connection_content_refresh.isRefreshing = true
            presenter.refreshConnectionList()
        }
    }

    override fun stopRefreshConnectionList() {
        connection_content_refresh.isRefreshing = false
        chat_connect_search.visibility = View.VISIBLE
        presenter.startDiscovery()
    }

    override fun setToolbarTitle(roomTitle: String) {
        supportActionBar!!.title = roomTitle
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
        chat_connect_search.visibility = View.VISIBLE
        chatAdapter.messagesList = mutableListOf()
        supportActionBar!!.title = resources.getString(R.string.chat_connect_room_title)
        presenter.startDiscovery()
        guestListMenu.isVisible = false
        Snackbar.make(connection_layout, resources.getString(R.string.connection_ended), Snackbar.LENGTH_SHORT).show()
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

    override fun updateConnectionList(availableRooms: List<Pair<String, String>>) {
        if (availableRooms.isEmpty()) {
            chat_connect_search.visibility = View.VISIBLE
        } else {
            chat_connect_search.visibility = View.GONE
        }
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyDataSetChanged()
    }

    override fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>) {
        chatAdapter.messagesList = messageList
        chatAdapter.notifyItemInserted(messageList.size-1)
        chat_input.text = null
        chat_content.scrollToPosition(messageList.size - 1)
    }

    override fun updateMessageList(messageList: List<Pair<ChatMessage, Int>>, position: Int) {
        chatAdapter.messagesList = messageList
        chatAdapter.notifyItemChanged(position)
        chat_input.text = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri = data.data

                val pfd = contentResolver.openFileDescriptor(uri, "r")
                val filePayload = Payload.fromFile(pfd)

                presenter.sendFile(filePayload)

                val format = DateTimeFormat.forPattern("HH:mm - d.MM.yyyy")
                val formattedDate = format.print(DateTime.now())
                val chatMessage = ChatMessage(username, filePayload.id.toString(), formattedDate, cardColor, 2)
                presenter.addMessage(Pair(chatMessage, 1))
            }
        }
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
package com.nearby.messages.nearbyconnection.ui.hostchat

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.ui.chat.ChatAdapter
import com.nearby.messages.nearbyconnection.ui.views.GuestListDialog
import kotlinx.android.synthetic.main.activity_host_chat.*
import java.text.SimpleDateFormat
import java.util.Date

class HostChatActivity : BaseActivity<HostChatMvp.Presenter>(), HostChatMvp.View {

    private lateinit var chatAdapter: ChatAdapter
    lateinit var username: String
    private var cardColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = HostChatPresenter(this)
        setContentView(R.layout.activity_host_chat)

        setSupportActionBar(host_chat_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        username = intent.getStringExtra(ARG_MY_USER_NAME)
        cardColor = intent.getIntExtra(ARG_CARD_BACKGROUND_COLOR, -1)

        presenter.init(username, packageName, cardColor)
        presenter.startAdvertising()

        chatAdapter = ChatAdapter(this)
        chat_content.layoutManager = LinearLayoutManager(this)
        chat_content.adapter = chatAdapter

        chat_send.setOnClickListener {
            if (!chat_input.text.toString().isNullOrEmpty() && chat_input.text.toString() != "" && chat_input.text.toString().replace("\\s".toRegex(), "").isNotEmpty()) {
                val format = SimpleDateFormat("HH:mm - d.MM.yyyy")
                val formattedDate = format.format(Date())
                val chatMessage = ChatMessage(username, chat_input.text.toString(), formattedDate, cardColor)
                presenter.sendMessage(Gson().toJson(chatMessage))
                presenter.addMessage(Pair(chatMessage, 1))
            }
        }
    }

    override fun showJoinDialog(user: String, endpointId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.dialog_request_title))
        builder.setMessage(resources.getString(R.string.dialog_request_description, user ))
        builder.setPositiveButton(resources.getString(R.string.dialog_request_accept)) { _, _ ->
            presenter.acceptConnection(user, endpointId)
        }
        builder.setNegativeButton(resources.getString(R.string.dialog_request_reject)) { _, _ ->
            presenter.rejectConnection(endpointId)
        }
        builder.setOnDismissListener { presenter.rejectConnection(endpointId) }
        val dialog = builder.create()
        dialog.show()
    }

    override fun setMessages(messageList: List<Pair<ChatMessage, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyItemInserted(messageList.size-1)
        chat_input.text = null
        chat_content.scrollToPosition(messageList.size - 1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_guests, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
            R.id.guests_list -> {
                if (presenter.getGuestList().isNotEmpty()) {
                    GuestListDialog(this).init(presenter.getGuestList())
                            .setPositiveButton { dialog ->
                                dialog.dismiss()
                            }
                            .setTitleText(resources.getString(R.string.chat_host_room_title))
                            .show()
                }
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAdvertising()
        presenter.stopAllConnections()
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, HostChatActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}

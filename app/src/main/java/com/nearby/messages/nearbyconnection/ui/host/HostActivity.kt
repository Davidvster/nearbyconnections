package com.nearby.messages.nearbyconnection.ui.host

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.ui.chat.ChatAdapter
import kotlinx.android.synthetic.main.activity_host.*
import java.util.Date

class HostActivity : BaseActivity<HostMvp.Presenter>(), HostMvp.View {

    private lateinit var chatAdapter: ChatAdapter
    lateinit var username: String
    private var cardColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = HostPresenter(this)
        setContentView(R.layout.activity_host)

        setSupportActionBar(host_chat_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)

        username = intent.getStringExtra(ARG_MY_USER_NAME)
        cardColor = intent.getIntExtra(ARG_CARD_BACKGROUND_COLOR, -1)

        presenter.init(username, packageName, cardColor)
        presenter.startAdvertising()

        chatAdapter = ChatAdapter(this)
        messages_content.layoutManager = LinearLayoutManager(this)
        messages_content.adapter = chatAdapter

        messages_send.setOnClickListener {
            if (!messages_input.text.toString().isNullOrEmpty() && messages_input.text.toString() != "") {
                val chatMessage = ChatMessage(username, messages_input.text.toString(), Date().toString(), cardColor)
                presenter.sendMessage(Gson().toJson(chatMessage))
                presenter.addMessage(Pair(chatMessage, 1))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                    this.finish()
                return true
            }
        }
        return false
    }

    override fun setChattiningTitle(guestNames: HashMap<String, String>) {
        messages_guest_name.text = "Chatting with: " + guestNames
    }

    override fun onBackPressed() {
        super.onBackPressed()
        presenter.stopAllConnections()
        this.finish()
    }

    override fun showConnectionDialog(user: String, endpointId: String) {
        val builder = AlertDialog.Builder(this)

        messages_guest_name.visibility = View.VISIBLE

        builder.setTitle("Connection found")
        builder.setMessage(user+ " wants to connect to you!")
        builder.setPositiveButton("Accept") { dialog, which ->
            messages_guest_name.visibility = View.VISIBLE
            presenter.acceptConnection(user, endpointId)
        }
        builder.setNegativeButton("Reject") { dialog, which ->
            presenter.rejectConnection(endpointId)
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun setMessages(messageList: List<Pair<ChatMessage, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyDataSetChanged()
        messages_input.text = null
        messages_content.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAllConnections()
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, HostActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}

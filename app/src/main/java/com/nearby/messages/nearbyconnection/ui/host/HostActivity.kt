package com.nearby.messages.nearbyconnection.ui.host

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.ui.chat.ChatActivity
import com.nearby.messages.nearbyconnection.ui.chat.ChatAdapter
import kotlinx.android.synthetic.main.activity_chat.*

class HostActivity : BaseActivity<HostMvp.Presenter>(), HostMvp.View {

    private lateinit var chatAdapter: ChatAdapter
    lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = HostPresenter(this)
        setContentView(R.layout.activity_host)

        presenter.init()
        username = intent.getStringExtra(ARG_MY_USER_NAME)
        presenter.startAdvertising(username, packageName)

        chatAdapter = ChatAdapter(this)
        messages_content.layoutManager = LinearLayoutManager(this)
        messages_content.adapter = chatAdapter
    }

    companion object {
        val ARG_MY_USER_NAME = "username.string"

        @JvmStatic
        fun start(context: Activity, username: String) {
            val intent = Intent(context, HostActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            context.startActivity(intent)
        }
    }

    override fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>) {
        chatAdapter.messagesList = messageList.toMutableList()
        chatAdapter.notifyDataSetChanged()
        messages_input.text = null
        messages_content.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAllConnections()
    }

}

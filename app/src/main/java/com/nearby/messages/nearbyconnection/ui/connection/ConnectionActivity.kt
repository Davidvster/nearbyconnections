package com.nearby.messages.nearbyconnection.ui.connection

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.activity_connection.*
import android.content.DialogInterface

class ConnectionActivity : BaseActivity<ConnectionMvp.Presenter>(), ConnectionMvp.View {

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    private lateinit var connectionAdapter: ConnectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ConnectionPresenter(this)
        setContentView(R.layout.activity_connection)

        presenter.init()

        connectionAdapter = ConnectionAdapter(this)

        messages_content.layoutManager = LinearLayoutManager(this)
        messages_content.adapter = connectionAdapter

        message_connect.setOnClickListener {
            message_connect.isEnabled = false
            messages_user.isEnabled = false
            connectionAdapter.userId = messages_user.text.toString()
            presenter.startDiscovery(packageName)
            presenter.startAdvertising(messages_user.text.toString(), packageName)
        }

        messages_send.setOnClickListener {
            presenter.sendMessage(messages_input.text.toString())
            presenter.addMessage(Pair(Pair(messages_input.text.toString(), messages_user.text.toString()), 1))
        }

    }

    override fun setMessages(messageList: List<Pair<Pair<String, String>, Int>>) {
        connectionAdapter.messagesList = messageList.toMutableList()
        connectionAdapter.notifyDataSetChanged()
        messages_input.text = null
        messages_content.scrollToPosition(messageList.size - 1)
    }

    override fun setDisconnected() {
        message_connect.visibility = View.VISIBLE
        messages_user.visibility = View.VISIBLE
        message_connect.isEnabled = true
        messages_user.isEnabled = true
        messages_guest_name.visibility = View.GONE
        messages_guest_name.text = ""
        messages_content.visibility = View.GONE
        messages_input.visibility = View.GONE
        messages_send.visibility = View.GONE
    }

    override fun setConnected() {
        message_connect.visibility = View.GONE
        messages_user.visibility = View.GONE
        messages_content.visibility = View.VISIBLE
        messages_input.visibility = View.VISIBLE
        messages_send.visibility = View.VISIBLE
    }

    override fun setChattiningTitle(guestNames: HashMap<String, String>) {
        messages_guest_name.text = "Chatting with: " + guestNames
    }

    override fun showConnectionDialog(user: String, endpointId: String) {
        val builder = AlertDialog.Builder(this)

        messages_guest_name.visibility = View.VISIBLE
        presenter.acceptConnection(user, endpointId)

        builder.setTitle("Connection found")
        builder.setMessage(user+ " wants to connect to you!")
        builder.setPositiveButton("Accept") { dialog, which ->
            messages_guest_name.visibility = View.VISIBLE
            presenter.acceptConnection(user, endpointId)
        }
        builder.setNegativeButton("Reject") { dialog, which ->
            message_connect.isEnabled = true
            messages_user.isEnabled = true
        }
        val dialog = builder.create()
//        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.basic_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.search_connections -> {
                presenter.startDiscovery(packageName)
                return true
            }
            R.id.advertise_connections -> {
                presenter.startAdvertising(messages_user.text.toString(), packageName)
                return true
            }
            R.id.avaible_devices -> {
                showAvaibleDevicesDialog(presenter.getAvaibleGuests())
                return true
            }
        }
        return false
    }

    override fun showAvaibleDevicesDialog(avaibleGuests: HashMap<String, String>) {
        val guestsLabel = mutableListOf<String>()
        val guests = mutableListOf<String>()
        for (guest in avaibleGuests) {
            guestsLabel.add(guest.key+" - "+guest.value)
            guests.add(guest.key)
        }
        if (guests.isNotEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select a device you want to connect/disconnect to")
            builder.setItems(guestsLabel.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                // the user clicked on colors[which]
                presenter.requestConnection(guests[which])
            })
            builder.show()
        }
    }
    override fun onStart() {
        super.onStart()

        if (!hasPermissions(this, *REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
    }

    override fun onStop() {
//        presenter.stopAllConnections()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAllConnections()
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return
        }

        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "A permission was denied", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        recreate()
    }
}

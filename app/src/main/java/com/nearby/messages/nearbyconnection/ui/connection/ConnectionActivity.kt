package com.nearby.messages.nearbyconnection.ui.connection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.activity_connection.*
import android.view.View
import com.nearby.messages.nearbyconnection.ui.chat.ChatActivity
import com.nearby.messages.nearbyconnection.ui.host.HostActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.nearby.messages.nearbyconnection.ui.views.ColorPickerDialog


class ConnectionActivity : BaseActivity<ConnectionMvp.Presenter>(), ConnectionMvp.View {

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private var cardColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ConnectionPresenter(this)
        setContentView(R.layout.activity_connection)

        connection_chat_search.setOnClickListener {
            if (checkInputName()) {
                ChatActivity.start(this, connection_user.text.toString(), cardColor)
            }
        }

        connection_chat_create.setOnClickListener {
            if (checkInputName()) {
                HostActivity.start(this, connection_user.text.toString(), cardColor)
            }
        }

        select_color.setOnClickListener {
            ColorPickerDialog(this).init()
                    .setTitleText("Pick a color")
                    .setPositiveButton("Ok") { dialog ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog ->
                        dialog.dismiss()
                    }
                    .show()
//            ColorPickerDialogBuilder
//                    .with(this)
//                    .setTitle("Choose color")
//                    .initialColor(R.color.color_message_bg_guest)
//                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                    .density(12)
//                    .setOnColorSelectedListener { selectedColor -> Toast.makeText(this, "Selected color: " + Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show() }
//                    .setPositiveButton("Ok") { dialog, selectedColor, allColors -> cardColor = selectedColor }
//                    .setNegativeButton("Cancel") { dialog, which -> }
//                    .build()
//                    .show()
        }

    }

    private fun checkInputName(): Boolean {
        if (connection_user.text.toString().isNullOrEmpty() || connection_user.text.toString() == "") {
            connection_error_user.text = "Please insert an username!"
            connection_error_user.visibility = View.VISIBLE
            return false
        }
        if (connection_user.text.toString().replace("/^\\s*/".toRegex(), "").isEmpty()) {
            connection_error_user.text = "Please insert a valid username!"
            connection_error_user.visibility = View.VISIBLE
            return false
        }
        return true
    }



//    override fun setDisconnected() {
//        message_connect.visibility = View.VISIBLE
//        messages_user.visibility = View.VISIBLE
//        message_connect.isEnabled = true
//        messages_user.isEnabled = true
//        messages_guest_name.visibility = View.GONE
//        messages_guest_name.text = ""
//        messages_content.visibility = View.GONE
//        messages_input.visibility = View.GONE
//        messages_send.visibility = View.GONE
//    }
//
//    override fun setConnected() {
//        message_connect.visibility = View.GONE
//        messages_user.visibility = View.GONE
//        messages_content.visibility = View.VISIBLE
//        messages_input.visibility = View.VISIBLE
//        messages_send.visibility = View.VISIBLE
//    }

//    override fun showConnectionDialog(user: String, endpointId: String) {
//        val builder = AlertDialog.Builder(this)
//
//        messages_guest_name.visibility = View.VISIBLE
//        presenter.acceptConnection(user, endpointId)
//
//        builder.setTitle("Connection found")
//        builder.setMessage(user+ " wants to connect to you!")
//        builder.setPositiveButton("Accept") { dialog, which ->
//            messages_guest_name.visibility = View.VISIBLE
//            presenter.acceptConnection(user, endpointId)
//        }
//        builder.setNegativeButton("Reject") { dialog, which ->
//            message_connect.isEnabled = true
//            messages_user.isEnabled = true
//        }
//        val dialog = builder.create()
////        dialog.show()
//    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.basic_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId){
//            R.id.search_connections -> {
//                presenter.startDiscovery(packageName)
//                return true
//            }
//            R.id.advertise_connections -> {
//                presenter.startAdvertising(connection_user.text.toString(), packageName)
//                return true
//            }
//            R.id.avaible_devices -> {
//                showAvaibleDevicesDialog(presenter.getAvaibleGuests())
//                return true
//            }
//        }
//        return false
//    }

//    override fun showAvaibleDevicesDialog(avaibleGuests: HashMap<String, String>) {
//        val guestsLabel = mutableListOf<String>()
//        val guests = mutableListOf<String>()
//        for (guest in avaibleGuests) {
//            guestsLabel.add(guest.key+" - "+guest.value)
//            guests.add(guest.key)
//        }
//        if (guests.isNotEmpty()) {
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Select a device you want to connect/disconnect to")
//            builder.setItems(guestsLabel.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
//                // the user clicked on colors[which]
//                presenter.requestConnection(guests[which])
//            })
//            builder.show()
//        }
//    }

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

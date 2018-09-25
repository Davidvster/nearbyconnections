package com.nearby.messages.nearbyconnection.ui.lobby

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.activity_lobby.*
import android.view.View
import com.nearby.messages.nearbyconnection.ui.chat.ChatActivity
import com.nearby.messages.nearbyconnection.ui.hostchat.HostChatActivity
import com.nearby.messages.nearbyconnection.ui.hostquiz.HostQuizActivity
import com.nearby.messages.nearbyconnection.ui.quiz.QuizActivity
import com.nearby.messages.nearbyconnection.ui.views.ColorPickDialog
import android.content.DialogInterface
import com.nearby.messages.nearbyconnection.ui.views.CustomFlag
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class LobbyActivity : BaseActivity<LobbyMvp.Presenter>(), LobbyMvp.View {

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private var cardColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = LobbyPresenter(this)
        setContentView(R.layout.activity_lobby)

        cardColor = ContextCompat.getColor(this, R.color.chat_color_1)
        lobby_chat_join.setOnClickListener {
            if (checkInputName()) {
                ChatActivity.start(this, lobby_user_name.text.toString(), cardColor)
            }
        }

        lobby_chat_host.setOnClickListener {
            if (checkInputName()) {
                HostChatActivity.start(this, lobby_user_name.text.toString(), cardColor)
            }
        }

        lobby_quiz_join.setOnClickListener {
            if (checkInputName()) {
                QuizActivity.start(this, lobby_user_name.text.toString(), cardColor)
            }
        }

        lobby_quiz_host.setOnClickListener {
            if (checkInputName()) {
                HostQuizActivity.start(this, lobby_user_name.text.toString(), cardColor)
            }
        }

        lobby_color_select.setOnClickListener {
            val builder = ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
            builder.setTitle("Pick your color")
            builder.setFlagView(CustomFlag(this, R.layout.dialog_layout_flag))
            builder.setPositiveButton(getString(R.string.color_confirm), ColorEnvelopeListener { envelope, fromUser -> cardColor = envelope.color })
            builder.setNegativeButton(getString(R.string.color_cancel), DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
//            builder.attachAlphaSlideBar() // attach AlphaSlideBar
//            builder.attachBrightnessSlideBar() // attach BrightnessSlideBar
            builder.show() // show dialog
//            ColorPickDialog(this).init()
//                    .setTitleText("Pick a color")
//                    .setSelectedColor(cardColor)
//                    .setPositiveButton("Ok") { dialog ->
//                        cardColor = dialog.selectedColor
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton("Cancel") { dialog ->
//                        dialog.dismiss()
//                    }
//                    .show()
        }
    }

    private fun checkInputName(): Boolean {
        if (lobby_user_name.text.toString().isNullOrEmpty() || lobby_user_name.text.toString() == "") {
            lobby_user_name_error.text = "Please insert an username!"
            lobby_user_name_error.visibility = View.VISIBLE
            return false
        }
        if (lobby_user_name.text.toString().replace("/^\\s*/".toRegex(), "").isEmpty()) {
            lobby_user_name_error.text = "Please insert a valid username!"
            lobby_user_name_error.visibility = View.VISIBLE
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        if (!hasPermissions(this, *REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }
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

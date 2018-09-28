package com.nearby.messages.nearbyconnection.ui.views

import android.support.v7.app.AppCompatDialog
import android.view.View
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.Window
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.dialog_guest_list.*

class GuestListDialog(context: Context) : AppCompatDialog(context) {

    private var onPositiveClicked: ((GuestListDialog) -> Unit)? = null

    private lateinit var guestListAdapter: GuestListAdapter

    fun init(guestList: List<String>): GuestListDialog {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_guest_list)
        dialog_guest_close.setOnClickListener {
            onPositiveClicked?.invoke(this)
        }

        guestListAdapter = GuestListAdapter(context)
        guestListAdapter.guestList = guestList
        dialog_guest_list.layoutManager = LinearLayoutManager(context)
        dialog_guest_list.adapter = guestListAdapter
        return this
    }

    fun setTitleText(title: String): GuestListDialog {
        dialog_guest_owner.text = title
        return this
    }

    fun setPositiveButton( onPositiveClick: ((GuestListDialog) -> Unit)?): GuestListDialog {
        dialog_guest_close.visibility = View.VISIBLE
        this.onPositiveClicked = onPositiveClick
        return this
    }
}
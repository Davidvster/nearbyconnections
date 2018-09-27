package com.nearby.messages.nearbyconnection.ui.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.element_dialog_guest.view.*

class GuestListAdapter constructor(val context: Context) : RecyclerView.Adapter<GuestListAdapter.ViewHolder>() {

    var guestList = listOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_dialog_guest, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        guestList[holder.adapterPosition].let { guest ->
            holder.itemView.guest_name.text = guest
        }
    }

    override fun getItemCount(): Int = guestList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
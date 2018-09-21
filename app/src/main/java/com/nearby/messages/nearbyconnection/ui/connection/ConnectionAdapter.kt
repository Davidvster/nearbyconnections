package com.nearby.messages.nearbyconnection.ui.connection

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R

class ConnectionAdapter constructor(val context: Context) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    var messagesList = mutableListOf<Pair<Pair<String, String>, Int>>()
    var userId = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionAdapter.ViewHolder {
        when (viewType) {
            1 -> {
                return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_message_me, parent, false))
            }
            2 -> {
                return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_message_guest, parent, false))
            }
        }
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_message_me, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return messagesList[position].second
    }

    override fun onBindViewHolder(holder: ConnectionAdapter.ViewHolder, position: Int) {
        messagesList[holder.adapterPosition].first.let { message ->
            holder.itemView.findViewById<TextView>(R.id.message_content_user).text = message.second
            holder.itemView.findViewById<TextView>(R.id.message_content_message).text = message.first
        }
    }

    override fun getItemCount(): Int = messagesList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
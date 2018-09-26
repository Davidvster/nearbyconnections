package com.nearby.messages.nearbyconnection.ui.chat

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import java.text.SimpleDateFormat


class ChatAdapter constructor(val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messagesList = mutableListOf<Pair<ChatMessage, Int>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
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

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        messagesList[holder.adapterPosition].first.let { message ->
            holder.itemView.findViewById<TextView>(R.id.message_content_user).text = message.user
            holder.itemView.findViewById<TextView>(R.id.message_content_message).text = message.message
            val parser = SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy")
            val date = parser.parse(message.date)
            val format = SimpleDateFormat("HH:mm:ss d.MM.yyyy")
            val formatedDate = format.format(date)
            holder.itemView.findViewById<TextView>(R.id.message_content_date).text = formatedDate.toString()
            holder.itemView.findViewById<CardView>(R.id.message_content_user_card).setCardBackgroundColor(message.color)

        }
    }

    override fun getItemCount(): Int = messagesList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
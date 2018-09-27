package com.nearby.messages.nearbyconnection.ui.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.element_connection.view.*

class ConnectionAdapter constructor(val context: Context) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    var connectionList = mutableListOf<Pair<String, String>>()

    var onRoomClicked: ((endpointId: String) -> Unit)? = null

    var isClickable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_connection, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        connectionList[holder.adapterPosition].let { connection ->
            holder.itemView.connection_element_name.text = connection.second
            holder.itemView.connection_card.setOnClickListener {
                if (isClickable) {
                    onRoomClicked!!.invoke(connection.first)
                }
            }
        }
    }

    override fun getItemCount(): Int = connectionList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
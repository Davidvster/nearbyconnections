package com.nearby.messages.nearbyconnection.ui.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R

class ConnectionAdapter constructor(val context: Context) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    var connectionList = mutableListOf<String>()

    var onRoomClicked: ((endpointId: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_connection, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        connectionList[holder.adapterPosition].let { endpointId ->
            holder.itemView.findViewById<TextView>(R.id.connection_element_name).text = endpointId
            holder.itemView.setOnClickListener {
                onRoomClicked!!.invoke(endpointId)
            }
        }
    }

    override fun getItemCount(): Int = connectionList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
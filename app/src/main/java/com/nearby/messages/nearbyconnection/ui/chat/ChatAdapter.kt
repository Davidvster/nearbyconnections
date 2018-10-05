package com.nearby.messages.nearbyconnection.ui.chat

import android.content.Context
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.squareup.picasso.Picasso

class ChatAdapter constructor(val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messagesList = listOf<Pair<ChatMessage, Int>>()

    var onImageClicked: ((fileUri: String) -> Unit)? = null

    var onImageLongPressed: ((fileUri: Uri) -> Unit)? = null

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
            val imageView = holder.itemView.findViewById<ImageView>(R.id.message_content_image)
            imageView.visibility = View.GONE
            if (message.type == 2) {
                if (message.picture == null && message.pictureUri == null ) {
                    holder.itemView.findViewById<TextView>(R.id.message_content_message).text = context.resources.getString(R.string.chat_message_image_loading)
                } else {
                    holder.itemView.findViewById<TextView>(R.id.message_content_message).text = ""
                    if (message.picture != null) {
                        Picasso.with(context).load(message.picture).resize(800, 800).centerInside().into(imageView)
                        imageView.visibility = View.VISIBLE
                        imageView.setOnClickListener {
                            onImageClicked!!.invoke(Uri.fromFile(message.picture).toString())
                        }
                        imageView.setOnLongClickListener {
                            onImageLongPressed!!.invoke(Uri.fromFile(message.picture!!))
                            true
                        }
                    } else if (message.pictureUri != null) {
                        Picasso.with(context).load(message.pictureUri).resize(800, 800).centerInside().into(imageView)
                        imageView.visibility = View.VISIBLE
                        imageView.setOnClickListener {
                            onImageClicked!!.invoke(message.pictureUri!!.toString())
                        }
                        imageView.setOnLongClickListener {
                            onImageLongPressed!!.invoke(message.pictureUri!!)
                            true
                        }
                    } else {
                        holder.itemView.findViewById<TextView>(R.id.message_content_message).text = context.resources.getString(R.string.chat_message_image_loading_error)
                    }
                }
            } else {
                holder.itemView.findViewById<TextView>(R.id.message_content_message).text = message.message
            }
            holder.itemView.findViewById<TextView>(R.id.message_content_date).text = message.date
            holder.itemView.findViewById<CardView>(R.id.message_content_user_card).setCardBackgroundColor(message.color)
        }
    }

    override fun getItemCount(): Int = messagesList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
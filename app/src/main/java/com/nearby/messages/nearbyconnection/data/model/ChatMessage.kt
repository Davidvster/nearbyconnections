package com.nearby.messages.nearbyconnection.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ChatMessage(@SerializedName("user")
                       val user: String,
                       @SerializedName("message")
                       val message: String,
                       @SerializedName("date")
                       val date: String,
                       @SerializedName("color")
                       val color: Int
)
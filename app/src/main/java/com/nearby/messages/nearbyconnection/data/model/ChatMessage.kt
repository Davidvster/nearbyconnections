package com.nearby.messages.nearbyconnection.data.model

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.Date

data class ChatMessage(@SerializedName("user")
                       val user: String,
                       @SerializedName("message")
                       val message: String,
                       @SerializedName("date")
                       val date: String,
                       @SerializedName("color")
                       val color: Int,
                       @SerializedName("type")
                       val type: Int
) {
    var picture: File? = null
    var pictureUri: Uri? = null
}
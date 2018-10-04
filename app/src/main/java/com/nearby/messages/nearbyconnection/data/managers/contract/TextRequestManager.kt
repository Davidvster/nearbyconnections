package com.nearby.messages.nearbyconnection.data.managers.contract

import android.net.Uri
import com.google.gson.JsonObject
import io.reactivex.Single

interface TextRequestManager {
    fun getTextSummary(language: String, text: String): Single<JsonObject>
    fun getTextLanguage(text: String) : Single<JsonObject>
    fun getImageClassification(imageUri: Uri)
}
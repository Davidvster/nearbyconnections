package com.nearby.messages.nearbyconnection.data.managers.contract

import android.net.Uri
import com.google.gson.JsonObject
import io.reactivex.Single

interface RecognizeRequestManager {
    fun getTextSummary(language: String, text: String): Single<JsonObject>
    fun getTextLanguage(text: String) : Single<JsonObject>
    fun recognizeImage(imageUri: Uri): Single<Pair<String, String>>
}
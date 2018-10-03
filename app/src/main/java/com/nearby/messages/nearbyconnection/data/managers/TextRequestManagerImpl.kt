package com.nearby.messages.nearbyconnection.data.managers

import com.google.gson.JsonObject
import com.nearby.messages.nearbyconnection.data.api.TextTisaneService
import com.nearby.messages.nearbyconnection.data.managers.contract.TextRequestManager
import io.reactivex.Single
import com.nearby.messages.nearbyconnection.arch.ApiModule
import com.nearby.messages.nearbyconnection.data.api.LanguageService

class TextRequestManagerImpl constructor(private val textTisaneService: TextTisaneService = ApiModule.textTisaneService,
                                         private val languageService: LanguageService = ApiModule.languageService) : TextRequestManager {

    override fun getTextSummary(language: String, text: String): Single<JsonObject> {
        val jsonObject = JsonObject()

        jsonObject.addProperty("language", language)
        jsonObject.addProperty("content", text)
        jsonObject.add("settings", JsonObject())
        return textTisaneService.summarizeText(jsonObject)
    }

    override fun getTextLanguage(text: String): Single<JsonObject> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("q", text)
        return languageService.getTextLanguage(jsonObject)
    }
}
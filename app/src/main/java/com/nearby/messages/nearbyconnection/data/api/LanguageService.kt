package com.nearby.messages.nearbyconnection.data.api

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LanguageService {
    @POST("detect")
    fun getTextLanguage(@Body requestBody: JsonObject): Single<JsonObject>
}

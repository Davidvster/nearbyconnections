package com.nearby.messages.nearbyconnection.data.api

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TextTisaneService {
    @POST("parse")
    fun summarizeText(@Body requestBody: JsonObject): Single<JsonObject>

    @GET("parse")
    fun summarize(): Single<String>
}

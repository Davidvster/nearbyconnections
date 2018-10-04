package com.nearby.messages.nearbyconnection.data.api

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface ImageService {
    @POST("imageClassify")
    fun getImageClassification(@Body requestBody: JsonObject): Single<JsonObject>
}
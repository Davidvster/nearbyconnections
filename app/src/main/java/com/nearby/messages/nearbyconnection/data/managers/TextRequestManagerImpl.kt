package com.nearby.messages.nearbyconnection.data.managers

import android.content.Context
import com.google.gson.JsonObject
import com.nearby.messages.nearbyconnection.data.api.TextTisaneService
import com.nearby.messages.nearbyconnection.data.managers.contract.TextRequestManager
import io.reactivex.Single
import com.nearby.messages.nearbyconnection.arch.ApiModule
import com.nearby.messages.nearbyconnection.data.api.ImageService
import com.nearby.messages.nearbyconnection.data.api.LanguageService
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.nearby.messages.nearbyconnection.arch.AppModule
import java.io.ByteArrayOutputStream
import okhttp3.OkHttpClient
import clarifai2.api.ClarifaiBuilder
import clarifai2.dto.input.ClarifaiInput
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.concurrent.thread
import com.cloudmersive.client.invoker.auth.ApiKeyAuth
import com.cloudmersive.client.invoker.Configuration.getDefaultApiClient
import com.cloudmersive.client.invoker.ApiClient
import com.cloudmersive.client.invoker.Configuration
import com.cloudmersive.client.ConvertDocumentApi
import com.cloudmersive.client.invoker.ApiException
import com.cloudmersive.client.model.ImageDescriptionResponse
import com.cloudmersive.client.RecognizeApi




class TextRequestManagerImpl constructor(private val context: Context = AppModule.application,
                                         private val textTisaneService: TextTisaneService = ApiModule.textTisaneService,
                                         private val languageService: LanguageService = ApiModule.languageService,
                                         private val imageService: ImageService = ApiModule.imageService) : TextRequestManager {

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

    override fun getImageClassification(imageUri: Uri) {




//       val response = client.defaultModels.generalModel().predict()
//               .withInputs(ClarifaiInput.forImage(f))
//               .executeAsync { it ->
//                   Log.v("SLIKA", it.toString()) }
//
//        val jsonObject = JsonObject()
//
//
//        byteArray.toString()
//        jsonObject.addProperty("file", byteArray.toString())
//        return imageService.getImageClassification(jsonObject)
    }
}
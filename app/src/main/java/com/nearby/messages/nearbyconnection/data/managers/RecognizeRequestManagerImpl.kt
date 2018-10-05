package com.nearby.messages.nearbyconnection.data.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions
import com.google.gson.JsonObject
import com.nearby.messages.nearbyconnection.data.api.TextTisaneService
import com.nearby.messages.nearbyconnection.data.managers.contract.RecognizeRequestManager
import io.reactivex.Single
import com.nearby.messages.nearbyconnection.arch.ApiModule
import com.nearby.messages.nearbyconnection.data.api.LanguageService
import com.nearby.messages.nearbyconnection.arch.AppModule
import timber.log.Timber

class RecognizeRequestManagerImpl constructor(private val context: Context = AppModule.application,
                                              private val textTisaneService: TextTisaneService = ApiModule.textTisaneService,
                                              private val languageService: LanguageService = ApiModule.languageService) : RecognizeRequestManager {

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

    override fun recognizeImage(imageUri: Uri): Single<Pair<String, String>> {
        return Single.create<Pair<String, String>> { single ->
            FirebaseApp.initializeApp(context)
            val fireVisionOptions = FirebaseVisionLabelDetectorOptions.Builder()
                    .setConfidenceThreshold(0.8f)
                    .build()

            val imageOptions = BitmapFactory.Options()
            imageOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

            val image = FirebaseVisionImage.fromBitmap(bitmap)

            FirebaseVision.getInstance()
            val detector = FirebaseVision.getInstance()
                    .getVisionLabelDetector(fireVisionOptions)

            detector.detectInImage(image)
                    .addOnSuccessListener { labels ->
                        var objects = ""
                        for (label in labels) {
                            objects += label.label + "; "
                        }
                        if (objects.isEmpty()) {
                            objects = "Could not recognize any object in this image."
                        }
                        single.onSuccess(Pair("Found", objects))
                    }
                    .addOnFailureListener { it ->
                        Timber.d(it)
                        single.onSuccess(Pair("Error:", "An error appeared while analysing the image."))
                    }
        }
    }
}
package com.nearby.messages.nearbyconnection.arch

import android.util.Log
import com.nearby.messages.nearbyconnection.data.api.LanguageService
import com.nearby.messages.nearbyconnection.data.api.TextTisaneService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

internal object ApiModule {

    internal val textTisaneService by lazy {
        retrofitText.create(TextTisaneService::class.java)
    }

    internal val languageService by lazy {
        retrofitLanguage.create(LanguageService::class.java)
    }

    private val retrofitText by lazy {
        Retrofit.Builder()
                .baseUrl("https://api.tisane.ai/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientText)
                .build()
    }

    private val retrofitLanguage by lazy {
        Retrofit.Builder()
                .baseUrl("https://ws.detectlanguage.com/0.2/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientLanguage)
                .build()
    }

    private val okHttpClientText by lazy {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { message -> Timber.d(message) }
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor { chain ->
                    var request = chain.request().newBuilder()
                            .addHeader("Ocp-Apim-Subscription-Key", "77603b6d103c45ad94fde80b573b452e")
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    private val okHttpClientLanguage by lazy {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { message -> Timber.d(message) }
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor { chain ->
                    var request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ac9dcf7f28e24aa3c0fbd0d90be6ea2b")
                            .build()
                    chain.proceed(request)
                }
                .build()
    }
}
package com.nearby.messages.nearbyconnection.arch

import com.nearby.messages.nearbyconnection.BuildConfig
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
                .baseUrl(BuildConfig.TISANE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientText)
                .build()
    }

    private val okHttpClientText by lazy {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { message -> Timber.d(message) }
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor { chain ->
                    var request = chain.request().newBuilder()
                            .addHeader(TISANE_SUBSCRIPTION_KEY_HEADER, BuildConfig.TISANE_API_KEY)
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    private val retrofitLanguage by lazy {
        Retrofit.Builder()
                .baseUrl(BuildConfig.LANGUAGE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientLanguage)
                .build()
    }

    private val okHttpClientLanguage by lazy {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { message -> Timber.d(message) }
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor { chain ->
                    var request = chain.request().newBuilder()
                            .addHeader(LANGUAGE_AUTH_HEADER, LANGUAGE_AUTH_KEY_PREFIX + BuildConfig.LANGUAGE_API_KEY)
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    private const val TISANE_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key"
    private const val LANGUAGE_AUTH_HEADER = "Authorization"
    private const val LANGUAGE_AUTH_KEY_PREFIX = "Bearer "
}
package com.nearby.messages.nearbyconnection.ext.rx

import io.reactivex.Single

fun List<Long>.wrap(): Single<List<Long>> {
    return Single.fromCallable { this }
}

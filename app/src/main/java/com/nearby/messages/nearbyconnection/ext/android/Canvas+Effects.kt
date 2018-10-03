package com.nearby.messages.nearbyconnection.ext.android

import android.graphics.Canvas

fun Canvas.alter(func: () -> Unit) {
    save()
    func()
    restore()
}

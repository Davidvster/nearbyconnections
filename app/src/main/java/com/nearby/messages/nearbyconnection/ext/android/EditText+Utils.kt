package com.nearby.messages.nearbyconnection.ext.android

import android.widget.EditText

fun EditText.getTextAsString(): String {
    return text.toString()
}

fun EditText.isEmpty(): Boolean {
    return getTextAsString().isEmpty()
}

package com.nearby.messages.nearbyconnection.ext

fun Int.matches(flag: Int): Boolean {
    return (this and flag) == flag
}

fun Long.matches(flag: Long): Boolean {
    return (this and flag) == flag
}

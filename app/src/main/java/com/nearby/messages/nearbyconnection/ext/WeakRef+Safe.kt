package com.nearby.messages.nearbyconnection.ext

import java.lang.ref.WeakReference

/**
 * Usage
 *
 * val actRef = WeakReference<Activity>(someAct)
 *
 * actRef.safe({ act->
 *   //use activity
 * }, {
 *   //activity is null
 * })
 *
 * actRef.safe({
 *   //no handling for when is null
 * })
 */
fun <T> WeakReference<T>.safe(nonNullFunction: T.(T) -> Unit, nullFunction: (() -> Unit)? = null) {
    this.get()?.nonNullFunction(this.get()!!) ?: nullFunction?.invoke()
}

package com.nearby.messages.nearbyconnection.ext.android

import android.app.Activity
import android.app.Fragment
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.graphics.ColorUtils
import android.view.View

fun Activity.setStatusBarColorWithColor(@ColorInt marshmallow: Int, @ColorInt lollipop: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.statusBarColor = marshmallow
        if (ColorUtils.calculateLuminance(marshmallow) > 0.5) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    } else {
        window.statusBarColor = lollipop
    }
}

fun Fragment.setStatusBarColorWithColor(@ColorInt marshmallow: Int, @ColorInt lollipop: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity!!.window.statusBarColor = marshmallow
        if (ColorUtils.calculateLuminance(marshmallow) > 0.5) {
            activity!!.window.decorView.systemUiVisibility = activity!!.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    } else {
        activity!!.window.statusBarColor = lollipop
    }
}

fun Activity.setStatusBarColorById(@ColorRes marshmallow: Int, @ColorRes lollipop: Int) {
    setStatusBarColorWithColor(getCompatColor(marshmallow), getCompatColor(lollipop))
}

fun Fragment.setStatusBarColorById(@ColorRes marshmallow: Int, @ColorRes lollipop: Int) {
    setStatusBarColorWithColor(activity!!.getCompatColor(marshmallow), activity!!.getCompatColor(lollipop))
}

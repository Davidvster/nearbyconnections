package com.nearby.messages.nearbyconnection.ui.views

import android.content.Context
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.AlphaTileView
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R
import com.skydoves.colorpickerview.flag.FlagView

class CustomFlag(context: Context, layout: Int) : FlagView(context, layout) {

    private val textView: TextView = findViewById(R.id.flag_color_code)
    private val alphaTileView: AlphaTileView = findViewById(R.id.flag_color_layout)

    override fun onRefresh(colorEnvelope: ColorEnvelope) {
        textView.text = context.resources.getString(R.string.dialog_color_name, colorEnvelope.hexCode)
        alphaTileView.setPaintColor(colorEnvelope.color)
    }
}
package com.nearby.messages.nearbyconnection.ui.views

import android.content.Context
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.AlphaTileView
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R
import com.skydoves.colorpickerview.flag.FlagView


class CustomFlag(context: Context, layout: Int) : FlagView(context, layout) {

    private val textView: TextView
    private val alphaTileView: AlphaTileView

    init {
        textView = findViewById(R.id.flag_color_code)
        alphaTileView = findViewById(R.id.flag_color_layout)
    }

    override fun onRefresh(colorEnvelope: ColorEnvelope) {
        textView.text = "#" + colorEnvelope.hexCode
        alphaTileView.setPaintColor(colorEnvelope.color)
    }
}
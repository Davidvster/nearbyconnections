package com.nearby.messages.nearbyconnection.ui.views

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.dialog_color_picker.*

class ColorPickDialog(context: Context) : AppCompatDialog(context) {

    var onPositiveClicked: ((ColorPickDialog) -> Unit)? = null
    var onNegativeClicked: ((ColorPickDialog) -> Unit)? = null

    var selectedColor = -1

    lateinit var colorAdapter: ColorGridAdapter

    fun init(): ColorPickDialog {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_color_picker)
        dialog_button_positive.setOnClickListener {
            onPositiveClicked?.invoke(this)
        }
        dialog_button_negative.setOnClickListener {
            onNegativeClicked?.invoke(this)
        }

        colorAdapter = ColorGridAdapter(context)
        colorAdapter.colorList = context.resources.getIntArray(R.array.card_colors).toMutableList()
        dialog_color_list.adapter = colorAdapter
        colorAdapter.onColorClicked = {
            selectedColor = it
            dialog_current_color.setCardBackgroundColor(it)
            dialog_current_color.visibility = View.VISIBLE
        }
        return this
    }

    fun setSelectedColor(color: Int): ColorPickDialog {
        dialog_current_color.setCardBackgroundColor(color)
        return this
    }

    fun setTitleText(@StringRes title: Int): ColorPickDialog {
        dialog_title.text = context.getString(title)
        return this
    }

    fun setTitleText(title: String): ColorPickDialog {
        dialog_title.text = title
        return this
    }

    fun setPositiveButton(@StringRes positiveText: Int, onPositiveClick: ((ColorPickDialog) -> Unit)?): ColorPickDialog {
        dialog_button_positive.visibility = View.VISIBLE
        dialog_button_positive.text = context.getString(positiveText)
        this.onPositiveClicked = onPositiveClick
        return this
    }

    fun setPositiveButton(positiveText: String, onPositiveClick: ((ColorPickDialog) -> Unit)?): ColorPickDialog {
        dialog_button_positive.visibility = View.VISIBLE
        dialog_button_positive.text = positiveText
        this.onPositiveClicked = onPositiveClick
        return this
    }

    fun setNegativeButton(@StringRes negativeText: Int, onNegativeClick: ((ColorPickDialog) -> Unit)?): ColorPickDialog {
        dialog_button_negative.visibility = View.VISIBLE
        dialog_button_negative.text = context.getString(negativeText)
        this.onNegativeClicked = onNegativeClick
        return this
    }

    fun setNegativeButton(negativeText: String, onNegativeClick: ((ColorPickDialog) -> Unit)?): ColorPickDialog {
        dialog_button_negative.visibility = View.VISIBLE
        dialog_button_negative.text = negativeText
        this.onNegativeClicked = onNegativeClick
        return this
    }

}
package com.nearby.messages.nearbyconnection.ui.views

import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.content.Context
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.arch.AppModule

class ColorGridAdapter constructor(var context: Context) : BaseAdapter() {

    var colorList = mutableListOf<Int>()

    var onColorClicked: ((cardColor: Int) -> Unit)? = null

    override fun getCount(): Int {
        return colorList.size
    }

    override fun getItem(position: Int): Int {
        return colorList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.dialog_element_color, viewGroup, false)
        }

        val color = getItem(position)

        // Set the TextView's contents
        val card = view!!.findViewById(R.id.card_color) as CardView
        card.setBackgroundColor(color)

        card.setOnClickListener {
            onColorClicked!!.invoke(color)
        }

        return view
    }
}
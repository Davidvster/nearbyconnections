package com.nearby.messages.nearbyconnection.ui.viewimage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import com.nearby.messages.nearbyconnection.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_image.*

class ViewImageActivity : BaseActivity<ViewImageMvp.Presenter>(), ViewImageMvp.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ViewImagePresenter(this)
        setContentView(R.layout.activity_view_image)

        val uri = Uri.parse( intent.getStringExtra(FILE_URI))
        Picasso.with(this).load(uri).resize(2048,2048).centerInside().into(image_full_screen)
    }

    companion object {
        private const val FILE_URI = "imagePath.string"

        @JvmStatic
        fun start(context: Activity, fileUri: String) {
            val intent = Intent(context, ViewImageActivity::class.java)
            intent.putExtra(FILE_URI, fileUri)
            context.startActivity(intent)
        }
    }

}

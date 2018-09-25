package com.nearby.messages.nearbyconnection.ui.quiz

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.ui.hostchat.HostChatActivity

class QuizActivity : BaseActivity<QuizMvp.Presenter>(), QuizMvp.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = QuizPresenter(this)
        setContentView(R.layout.activity_quiz)
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, HostChatActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}

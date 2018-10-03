package com.nearby.messages.nearbyconnection.ui.views

import android.support.v7.app.AppCompatDialog
import android.view.View
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.Window
import com.nearby.messages.nearbyconnection.R
import kotlinx.android.synthetic.main.dialog_guest_list.*

class GuestListDialog(context: Context) : AppCompatDialog(context) {

    private var onPositiveClicked: ((GuestListDialog) -> Unit)? = null

    private lateinit var guestListAdapter: GuestListAdapter

    fun init(guestList: List<String>): GuestListDialog {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_guest_list)
        dialog_guest_close.setOnClickListener {
            onPositiveClicked?.invoke(this)
        }

        if (guestList.isNotEmpty()){
            dialog_guest_participants.visibility = View.VISIBLE
            guestListAdapter = GuestListAdapter(context)
            guestListAdapter.guestList = guestList
            dialog_guest_list.layoutManager = LinearLayoutManager(context)
            dialog_guest_list.adapter = guestListAdapter
        }
        return this
    }

    fun setMainLanguage(language: String): GuestListDialog {
        if (language.isNotEmpty()) {
            dialog_language.visibility = View.VISIBLE
            dialog_language.text = context.resources.getString(R.string.chat_main_language_title, language)
        }
//        if (languages.isNotEmpty()) {
//            dialog_language.visibility = View.VISIBLE
//            val languageList = StringBuilder()
//            for (language in languages.subList(0, languages.size-1)) {
//                languageList.append("$language; ")
//            }
//            languageList.append(languages[languages.size-1])
//        dialog_language.text = context.resources.getString(R.string.chat_main_language_title, languageList)
//        }
        return this
    }

    fun setMainTopic(topics: List<String>): GuestListDialog {
        if (topics.isNotEmpty()) {
            dialog_topic.visibility = View.VISIBLE
            val topicList = StringBuilder()
            for (topic in topics.subList(0, topics.size-1)) {
                topicList.append("$topic; ")
            }
            topicList.append(topics[topics.size-1])
            dialog_topic.text = context.resources.getString(R.string.chat_main_topic_title, topicList)
        }
        return this
    }

    fun setTitleText(title: String): GuestListDialog {
        dialog_guest_owner.text = title
        return this
    }

    fun setPositiveButton( onPositiveClick: ((GuestListDialog) -> Unit)?): GuestListDialog {
        dialog_guest_close.visibility = View.VISIBLE
        this.onPositiveClicked = onPositiveClick
        return this
    }
}
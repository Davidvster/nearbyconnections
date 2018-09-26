package com.nearby.messages.nearbyconnection.ui.hostquiz

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import com.nearby.messages.nearbyconnection.ui.quiz.QuizAdapter
import com.nearby.messages.nearbyconnection.ui.views.GuestListDialog
import kotlinx.android.synthetic.main.activity_host_quiz.*
class HostQuizActivity : BaseActivity<HostQuizMvp.Presenter>(), HostQuizMvp.View {

    private lateinit var username: String
    private var cardColor = -1

    private var correctAnswer = 1

    private lateinit var quizAdapter: QuizAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = HostQuizPresenter(this)
        setContentView(R.layout.activity_host_quiz)

        handleRadioButtons()
        handleEditTexts()

        setSupportActionBar(host_quiz_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        username = intent.getStringExtra(ARG_MY_USER_NAME)
        cardColor = intent.getIntExtra(ARG_CARD_BACKGROUND_COLOR, -1)

        presenter.init(username, packageName, cardColor)
        presenter.startAdvertising()

        quiz_send.setOnClickListener {
            if (checkQuizInput()) {
                val question = QuizQuestion(quiz_input_question.text.toString(), quiz_input_answer_a.text.toString(), quiz_input_answer_b.text.toString(), quiz_input_answer_c.text.toString(), quiz_input_answer_d.text.toString())
                presenter.sendQuestion(question, correctAnswer)
                quiz_input_question.text = null
                quiz_input_answer_a.text = null
                quiz_input_answer_b.text = null
                quiz_input_answer_c.text = null
                quiz_input_answer_d.text = null
                correctAnswer = 1
                quiz_radio_answer_a.isChecked = true
                quiz_radio_answer_b.isChecked = false
                quiz_radio_answer_c.isChecked = false
                quiz_radio_answer_d.isChecked = false
            }
        }

        quizAdapter = QuizAdapter(this)
        quiz_content.layoutManager = LinearLayoutManager(this)
        quiz_content.adapter = quizAdapter
    }

    override fun updateQuizResult(resultList: MutableList<QuizResult>) {
        runOnUiThread {
            quizAdapter.resultList = resultList
            quizAdapter.notifyItemInserted(resultList.size-1)
            quiz_content.scrollToPosition(resultList.size -1)
        }
    }

    override fun showConnectionDialog(user: String, endpointId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Connection found")
        builder.setMessage(user+ " wants to connect to you!")
        builder.setPositiveButton("Accept") { dialog, which ->
            presenter.acceptConnection(user, endpointId)
        }
        builder.setNegativeButton("Reject") { dialog, which ->
            presenter.rejectConnection(endpointId)
        }
        builder.setOnDismissListener { presenter.rejectConnection(endpointId) }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkQuizInput(): Boolean {
        var allInserted = true
        if(checkInputEmpty(quiz_input_question) || checkInputEmpty(quiz_input_answer_a) || checkInputEmpty(quiz_input_answer_b) || checkInputEmpty(quiz_input_answer_c) || checkInputEmpty(quiz_input_answer_d)) {
            allInserted = false
        }
        return allInserted
    }

    private fun checkInputEmpty(editText: EditText): Boolean {
        if (editText.text.toString().replace("/^\\s*/".toRegex(), "").isNullOrEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                editText.backgroundTintList = ColorStateList.valueOf(Color.RED)
            }
            return true
        }
        return false
    }

    private fun handleRadioButtons() {
        quiz_radio_answer_a.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                correctAnswer = 1
                quiz_radio_answer_b.isChecked = false
                quiz_radio_answer_c.isChecked = false
                quiz_radio_answer_d.isChecked = false
            }
        }
        quiz_radio_answer_b.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                correctAnswer = 2
                quiz_radio_answer_a.isChecked = false
                quiz_radio_answer_c.isChecked = false
                quiz_radio_answer_d.isChecked = false
            }
        }
        quiz_radio_answer_c.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                correctAnswer = 3
                quiz_radio_answer_b.isChecked = false
                quiz_radio_answer_a.isChecked = false
                quiz_radio_answer_d.isChecked = false
            }
        }
        quiz_radio_answer_d.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                correctAnswer = 4
                quiz_radio_answer_b.isChecked = false
                quiz_radio_answer_c.isChecked = false
                quiz_radio_answer_a.isChecked = false
            }
        }
    }

    private fun handleEditTexts() {
        handleEditText(quiz_input_question)
        handleEditText(quiz_input_answer_a)
        handleEditText(quiz_input_answer_b)
        handleEditText(quiz_input_answer_c)
        handleEditText(quiz_input_answer_d)
    }

    private fun handleEditText(editText: EditText) {
        val defaultEditColor = Color.GRAY
        val accentEditColor = ContextCompat.getColor(this, R.color.color_accent)
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            editText.backgroundTintList = ColorStateList.valueOf(defaultEditColor)
            editText.setOnClickListener {
                if (it.isFocused) {
                    editText.backgroundTintList = ColorStateList.valueOf(accentEditColor)
                } else {
                    editText.backgroundTintList = ColorStateList.valueOf(defaultEditColor)
                }
            }

            editText.setOnFocusChangeListener { _, focused ->
                if (focused) {
                    editText.backgroundTintList = ColorStateList.valueOf(accentEditColor)
                } else {
                    editText.backgroundTintList = ColorStateList.valueOf(defaultEditColor)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_guests, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
            R.id.guests_list -> {
                if (presenter.getGuestList().isNotEmpty()) {
                    GuestListDialog(this).init(presenter.getGuestList())
                            .setPositiveButton { dialog ->
                                dialog.dismiss()
                            }
                            .setTitleText("Hosting a Quiz-Room")
                            .show()
                }
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopAdvertising()
        presenter.stopAllConnections()
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, HostQuizActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}

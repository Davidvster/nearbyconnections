package com.nearby.messages.nearbyconnection.ui.quiz

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import com.nearby.messages.nearbyconnection.ui.chat.ConnectionAdapter
import kotlinx.android.synthetic.main.activity_quiz.*

class QuizActivity : BaseActivity<QuizMvp.Presenter>(), QuizMvp.View {

    lateinit var username: String
    private var cardColor: Int = -1

    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var quizAdapter: QuizAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = QuizPresenter(this)
        setContentView(R.layout.activity_quiz)
        
        title = "Connect to a Quiz Room"
        setSupportActionBar(quiz_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        username = intent.getStringExtra(ARG_MY_USER_NAME)
        cardColor = intent.getIntExtra(ARG_CARD_BACKGROUND_COLOR, -1)

        presenter.init(username, packageName, cardColor)
        presenter.startDiscovery()

        connectionAdapter = ConnectionAdapter(this)
        connection_content.layoutManager = LinearLayoutManager(this)
        connection_content.adapter = connectionAdapter

        connectionAdapter.onRoomClicked =  {
            setProgressVisible(true)
            presenter.requestConnection(it)
        }

        quizAdapter = QuizAdapter(this)
        quiz_content.layoutManager = LinearLayoutManager(this)
        quiz_content.adapter = quizAdapter

        quiz_answer_a.setOnClickListener {
            presenter.sendAnswer(1)
            quiz_answer_layout.visibility = View.GONE
        }
        quiz_answer_b.setOnClickListener {
            presenter.sendAnswer(2)
            quiz_answer_layout.visibility = View.GONE
        }
        quiz_answer_c.setOnClickListener {
            presenter.sendAnswer(3)
            quiz_answer_layout.visibility = View.GONE
        }
        quiz_answer_d.setOnClickListener {
            presenter.sendAnswer(4)
            quiz_answer_layout.visibility = View.GONE
        }
    }

    override fun setToolbarTitle(newTitle: String) {
        supportActionBar!!.title = newTitle
    }

    override fun setQuizRoom() {
        quiz_room_layout.visibility = View.VISIBLE
        connection_layout.visibility = View.GONE
        connectionAdapter.connectionList = mutableListOf()
    }

    override fun setConnectionRoom() {
        setProgressVisible(false)
        quiz_room_layout.visibility = View.GONE
        connection_layout.visibility = View.VISIBLE
        quizAdapter.resultList = mutableListOf()
        quizAdapter.notifyDataSetChanged()
        supportActionBar!!.title = "Connect to a Quiz Room"
        presenter.startDiscovery()
    }

    override fun setQuestion(question: QuizQuestion) {
        quiz_question.text = question.question
        quiz_answer_a.text = question.answerA
        quiz_answer_b.text = question.answerB
        quiz_answer_c.text = question.answerC
        quiz_answer_d.text = question.answerD
        quiz_answer_layout.visibility = View.VISIBLE
    }

    override fun setProgressVisible(visible: Boolean) {
        if (visible) {
            connection_progress.visibility = View.VISIBLE
        } else {
            connection_progress.visibility = View.GONE
        }
    }

    override fun updateQuizResult(resultList: MutableList<QuizResult>) {
        quizAdapter.resultList = resultList
        quizAdapter.notifyItemInserted(resultList.size-1)
        quiz_answer_layout.visibility = View.GONE
        quiz_content.scrollToPosition(resultList.size -1)
    }

    override fun updateConnectionList(availableRooms: MutableList<Pair<String, String>>) {
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyItemInserted(availableRooms.size-1)
    }

    override fun setParticipantsList(guestNames: List<String>) {
        quiz_guest_name.text = "Playing with: " + guestNames
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (presenter.isConnected()) {
                    presenter.stopDiscovery()
                    presenter.stopAllConnections()
                    setConnectionRoom()
                } else {
                    this.finish()
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (presenter.isConnected()) {
            presenter.stopDiscovery()
            presenter.stopAllConnections()
            setConnectionRoom()
        } else {
            this.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopDiscovery()
        presenter.stopAllConnections()
    }

    companion object {
        private val ARG_MY_USER_NAME = "username.string"
        private val ARG_CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, QuizActivity::class.java)
            intent.putExtra(ARG_MY_USER_NAME, username)
            intent.putExtra(ARG_CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}

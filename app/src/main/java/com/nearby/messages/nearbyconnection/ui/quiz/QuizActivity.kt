package com.nearby.messages.nearbyconnection.ui.quiz

import android.app.Activity
import android.content.Intent
import com.nearby.messages.nearbyconnection.arch.BaseActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import com.nearby.messages.nearbyconnection.ui.chat.ConnectionAdapter
import com.nearby.messages.nearbyconnection.ui.views.GuestListDialog
import kotlinx.android.synthetic.main.activity_quiz.*
import android.animation.ValueAnimator
import android.support.design.widget.Snackbar
import android.view.animation.LinearInterpolator

class QuizActivity : BaseActivity<QuizMvp.Presenter>(), QuizMvp.View {

    private lateinit var username: String
    private var cardColor: Int = -1

    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var guestListMenu: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = QuizPresenter(this)
        setContentView(R.layout.activity_quiz)
        
        title = resources.getString(R.string.quiz_connect_room_title)
        setSupportActionBar(quiz_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        username = intent.getStringExtra(MY_USER_NAME)
        cardColor = intent.getIntExtra(CARD_BACKGROUND_COLOR, -1)

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
            submitQuestion(1)
        }
        quiz_answer_b.setOnClickListener {
            submitQuestion(2)
        }
        quiz_answer_c.setOnClickListener {
            submitQuestion(3)
        }
        quiz_answer_d.setOnClickListener {
            submitQuestion(4)
        }

        connection_content_refresh.setOnRefreshListener {
            connection_content_refresh.isRefreshing = true
            presenter.refreshConnectionList()
        }
    }

    private fun submitQuestion(response: Int) {
        presenter.sendAnswer(response)
        quiz_answer_layout.visibility = View.GONE
        quiz_answer_waiting.text = resources.getString(R.string.quiz_answer_responded)
        quiz_answer_waiting.visibility = View.VISIBLE
    }

    override fun stopRefreshConnectionList() {
        connection_content_refresh.isRefreshing = false
        quiz_connect_search.visibility = View.VISIBLE
        presenter.startDiscovery()
    }

    override fun setToolbarTitle(newTitle: String) {
        supportActionBar!!.title = newTitle
    }

    override fun setQuizRoom() {
        quiz_room_layout.visibility = View.VISIBLE
        connection_layout.visibility = View.GONE
        connectionAdapter.connectionList = mutableListOf()
        guestListMenu.isVisible = true
    }

    override fun setConnectionRoom() {
        setProgressVisible(false)
        quiz_room_layout.visibility = View.GONE
        connection_layout.visibility = View.VISIBLE
        quiz_connect_search.visibility = View.VISIBLE
        quizAdapter.resultList = mutableListOf()
        quizAdapter.notifyDataSetChanged()
        supportActionBar!!.title = resources.getString(R.string.quiz_connect_room_title)
        presenter.startDiscovery()
        guestListMenu.isVisible = false
        Snackbar.make(connection_layout, resources.getString(R.string.connection_ended), Snackbar.LENGTH_SHORT).show()
    }

    override fun setQuestion(question: QuizQuestion) {
        quiz_answer_waiting.text = resources.getString(R.string.quiz_answer_waiting)
        quiz_answer_waiting.visibility = View.GONE
        quiz_question.text = question.question
        quiz_answer_a.text = question.answerA
        quiz_answer_b.text = question.answerB
        quiz_answer_c.text = question.answerC
        quiz_answer_d.text = question.answerD
        quiz_answer_layout.visibility = View.VISIBLE
        quiz_timer_layout.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(question.durationSec.toInt(), 0)
        animator.interpolator = LinearInterpolator()
        try {
            ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType).invoke(null, 1f)
        } catch (t: Throwable) {
        }
        animator.duration = question.durationSec * 1000
        animator.addUpdateListener { animation ->
            quiz_timer.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    override fun setProgressVisible(visible: Boolean) {
        if (visible) {
            connection_progress.visibility = View.VISIBLE
            connectionAdapter.isClickable = false
        } else {
            connection_progress.visibility = View.GONE
            connectionAdapter.isClickable = true
        }
    }

    override fun updateQuizResult(resultList: MutableList<QuizResult>) {
        quizAdapter.resultList = resultList
        quizAdapter.notifyItemInserted(resultList.size-1)
        quiz_answer_waiting.text = resources.getString(R.string.quiz_answer_waiting)
        quiz_answer_waiting.visibility = View.VISIBLE
        quiz_answer_layout.visibility = View.GONE
        quiz_timer_layout.visibility = View.GONE
        quiz_content.scrollToPosition(resultList.size -1)
    }

    override fun updateConnectionList(availableRooms: List<Pair<String, String>>) {
        if (availableRooms.isEmpty()) {
            quiz_connect_search.visibility = View.VISIBLE
        } else {
            quiz_connect_search.visibility = View.GONE
        }
        connectionAdapter.connectionList = availableRooms
        connectionAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_guests, menu)
        guestListMenu = menu.findItem(R.id.guests_list)
        guestListMenu.isVisible = false
        return true
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
            R.id.guests_list -> {
                if (presenter.getGuestList().isNotEmpty()) {
                    GuestListDialog(this).init(presenter.getGuestList())
                            .setPositiveButton { dialog ->
                                dialog.dismiss()
                            }
                            .setTitleText(resources.getString(R.string.quiz_guest_list_room_host, presenter.getHostUsername()))
                            .show()
                } else{
                    Toast.makeText(this, resources.getString(R.string.guest_list_only_two_participants), Toast.LENGTH_LONG).show()
                }
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
        private const val MY_USER_NAME = "username.string"
        private const val CARD_BACKGROUND_COLOR = "color.integer"

        @JvmStatic
        fun start(context: Activity, username: String, cardColor: Int) {
            val intent = Intent(context, QuizActivity::class.java)
            intent.putExtra(MY_USER_NAME, username)
            intent.putExtra(CARD_BACKGROUND_COLOR, cardColor)
            context.startActivity(intent)
        }
    }
}
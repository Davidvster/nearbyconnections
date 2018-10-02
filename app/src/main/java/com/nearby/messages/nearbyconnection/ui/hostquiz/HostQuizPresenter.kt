package com.nearby.messages.nearbyconnection.ui.hostquiz

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.nearby.messages.nearbyconnection.BuildConfig
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.Guest
import com.nearby.messages.nearbyconnection.data.model.Participant
import com.nearby.messages.nearbyconnection.data.model.QuizGuestRequest
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResponse
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class HostQuizPresenter constructor(hostQuizView: HostQuizMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostQuizMvp.View>(hostQuizView), HostQuizMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guests = mutableListOf<Guest>()
    private var currentQuizResponses = mutableListOf<QuizResponse>()
    private var resultList = mutableListOf<QuizResult>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1
    private var correctAnswer = 0

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val quizResponse = Gson().fromJson(String(payload.asBytes()!!), QuizResponse::class.java)
            if (quizResponse.timeTaken != null && quizResponse.response != null) {
                quizResponse.endpointId = endpointId
                currentQuizResponses.add(quizResponse)
                if (currentQuizResponses.size == guests.size) {
                    endOfQuiz(resultList.size)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            val quizRequest = Gson().fromJson(connectionInfo.endpointName, QuizGuestRequest::class.java)
            view?.showJoinDialog(quizRequest, endpointId)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    sendParticipants()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                }
                else -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            guests.remove(guests.find { it.endpointId == endpointId })
            sendParticipants()
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName + BuildConfig.QUIZ_ID
        this.cardColor = cardColor
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }

    override fun startAdvertising() {
        connectionsClient.startAdvertising(
                username, packageName, connectionLifecycleCallback, AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build())
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    private fun sendParticipants() {
        for (guest in guests) {
            val tmpGuestNames = guests.toMutableList()
            tmpGuestNames.remove(guest)
            val message = Gson().toJson(Participant(tmpGuestNames.map { it.username}))
            connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
        }
    }

    override fun acceptConnection(user: QuizGuestRequest, endpointId: String) {
        val newGuest = Guest(endpointId, user.username)
        newGuest.cardColor = user.cardColor
        guests.add(newGuest)
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }

    private fun sendMessage(endpointIds: List<String>, message: String, endpointId: String? = null) {
        connectionsClient.sendPayload(endpointIds.filter { it != endpointId }, Payload.fromBytes(message.toByteArray()))
    }

    override fun sendQuestion(question: QuizQuestion, correctAnswer: Int) {
        this.correctAnswer = correctAnswer
        val message = Gson().toJson(question)
        connectionsClient.sendPayload(guests.map { it.endpointId }, Payload.fromBytes(message.toByteArray()))
        view?.enableQuizForm(false)
        val currentRound  = resultList.size
        Timer().schedule(timerTask {
            endOfQuiz(currentRound)
        }, 60000)
    }

    private fun endOfQuiz(round: Int) {
        if (round == resultList.size) {
            var winnerResponse: QuizResponse? = null
            var minTime = Long.MAX_VALUE
            for (response in currentQuizResponses) {
                if (response.response == correctAnswer) {
                    guests.find { it.endpointId == response.endpointId }!!.points += 60000 - response.timeTaken
                    if (response.timeTaken < minTime) {
                        winnerResponse = response
                        minTime = response.timeTaken
                    }
                }
            }
            if (winnerResponse != null) {
                val cal = Calendar.getInstance()
                cal.time = Date(winnerResponse.timeTaken)
                val seconds =  cal.get(Calendar.SECOND)
                currentQuizResponses = mutableListOf()
                val winner = guests.find { it.endpointId == winnerResponse.endpointId }
                var quizResult = QuizResult(context.resources.getString(R.string.quiz_winner_text_others, winner!!.username, seconds.toString(), context.resources.getQuantityString(R.plurals.seconds, seconds)), winner.cardColor!!, guests.sortedByDescending { it.points })
                sendMessage(guests.map { it.endpointId }, Gson().toJson(quizResult), winnerResponse.endpointId)
                resultList.add(quizResult)
                view?.updateQuizResult(resultList)

                quizResult = QuizResult(context.resources.getString(R.string.quiz_winner_text_winner, seconds.toString(), context.resources.getQuantityString(R.plurals.seconds, seconds)), winner.cardColor!!, guests.sortedByDescending { it.points })
                sendMessage(listOf(winnerResponse.endpointId), Gson().toJson(quizResult))
            } else {
                var quizResult = QuizResult(context.resources.getString(R.string.quiz_winner_text_no_winner), cardColor, guests.sortedByDescending { it.points })
                sendMessage(guests.map { it.endpointId }, Gson().toJson(quizResult))
                resultList.add(quizResult)
                view?.updateQuizResult(resultList)
            }
            currentQuizResponses = mutableListOf()
            view?.enableQuizForm(true)
        }
    }

    override fun getGuestList(): List<String> {
        return guests.map { it.username }
    }
}
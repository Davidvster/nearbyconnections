package com.nearby.messages.nearbyconnection.ui.hostquiz

import android.content.Context
import android.support.annotation.UiThread
import android.util.Log
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
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.Guest
import com.nearby.messages.nearbyconnection.data.model.Participant
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResponse
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class HostQuizPresenter constructor(hostQuizView: HostQuizMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostQuizMvp.View>(hostQuizView), HostQuizMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guestsEndpointId = mutableListOf<String>()
    private var guestNames = HashMap<String, String>()
    private var guests = mutableListOf<Guest>()
    private var currentQuizResponses = mutableListOf<QuizResponse>()
    private var resultList = mutableListOf<QuizResult>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1
    private var correctAnswer = 0
    private var quizEnded = true

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))
            val quizResponse = Gson().fromJson(String(payload.asBytes()!!), QuizResponse::class.java)
            if (quizResponse.timeTaken != null && quizResponse.response != null) {
                quizResponse.endpointId = endpointId
                currentQuizResponses.add(quizResponse)
                if (currentQuizResponses.size == guestsEndpointId.size) {
                    endOfQuiz()
//                    lateinit var winnerResponse: QuizResponse
//                    var minTime = Long.MAX_VALUE
//                    for (response in currentQuizResponses) {
//                        if (response.response == correctAnswer && response.timeTaken < minTime) {
//                            winnerResponse = response
////                                quizResult = QuizResult(guestNames[response.endpointId]!! + " responded in "+ Date(timeDiff).toString())
//                            minTime = response.timeTaken
//                        }
//                    }
//                    val cal = Calendar.getInstance()
//                    cal.time = Date(winnerResponse.timeTaken)
//                    val seconds =  cal.get(Calendar.SECOND)
//                    currentQuizResponses = mutableListOf()
//                    var quizResult = QuizResult(guestNames[winnerResponse.endpointId]!! + " responded in $seconds second/s")
//                    sendMessage(guestsEndpointId.toList(), Gson().toJson(quizResult), winnerResponse.endpointId)
//                    resultList.add(quizResult)
//                    view?.updateQuizResult(resultList)
//
//                    quizResult = QuizResult("Congratulations you won, you responded in $seconds second/s")
//                    sendMessage(listOf(winnerResponse.endpointId), Gson().toJson(quizResult))
                }
//                if (quizResponse.response == correctAnswer) {
//                    val quizResult = QuizResult(guestNames[endpointId]!!)
////                    sendMessage(listOf(endpointId), Gson().toJson(quizResult)) //winner
//                    sendMessage(guestsEndpointId.toList(), Gson().toJson(quizResult))
//                    resultList.add(quizResult)
//                    view?.updateQuizResult(resultList)
//                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            view?.showConnectionDialog(connectionInfo.endpointName, endpointId)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    guestsEndpointId.add(endpointId)
                    view?.setParticipantsTitle(guestNames.values.toList())
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
                    sendParticipants(guestNames)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    guestNames.remove(endpointId)
                    Log.v("SOGOVOREC2", "We're connected! Can now start sending and receiving data. " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    guestNames.remove(endpointId)
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
            guestsEndpointId.remove(endpointId)
            guestNames.remove(endpointId)
            view?.setParticipantsTitle(guestNames.values.toList())
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName + ".quiz"
        this.cardColor = cardColor
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }


    override fun startAdvertising() {
        Log.v("POVEZAVA", "started advertising")
        connectionsClient.startAdvertising(
                username, packageName, connectionLifecycleCallback, AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    override fun stopAdvertising() {
        Log.v("POVEZAVA", "stopped advertising")
        connectionsClient.stopAdvertising()
    }

    private fun sendParticipants(guestNames: HashMap<String, String>) {
        for (guest in guestsEndpointId) {
            val tmpGuestNames = guestNames.clone() as HashMap<String, String>
            tmpGuestNames.remove(guest)
            tmpGuestNames["username"] = username
            val message = Gson().toJson(Participant(tmpGuestNames.values.toList()))
            connectionsClient.sendPayload(guest, Payload.fromBytes(message.toByteArray()))
        }
    }

    override fun acceptConnection(user: String, endpointId: String) {
        guestNames[endpointId] = user
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }

    private fun sendMessage(endpointIds: List<String>, message: String, endpointId: String? = null) {
        for (guest in endpointIds) {
            if (guest != endpointId) {
                connectionsClient.sendPayload(guest, Payload.fromBytes(message.toByteArray()))
            }
        }
    }

    override fun sendQuestion(question: QuizQuestion, correctAnswer: Int) {
        this.correctAnswer = correctAnswer
        quizEnded = false
        for (guest in guestsEndpointId) {
            val message = Gson().toJson(question)
            connectionsClient.sendPayload(guest, Payload.fromBytes(message.toByteArray()))
        }
        Timer().schedule(timerTask { endOfQuiz() }, 60000)
    }

    private fun endOfQuiz() {
        if (quizEnded.not()) {
            quizEnded = true
            var winnerResponse: QuizResponse? = null
            var minTime = Long.MAX_VALUE
            for (response in currentQuizResponses) {
                if (response.response == correctAnswer && response.timeTaken < minTime) {
                    winnerResponse = response
//                                quizResult = QuizResult(guestNames[response.endpointId]!! + " responded in "+ Date(timeDiff).toString())
                    minTime = response.timeTaken
                }
            }
            if (winnerResponse != null) {
                val cal = Calendar.getInstance()
                cal.time = Date(winnerResponse.timeTaken)
                val seconds =  cal.get(Calendar.SECOND)
                currentQuizResponses = mutableListOf()
                var quizResult = QuizResult(guestNames[winnerResponse.endpointId]!! + " responded in $seconds second/s!")
                sendMessage(guestsEndpointId.toList(), Gson().toJson(quizResult), winnerResponse.endpointId)
                resultList.add(quizResult)
                view?.updateQuizResult(resultList)

                quizResult = QuizResult("Congratulations you won, you responded in $seconds second/s!")
                sendMessage(listOf(winnerResponse.endpointId), Gson().toJson(quizResult))
            } else {
                var quizResult = QuizResult("There are no winners for this round!")
                sendMessage(guestsEndpointId.toList(), Gson().toJson(quizResult))
                resultList.add(quizResult)
                view?.updateQuizResultUIthread(resultList)
            }
        }
    }

}
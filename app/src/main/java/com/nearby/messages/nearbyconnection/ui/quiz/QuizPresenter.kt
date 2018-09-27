package com.nearby.messages.nearbyconnection.ui.quiz

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.nearby.messages.nearbyconnection.BuildConfig
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.Participant
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResponse
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import java.util.Date

class QuizPresenter constructor(quizView: QuizMvp.View, private val context: Context = AppModule.application) : BasePresenter<QuizMvp.View>(quizView), QuizMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var hostEndpointId = ""
    private var availableGuests = HashMap<String, String>()
    private var guestList = listOf<String>()
    private var resultList = mutableListOf<QuizResult>()
    private lateinit var dateReceived: Date

    private var connectingTo = ""

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private var connected = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val question = Gson().fromJson(String(payload.asBytes()!!), QuizQuestion::class.java)
            if (question.question != null && question.answerA != null && question.answerB != null && question.answerC != null && question.answerD != null) {
                view?.setQuestion(question)
                dateReceived = Date()
            } else {
                val result = Gson().fromJson(String(payload.asBytes()!!), QuizResult::class.java)
                if (result.winnerName != null) {
                    resultList.add(result)
                    view?.updateQuizResult(resultList)
                } else {
                    val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
                    if (guests.participants != null) {
                        guestList = guests.participants
                    }
                }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            if (!connected) {
                availableGuests[endpointId] = discoveredEndpointInfo.endpointName
                view?.updateConnectionList(availableGuests.toMutableMap().toList().toMutableList())
            }
        }

        override fun onEndpointLost(endpointId: String) {
            availableGuests.remove(endpointId)
            view?.updateConnectionList(availableGuests.toMutableMap().toList().toMutableList())
            if (connectingTo == endpointId) {
                view?.setProgressVisible(false)
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(
                endpointId: String, connectionInfo: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    stopDiscovery()
                    hostEndpointId = endpointId
                    connected = true
                    view?.setToolbarTitle(context.resources.getString(R.string.quiz_room_title, availableGuests[endpointId]!!))
                    view?.setQuizRoom()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    connected = false
                    view?.setProgressVisible(false)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    connected = false
                    view?.setProgressVisible(false)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            availableGuests = HashMap()
            hostEndpointId = ""
            connected = false
            resultList = mutableListOf()
            view?.setConnectionRoom()
        }
    }

    override fun init(username: String, packageName: String, colorCard: Int) {
        this.username = username
        this.packageName = packageName + BuildConfig.QUIZ_ID
        this.cardColor = colorCard
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun sendAnswer(response: Int) {
        val quizResponse = QuizResponse(response, Date().time - dateReceived.time)
        val dataToSend = Gson().toJson(quizResponse)
        connectionsClient.sendPayload(hostEndpointId, Payload.fromBytes(dataToSend.toByteArray()))
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    override fun startDiscovery() {
        connectionsClient.startDiscovery(
                packageName, endpointDiscoveryCallback, DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build())
    }

    override fun stopAllConnections() {
        connected = false
        connectionsClient.stopAllEndpoints()
    }

    override fun acceptConnection(user: String, endpointId: String) {
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun getAvailableGuests(): HashMap<String, String>{
        return availableGuests
    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun requestConnection(endpointId: String) {
        if (availableGuests[endpointId] != null) {
            connectingTo = endpointId
            connectionsClient.requestConnection(username, endpointId, connectionLifecycleCallback)
        }
    }

    override fun getGuestList(): List<String> {
        return guestList
    }

    override fun getHostUsername(): String {
        return availableGuests[hostEndpointId]?: ""
    }
}
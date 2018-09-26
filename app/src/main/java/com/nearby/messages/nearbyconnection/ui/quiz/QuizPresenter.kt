package com.nearby.messages.nearbyconnection.ui.quiz

import android.content.Context
import android.util.Log
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
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.Participant
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResponse
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import java.util.Date

class QuizPresenter constructor(quizView: QuizMvp.View, private val context: Context = AppModule.application) : BasePresenter<QuizMvp.View>(quizView), QuizMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var opponentEndpointId = ""
    private var availableGuests = HashMap<String, String>()
    private var resultList = mutableListOf<QuizResult>()
    private lateinit var dateReceived: Date

    private var connectingTo = ""

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private var connected = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))
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
                        view?.setParticipantsList(guests.participants)
                    }
                }
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.v("SOGOVOREC", "An endpoint was found: " + endpointId)
            if (!connected) {
                availableGuests[endpointId] = discoveredEndpointInfo.endpointName + " QuizRoom"
                view?.updateConnectionList(availableGuests.toMutableMap().toList().toMutableList())
            }
        }

        override fun onEndpointLost(endpointId: String) {
            availableGuests.remove(endpointId)
            view?.updateConnectionList(availableGuests.toMutableMap().toList().toMutableList())
            Log.v("SOGOVOREC", "A previously discovered endpoint has gone away. " + endpointId)
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
                    opponentEndpointId = endpointId
                    connected = true
                    view?.setToolbarTitle(availableGuests[endpointId]!!)
                    view?.setQuizRoom()
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    connected = false
                    view?.setProgressVisible(false)
                    Log.v("SOGOVOREC2", "We're rejected by " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    connected = false
                    view?.setProgressVisible(false)
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
            availableGuests = HashMap()
            opponentEndpointId = ""
            connected = false
            view?.setConnectionRoom()
        }
    }

    override fun init(username: String, packageName: String, colorCard: Int) {
        this.username = username
        this.packageName = packageName +".quiz"
        this.cardColor = colorCard
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun sendAnswer(response: Int) {
        val quizResponse = QuizResponse(response, Date().time - dateReceived.time)
        val dataToSend = Gson().toJson(quizResponse)
        connectionsClient.sendPayload(opponentEndpointId, Payload.fromBytes(dataToSend.toByteArray()))
//        }
    }

    override fun stopDiscovery() {
        Log.v("POVEZAVA", "stopped discovery")
        connectionsClient.stopDiscovery()
    }

    override fun startDiscovery() {
        Log.v("POVEZAVA", "started discovery")
        connectionsClient.startDiscovery(
                packageName, endpointDiscoveryCallback, DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
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

}
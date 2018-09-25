package com.nearby.messages.nearbyconnection.ui.chat

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
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.data.model.Participant
import java.util.Date

class ChatPresenter constructor(chatView: ChatMvp.View, private val context: Context = AppModule.application) : BasePresenter<ChatMvp.View>(chatView), ChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var opponentEndpointId = ""
    private var guestNames = HashMap<String, String>()
    private var avaibleGuests = HashMap<String, String>()
//    private var messageList = mutableListOf<Pair<Pair<String, String>, Int>>()
    private var messageList = mutableListOf<Pair<ChatMessage, Int>>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private var connected = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))
            val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
            if (chatMessage.user != null && chatMessage.date != null && chatMessage.message != null && chatMessage.color != null) {
                addMessage(Pair(chatMessage, 2))
            } else {
                val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
                if (guests.participants != null) {
                    view?.setChattingTitle(guests.participants)
                }
            }

//            try {
//                val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
//                if (chatMessage.user != null && chatMessage.date != null && chatMessage.message != null && chatMessage.color != null) {
//                    addMessage(Pair(chatMessage, 2))
//                } else {
//                    try {
//                        val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
//                        if (guests.participants != null) {
//                            view?.setChattingTitle(guests.participants)
//                        }
//                    }
//                    catch (e: Exception) {
//                    }
//                }
//            }
//            catch (e: Exception) {
//                try {
//                    val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
//                    if (guests.participants != null) {
//                        view?.setChattingTitle(guests.participants)
//                    }
//                }
//                catch (e: Exception) {
//                }
//            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
//            if (update.status == PayloadTransferUpdate.Status.SUCCESS && myChoice != null && opponentChoice != null) {
//                finishRound()
//            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.v("SOGOVOREC", "An endpoint was found: " + endpointId)
            if (!connected) {
                avaibleGuests[endpointId] = discoveredEndpointInfo.endpointName + " ChatRoom"
                view?.updateConnectionList(avaibleGuests.toMutableMap().toList().toMutableList())
            }
//            view?.showAvaibleDevicesDialog(avaibleGuests)
//            connectionsClient.requestConnection(ownerUsername, endpointId, connectionLifecycleCallback)
            // An endpoint was found!
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
            avaibleGuests.remove(endpointId)
            view?.updateConnectionList(avaibleGuests.toMutableMap().toList().toMutableList())
            Log.v("SOGOVOREC", "A previously discovered endpoint has gone away. " + endpointId)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(
                endpointId: String, connectionInfo: ConnectionInfo) {
//            view?.showConnectionDialog(connectionInfo.endpointName, endpointId)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
//                    avaibleGuests[endpointId] = "connected"
//                    stopAdvertising()
                    stopDiscovery()
                    opponentEndpointId = endpointId
                    connected = true
                    view?.setToolbarTitle(avaibleGuests[endpointId]!!)
                    view?.setChatRoom()
//                    view?.setChattiningTitle(guestNames)
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
//                    view?.setConnected()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
//                    avaibleGuests[endpointId] = "rejeceted"
//                    stopAdvertising()
//                    stopDiscovery()
                    guestNames.remove(endpointId)
                    connected = false
                    view?.setProgressVisible(false)
                    Log.v("SOGOVOREC2", "We're rejected by " + endpointId)
//                    if (opponentEndpointId.size < 1) {
////                        view?.setDisconnected()
//                    }
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
//                    avaibleGuests[endpointId] = "error"
                    guestNames.remove(endpointId)
//                    stopAdvertising()
//                    stopDiscovery()
                    connected = false
                    view?.setProgressVisible(false)
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
//                    if (opponentEndpointId.size < 1) {
////                        view?.setDisconnected()
//                    }
                }
            }// We're connected! Can now start sending and receiving data.
            // The connection was rejected by one or both sides.
            // The connection broke before it was able to be accepted.
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
//            avaibleGuests[endpointId] = "disconnected"
            avaibleGuests = HashMap()
            opponentEndpointId = ""
            guestNames.remove(endpointId)
//            view?.setChattiningTitle(guestNames)
            stopDiscovery()
            startDiscovery()
            connected = false
//            if (opponentEndpointId.size < 1) {
//                view?.setDisconnected()
//            }
            view?.finish()
        }
    }

    override fun init(username: String, packageName: String, colorCard: Int) {
        this.username = username
        this.packageName = packageName
        this.cardColor = colorCard
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun addMessage(message: Pair<ChatMessage, Int>) {
        messageList.add(message)
        view?.setMessages(messageList)
    }

    override fun sendMessage(message: String) {
//        for (guest in opponentEndpointId) {
//            Log.v("POSILJAM", guestNames[opponentEndpointId])

        val chatMessage = ChatMessage(username, message, Date().toString(), cardColor)

        val dataToSend = Gson().toJson(chatMessage)
        connectionsClient.sendPayload(opponentEndpointId, Payload.fromBytes(dataToSend.toByteArray()))
//        }
    }

    override fun stopDiscovery() {
        Log.v("POVEZAVA", "stopped discovery")
        connectionsClient.stopDiscovery()
    }

    override fun startDiscovery() {
        Log.v("POVEZAVA", "started discovery")
//        connectionsClient.stopDiscovery()
        connectionsClient.startDiscovery(
                packageName, endpointDiscoveryCallback, DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }

    override fun acceptConnection(user: String, endpointId: String) {
        guestNames[endpointId] = user
        // Automatically accept the connection on both sides.
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun getAvaibleGuests(): HashMap<String, String>{
        return avaibleGuests
    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun requestConnection(endpointId: String) {
        if (avaibleGuests[endpointId] != null && avaibleGuests[endpointId] == "connected") {
//            connectionsClient.disconnectFromEndpoint(endpointId)
//            avaibleGuests[endpointId] = "disconnected"
//            opponentEndpointId.remove(endpointId)
//            guestNames.remove(endpointId)
//            view?.setChattiningTitle(guestNames)
//            stopDiscovery()
//            if (opponentEndpointId.size < 1) {
////                view?.setDisconnected()
//            }
        } else if (avaibleGuests[endpointId] != null) {
            connectionsClient.requestConnection(username, endpointId, connectionLifecycleCallback)
        }

    }
}
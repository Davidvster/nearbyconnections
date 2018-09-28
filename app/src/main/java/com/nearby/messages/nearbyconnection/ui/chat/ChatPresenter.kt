package com.nearby.messages.nearbyconnection.ui.chat

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
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.data.model.Participant
import java.text.SimpleDateFormat
import java.util.Date

class ChatPresenter constructor(chatView: ChatMvp.View, private val context: Context = AppModule.application) : BasePresenter<ChatMvp.View>(chatView), ChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var hostEndpointId = ""
    private var availableGuests = HashMap<String, String>()

    private var guestList = listOf<String>()
    private var messageList = mutableListOf<Pair<ChatMessage, Int>>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private var connected = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
            if (chatMessage.user != null && chatMessage.date != null && chatMessage.message != null && chatMessage.color != null) {
                addMessage(Pair(chatMessage, 2))
            } else {
                val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
                if (guests.participants != null) {
                    guestList = guests.participants
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
                view?.updateConnectionList(availableGuests.toList())
            }
        }

        override fun onEndpointLost(endpointId: String) {
            availableGuests.remove(endpointId)
            view?.updateConnectionList(availableGuests.toList())
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
                    view?.setToolbarTitle(context.resources.getString(R.string.chat_room_title, availableGuests[endpointId]!!))
                    view?.setChatRoom()
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
            view?.updateConnectionList(availableGuests.toMutableMap().toList().toMutableList())
            hostEndpointId = ""
            connected = false
            messageList = mutableListOf()
            stopDiscovery()
            view?.setConnectionRoom()
        }
    }

    override fun init(username: String, packageName: String, colorCard: Int) {
        this.username = username
        this.packageName = packageName + BuildConfig.CHAT_ID
        this.cardColor = colorCard
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun addMessage(message: Pair<ChatMessage, Int>) {
        messageList.add(message)
        view?.updateMessageList(messageList)
    }

    override fun sendMessage(message: String) {
        val format = SimpleDateFormat("HH:mm - d.MM.yyyy")
        val formattedDate = format.format(Date())
        val chatMessage = ChatMessage(username, message, formattedDate, cardColor)
        val dataToSend = Gson().toJson(chatMessage)
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

    override fun isConnected(): Boolean {
        return connected
    }

    override fun requestConnection(endpointId: String) {
        if (availableGuests[endpointId] != null) {
            connectionsClient.requestConnection(username, endpointId, connectionLifecycleCallback)
        }
    }

    override fun getGuestList(): List<String> {
        return guestList
    }

    override fun getHostUsername(): String {
        return availableGuests[hostEndpointId]?: ""
    }

    override fun refreshConnectionList() {
        stopDiscovery()
        availableGuests = HashMap()
        view?.updateConnectionList(availableGuests.toList())
        view?.stopRefreshConnectionList()
    }
}
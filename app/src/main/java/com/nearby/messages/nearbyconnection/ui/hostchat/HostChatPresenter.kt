package com.nearby.messages.nearbyconnection.ui.hostchat

import android.content.Context
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
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.data.model.Guest
import com.nearby.messages.nearbyconnection.data.model.Participant

class HostChatPresenter constructor(hostChatView: HostChatMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostChatMvp.View>(hostChatView), HostChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guests = mutableListOf<Guest>()
    private var messageList = mutableListOf<Pair<ChatMessage, Int>>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))

            val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
            addMessage(Pair(chatMessage, 2))
            sendMessage(String(payload.asBytes()!!), endpointId)
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
//                    view?.setParticipantsTitle(guests.map { it.username })
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
                    sendParticipants()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                    Log.v("SOGOVOREC2", "We're connected! Can now start sending and receiving data. " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                }
                else -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
            guests.remove(guests.find { it.endpointId == endpointId })
//            view?.setParticipantsTitle(guests.map { it.username })
            sendParticipants()
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName + ".chat"
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

    override fun addMessage(message: Pair<ChatMessage, Int>) {
        messageList.add(message)
        view?.setMessages(messageList)
    }

    override fun sendMessage(message: String, endpointId: String) {
        for (guest in guests) {
            if (guest.endpointId != endpointId) {
                connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
            }
        }
    }

    private fun sendParticipants() {
        for (guest in guests) {
            val tmpGuestNames = guests.toMutableList()
            tmpGuestNames.remove(guest)
//            tmpGuestNames.add(Guest("chathost", username, 0))
            val message = Gson().toJson(Participant(tmpGuestNames.map { it.username }))
            connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
        }
    }

    override fun acceptConnection(user: String, endpointId: String) {
        guests.add(Guest(endpointId, user))
        connectionsClient.acceptConnection(endpointId, payloadCallback)

    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }

    override fun getGuestList(): List<String> {
        return guests.map { it.username }
    }
}
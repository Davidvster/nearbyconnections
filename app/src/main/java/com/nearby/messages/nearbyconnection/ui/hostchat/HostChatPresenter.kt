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
import com.nearby.messages.nearbyconnection.data.model.Participant

class HostChatPresenter constructor(hostChatView: HostChatMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostChatMvp.View>(hostChatView), HostChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guestsEndpointId = mutableListOf<String>()
    private var guestNames = HashMap<String, String>()
    private var avaibleGuests = HashMap<String, String>()
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
//            if (update.status == PayloadTransferUpdate.Status.SUCCESS && myChoice != null && opponentChoice != null) {
//                finishRound()
//            }
        }
    }

//    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
//        override fun onEndpointFound(
//                endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
//            Log.v("SOGOVOREC", "An endpoint was found: " + endpointId)
//            avaibleGuests[endpointId] = "found"
////            view?.updateConnectionList(avaibleGuests.values.toMutableList())
////            view?.showAvaibleDevicesDialog(avaibleGuests)
////            connectionsClient.requestConnection(ownerUsername, endpointId, connectionLifecycleCallback)
//            // An endpoint was found!
//        }
//
//        override fun onEndpointLost(endpointId: String) {
//            // A previously discovered endpoint has gone away.
//            avaibleGuests.remove(endpointId)
//            Log.v("SOGOVOREC", "A previously discovered endpoint has gone away. " + endpointId)
//        }
//    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            view?.showConnectionDialog(connectionInfo.endpointName, endpointId)
//            guestNames[endpointId] = connectionInfo.endpointName
//            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    avaibleGuests[endpointId] = "connected"
//                    stopAdvertising()
                    guestsEndpointId.add(endpointId)
                    view?.setChattingTitle(guestNames.values.toList())
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
//                    val guests = Participant(guestNames.values.toList())
//                    sendMessage(Gson().toJson(guests))
                    sendParticipants(guestNames)
//                    view?.setConnected()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    avaibleGuests[endpointId] = "rejeceted"
//                    stopAdvertising()
                    guestNames.remove(endpointId)
                    Log.v("SOGOVOREC2", "We're connected! Can now start sending and receiving data. " + endpointId)
                    if (guestsEndpointId.size < 1) {
//                        view?.setDisconnected()
//                        stopAdvertising()
                    }
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    avaibleGuests[endpointId] = "error"
                    guestNames.remove(endpointId)
//                    stopAdvertising()
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                    if (guestsEndpointId.size < 1) {
//                        view?.setDisconnected()
//                        stopAdvertising()
                    }
                }
            }// We're connected! Can now start sending and receiving data.
            // The connection was rejected by one or both sides.
            // The connection broke before it was able to be accepted.
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
            avaibleGuests[endpointId] = "disconnected"
            guestsEndpointId.remove(endpointId)
            guestNames.remove(endpointId)
            view?.setChattingTitle(guestNames.values.toList())
//            stopAdvertising()
            if (guestsEndpointId.size < 1) {
//                view?.setDisconnected()
//                stopAdvertising()
            }
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName
        this.cardColor = cardColor
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }


    override fun startAdvertising() {
        Log.v("POVEZAVA", "started advertising")
//        connectionsClient.stopAdvertising()
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
        for (guest in guestsEndpointId) {
            if (guest != endpointId) {
//                Log.v("POSILJAM", guestNames[guest])
                connectionsClient.sendPayload(guest, Payload.fromBytes(message.toByteArray()))
            }
        }
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
        // Automatically accept the connection on both sides.
        connectionsClient.acceptConnection(endpointId, payloadCallback)

    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }
}
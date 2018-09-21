package com.nearby.messages.nearbyconnection.ui.host

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter

class HostPresenter constructor(hostView: HostMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostMvp.View>(hostView), HostMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var opponentEndpointId = mutableListOf<String>()
    private var guestNames = HashMap<String, String>()
    private var avaibleGuests = HashMap<String, String>()
    private var messageList = mutableListOf<Pair<Pair<String, String>, Int>>()

    private var ownerUsername = ""

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))
            addMessage(Pair(Pair(String(payload.asBytes()!!), guestNames[endpointId]?:"unknown"), 2))
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
//            if (update.status == PayloadTransferUpdate.Status.SUCCESS && myChoice != null && opponentChoice != null) {
//                finishRound()
//            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(
                endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.v("SOGOVOREC", "An endpoint was found: " + endpointId)
            avaibleGuests[endpointId] = "found"
//            view?.updateConnectionList(avaibleGuests.values.toMutableList())
//            view?.showAvaibleDevicesDialog(avaibleGuests)
//            connectionsClient.requestConnection(ownerUsername, endpointId, connectionLifecycleCallback)
            // An endpoint was found!
        }

        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
            avaibleGuests.remove(endpointId)
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
                    avaibleGuests[endpointId] = "connected"
//                    stopAdvertising()
                    opponentEndpointId.add(endpointId)
//                    view?.setChattiningTitle(guestNames)
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
//                    view?.setConnected()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    avaibleGuests[endpointId] = "rejeceted"
//                    stopAdvertising()
                    guestNames.remove(endpointId)
                    Log.v("SOGOVOREC2", "We're connected! Can now start sending and receiving data. " + endpointId)
                    if (opponentEndpointId.size < 1) {
//                        view?.setDisconnected()
                        stopAdvertising()
                    }
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    avaibleGuests[endpointId] = "error"
                    guestNames.remove(endpointId)
//                    stopAdvertising()
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                    if (opponentEndpointId.size < 1) {
//                        view?.setDisconnected()
                        stopAdvertising()
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
            opponentEndpointId.remove(endpointId)
            guestNames.remove(endpointId)
//            view?.setChattiningTitle(guestNames)
            stopAdvertising()
            if (opponentEndpointId.size < 1) {
//                view?.setDisconnected()
                stopAdvertising()
            }
        }
    }

    override fun init() {
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }


    override fun startAdvertising(username: String, packageName: String) {
        Log.v("POVEZAVA", "started advertising")
        ownerUsername = username
//        connectionsClient.stopAdvertising()
        connectionsClient.startAdvertising(
                username, packageName, connectionLifecycleCallback, AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    override fun stopAdvertising() {
        Log.v("POVEZAVA", "stopped advertising")
        connectionsClient.stopAdvertising()
    }

    override fun addMessage(message: Pair<Pair<String, String>, Int>) {
        messageList.add(message)
        view?.setMessages(messageList)
    }
}
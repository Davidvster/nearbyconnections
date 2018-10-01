package com.nearby.messages.nearbyconnection.ui.hostchat

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
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.ChatMessage
import com.nearby.messages.nearbyconnection.data.model.Guest
import com.nearby.messages.nearbyconnection.data.model.Participant
import android.support.v4.util.SimpleArrayMap
import java.text.SimpleDateFormat
import java.util.Date


class HostChatPresenter constructor(hostChatView: HostChatMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostChatMvp.View>(hostChatView), HostChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guests = mutableListOf<Guest>()
    private var messageList = mutableListOf<Pair<ChatMessage, Int>>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private val incomingPayloads = SimpleArrayMap<Long, Payload>()
    private val filePayloadReference = SimpleArrayMap<Long, Int>()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
                if (chatMessage.type == 1) {
                    addMessage(Pair(chatMessage, 2))
                    sendMessage(String(payload.asBytes()!!), endpointId)
                } else {
                    filePayloadReference.put(chatMessage.message.toLong(), messageList.size)
                    addMessage(Pair(chatMessage, 2))
                    sendMessage(String(payload.asBytes()!!), endpointId)
                }
            } else if (payload.type == Payload.Type.FILE) {
                incomingPayloads.put(payload.id, payload)
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payloadId = update.payloadId
                val payload = incomingPayloads.remove(payloadId)
                if (payload != null && payload.type == Payload.Type.FILE) {
                    val payloadFile = payload.asFile()!!.asJavaFile()

//                    val newFilename = DateTime.now().toString()

                    val imageMessage = ChatMessage(messageList[filePayloadReference[payloadId]!!].first.user, messageList[filePayloadReference[payloadId]!!].first.message, messageList[filePayloadReference[payloadId]!!].first.date, messageList[filePayloadReference[payloadId]!!].first.color, 2)
                    imageMessage.picture = payloadFile
                    messageList[filePayloadReference[payloadId]!!] = Pair(imageMessage, 2)
                    view?.updateMessageList(messageList, filePayloadReference[payloadId]!!)
//                    payloadFile!!.renameTo(File(payloadFile.parentFile, newFilename))
                }
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            view?.showJoinDialog(connectionInfo.endpointName, endpointId)
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
        this.packageName = packageName + BuildConfig.CHAT_ID
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

    override fun addMessage(message: Pair<ChatMessage, Int>) {
        messageList.add(message)
        view?.updateMessageList(messageList)
    }

    override fun sendMessage(message: String, endpointId: String) {
        for (guest in guests) {
            if (guest.endpointId != endpointId) {
                connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
            }
        }
    }

    override fun sendFile(filePayload: Payload, endpointId: String) {
        for (guest in guests) {
            if (guest.endpointId != endpointId) {
                val format = SimpleDateFormat("HH:mm - d.MM.yyyy")
                val formattedDate = format.format(Date())
                val chatMessage = ChatMessage(guest.username, filePayload.id.toString(), formattedDate, cardColor, 2)
                val dataToSend = Gson().toJson(chatMessage)
                connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(dataToSend.toByteArray()))
                connectionsClient.sendPayload(guest.endpointId, filePayload)
            }
        }
    }

    private fun sendParticipants() {
        for (guest in guests) {
            val tmpGuestNames = guests.toMutableList()
            tmpGuestNames.remove(guest)
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
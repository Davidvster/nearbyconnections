package com.nearby.messages.nearbyconnection.ui.chat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v4.util.SimpleArrayMap
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val incomingPayloads = SimpleArrayMap<Long, Payload>()
    private val filePayloadReference = SimpleArrayMap<Long, Int>()

    private var currentPhotoPath: String = ""

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
                if (chatMessage.user != null && chatMessage.date != null && chatMessage.message != null && chatMessage.color != null) {
                    if (chatMessage.type == 1) {
                        addMessage(Pair(chatMessage, 2))
                    } else {
                        filePayloadReference.put(chatMessage.message.toLong(), messageList.size)
                        addMessage(Pair(chatMessage, 2))
                    }
                } else {
                    val guests = Gson().fromJson(String(payload.asBytes()!!), Participant::class.java)
                    if (guests.participants != null) {
                        guestList = guests.participants
                    }
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

                    val imageMessage = ChatMessage(messageList[filePayloadReference[payloadId]!!].first.user, messageList[filePayloadReference[payloadId]!!].first.message, messageList[filePayloadReference[payloadId]!!].first.date, messageList[filePayloadReference[payloadId]!!].first.color, 2)
                    imageMessage.picture = payloadFile
                    messageList[filePayloadReference[payloadId]!!] = Pair(imageMessage, 2)
                    view?.updateMessageList(messageList, filePayloadReference[payloadId]!!)
                }
            }
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
            view?.updateConnectionList(availableGuests.toList())
            hostEndpointId = ""
            connected = false
            messageList = mutableListOf()
            stopDiscovery()
            view?.setConnectionRoom()
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName + BuildConfig.CHAT_ID
        this.cardColor = cardColor
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun addMessage(message: Pair<ChatMessage, Int>) {
        messageList.add(message)
        view?.updateMessageList(messageList)
    }

    override fun sendMessage(message: String) {
        val format = SimpleDateFormat("HH:mm - d.MM.yyyy", Locale.UK)
        val formattedDate = format.format(Date())
        val chatMessage = ChatMessage(username, message, formattedDate, cardColor, 1)
        val dataToSend = Gson().toJson(chatMessage)
        addMessage(Pair(chatMessage, 1))
        connectionsClient.sendPayload(hostEndpointId, Payload.fromBytes(dataToSend.toByteArray()))
    }

    override fun sendFile(uri: Uri?) {
        var uri = uri
        if (uri == null) {
            uri = Uri.fromFile(File(currentPhotoPath))
        }
        val pfd = context.contentResolver.openFileDescriptor(uri!!, "r")
        val filePayload = Payload.fromFile(pfd!!)

        val format = SimpleDateFormat("HH:mm - d.MM.yyyy", Locale.UK)
        val formattedDate = format.format(Date())
        val chatMessage = ChatMessage(username, filePayload.id.toString(), formattedDate, cardColor, 2)
        val dataToSend = Gson().toJson(chatMessage)

        chatMessage.pictureUri = uri
        addMessage(Pair(chatMessage, 1))

        connectionsClient.sendPayload(hostEndpointId, Payload.fromBytes(dataToSend.toByteArray()))
        connectionsClient.sendPayload(hostEndpointId, filePayload)
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

    override fun attachImage(takePictureIntent: Intent, componentName: ComponentName) {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "com.nearby.messages.nearbyconnection",
                    it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            view?.startCameraActivity(takePictureIntent)

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}
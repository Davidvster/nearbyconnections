package com.nearby.messages.nearbyconnection.ui.hostchat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
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
import android.util.Log
import com.cloudmersive.client.RecognizeApi
import com.cloudmersive.client.invoker.ApiException
import com.cloudmersive.client.invoker.Configuration
import com.cloudmersive.client.invoker.auth.ApiKeyAuth
import com.nearby.messages.nearbyconnection.arch.DataModule
import com.nearby.messages.nearbyconnection.arch.Languages.LANGUAGE_LIST
import com.nearby.messages.nearbyconnection.data.managers.contract.TextRequestManager
import com.nearby.messages.nearbyconnection.data.model.LanguagesTopics
import com.nearby.messages.nearbyconnection.ext.rx.applyIoSchedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class HostChatPresenter constructor(hostChatView: HostChatMvp.View, private val context: Context = AppModule.application, private val textRequest: TextRequestManager = DataModule.textRequestManager) : BasePresenter<HostChatMvp.View>(hostChatView), HostChatMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

    private var guests = mutableListOf<Guest>()
    private var messageList = mutableListOf<Pair<ChatMessage, Int>>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1

    private val incomingPayloads = SimpleArrayMap<Long, Payload>()
    private val filePayloadReference = SimpleArrayMap<Long, Int>()

    private var currentPhotoPath: String = ""
    private var mainLanguage: String = ""
    private var mainTopics = HashMap<String, Int>()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val chatMessage = Gson().fromJson(String(payload.asBytes()!!), ChatMessage::class.java)
                if (chatMessage.type == 1) {
                    addMessage(Pair(chatMessage, 2))
                    sendReceivedMessage(String(payload.asBytes()!!), endpointId)
                } else {
                    filePayloadReference.put(chatMessage.message.toLong(), messageList.size)
                    addMessage(Pair(chatMessage, 2))
                    sendReceivedMessage(String(payload.asBytes()!!), endpointId)
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

//                    val newFilename = DateTime.now().toString()=
                    sendReceivedFile(payload, endpointId)
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
        val textToAnalyse = StringBuilder()
        var start = 0
        if (messageList.size > 10) {
            start = messageList.size - 10
        }
        for (message in messageList.subList(start, messageList.size)) {
            textToAnalyse.append(message.first.message + "\n")
        }
        analyseText(textToAnalyse.toString())
        view?.updateMessageList(messageList)
    }

    override fun sendMessage(message: String) {
        val format = DateTimeFormat.forPattern("HH:mm - d.MM.yyyy")
        val formattedDate = format.print(DateTime.now())
        val chatMessage = ChatMessage(username, message, formattedDate, cardColor, 1)
        addMessage(Pair(chatMessage, 1))
        connectionsClient.sendPayload(guests.map { it.endpointId }, Payload.fromBytes(Gson().toJson(chatMessage).toByteArray()))
    }

    override fun sendReceivedMessage(message: String, endpointId: String) {
        connectionsClient.sendPayload(guests.map { it.endpointId }.filter { it != endpointId }, Payload.fromBytes(message.toByteArray()))
    }

    override fun sendReceivedFile(filePayload: Payload, endpointId: String) {
        connectionsClient.sendPayload(guests.map { it.endpointId }.filter { it != endpointId }, filePayload)
    }

    override fun sendFile(uri: Uri?) {
        var uri = uri
        if (uri == null) {
            uri = Uri.fromFile(File(currentPhotoPath))
        }
        val pfd = context.contentResolver.openFileDescriptor(uri!!, "r")
        val filePayload = Payload.fromFile(pfd!!)

        val format = DateTimeFormat.forPattern("HH:mm - d.MM.yyyy")
        val formattedDate = format.print(DateTime.now())
        val chatMessage = ChatMessage(username, filePayload.id.toString(), formattedDate, cardColor, 2)
        val dataToSend = Gson().toJson(chatMessage)
        chatMessage.pictureUri = uri
        addMessage(Pair(chatMessage, 1))

        for (guest in guests) {
            connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(dataToSend.toByteArray()))
        }
        connectionsClient.sendPayload(guests.map { it.endpointId }, filePayload)
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

    private fun analyseText(text: String) {
        subscription.add(
                textRequest.getTextLanguage(text)
                        .applyIoSchedulers()
                        .subscribe({
                            val language = it.get("data").asJsonObject.get("detections").asJsonArray.get(0).asJsonObject.get("language").asString
                            if (LANGUAGE_LIST.containsKey(language)) {
                                mainLanguage = LANGUAGE_LIST[language]?: ""
                            }
                            subscription.add(
                                textRequest.getTextSummary(language, text)
                                        .applyIoSchedulers()
                                        .subscribe({
                                            if (it.get("topics") != null){
                                                val topics = it.get("topics").asJsonArray
                                                for (topic in topics) {
                                                    var count = mainTopics[topic.asString]?: 0
                                                    count ++
                                                    mainTopics[topic.asString] =  count
                                                }
                                            }
                                            sendLanguagesAndTopic()
                                        }) {
                                            Timber.d(it)
                                        }
                            )
                        })
                        {
                            Timber.d(it)
                        }
        )
    }

//    private fun analyseImage(imageUri: Uri) {
//        subscription.add(
//                textRequest.getImageClassification(imageUri)
//                        .applyIoSchedulers()
//                        .subscribe({
//                            Log.v("SLIKA", it.toString())
//                        })
//                        {
//                            Log.v("SLIKANAPAKA", it.toString())
//                        }
//        )
//    }

    private fun sendLanguagesAndTopic() {
        val languagesTopics = LanguagesTopics(getMainLanguage(), getMainTopic())
        connectionsClient.sendPayload(guests.map { it.endpointId }, Payload.fromBytes(Gson().toJson(languagesTopics).toByteArray()))
    }

    override fun getMainLanguage(): String {
        return mainLanguage
    }

    override fun getMainTopic(): List<String> {
        var count: Int? = null
        var topTopic: MutableList<String> = mutableListOf()
        for (entry in mainTopics.entries) {
            if (count == null || entry.value > count) {
                count = entry.value
                topTopic = mutableListOf()
                topTopic.add(entry.key)
            } else if (entry.value == count) {
                topTopic.add(entry.key)
            }
        }
        return topTopic
    }

    override fun recognizeImage(imageUri: Uri) {
        thread {
            val f = File(context.cacheDir, "tmp_img")
            f.createNewFile()

            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val fos = FileOutputStream(f)
            fos.write(byteArray)
            fos.flush()
            fos.close()


            val defaultClient = Configuration.getDefaultApiClient()

            val Apikey = defaultClient.getAuthentication("Apikey") as ApiKeyAuth
            Apikey.apiKey = "d0785b33-c008-48a5-86c4-c55d34ea69a6"

            val apiInstance = RecognizeApi()


            try {
                var objects = ""
                val result = apiInstance.recognizeDetectObjects(f)
                if (result.objectCount == 0) {
                    Log.v("NAJDENO: ", "not found")
                    objects = "nothing found"
                } else {
                    for (found in result.objects) {
                        objects += found.objectClassName + " "
                        Log.v("NAJDENO: ", found.objectClassName)
                    }
                }
                val result2 = apiInstance.recognizeDescribe(f)
                Log.v("NAJDENO2BEST", result2.bestOutcome.description)
                Log.v("NAJDENO2SECOND", result2.runnerUpOutcome.description)
                view?.showImageDescriptionDialog(objects, result2.bestOutcome.description + " CONFIDENCE: " + result2.bestOutcome.confidenceScore)

            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }
}
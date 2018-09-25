package com.nearby.messages.nearbyconnection.data.model

data class QuizResponse(val response: Int,
                        val dateReceived: String,
                        val dateSent: String) {
    var endpointId: String? = null
    var correct: Boolean = false
}
package com.nearby.messages.nearbyconnection.data.model

data class QuizResponse(val response: Int,
                        val timeTaken: Long) {
    var endpointId: String = ""
}
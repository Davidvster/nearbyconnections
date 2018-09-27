package com.nearby.messages.nearbyconnection.data.model

data class Guest(val endpointId: String,
                 val username: String,
                 var points: Long = 0)
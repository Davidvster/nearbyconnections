package com.nearby.messages.nearbyconnection.data.model

data class QuizResult(val winnerName: String,
                      val cardColor: Int,
                      val guests: List<Guest>)
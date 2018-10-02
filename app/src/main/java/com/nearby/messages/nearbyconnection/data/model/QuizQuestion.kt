package com.nearby.messages.nearbyconnection.data.model

data class QuizQuestion(val question: String,
                        val answerA: String,
                        val answerB: String,
                        val answerC: String,
                        val answerD: String,
                        var durationSec: Long = 60)
package com.nearby.messages.nearbyconnection.ui.hostquiz

import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult

interface HostQuizMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setParticipantsTitle(guestNames: List<String>)
        fun showConnectionDialog(user: String, endpointId: String)
        fun updateQuizResult(resultList: MutableList<QuizResult>)
    }

    interface Presenter : BaseMvp.Presenter {
        fun rejectConnection(endpointId: String)
        fun acceptConnection(user: String, endpointId: String)
        fun stopAdvertising()
        fun startAdvertising()
        fun stopAllConnections()
        fun init(username: String, packageName: String, cardColor: Int)
        fun sendQuestion(question: QuizQuestion, correctAnswer: Int)
    }
}
package com.nearby.messages.nearbyconnection.ui.lobby

import android.content.Context
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter

class LobbyPresenter constructor(lobbyView: LobbyMvp.View, private val context: Context = AppModule.application) : BasePresenter<LobbyMvp.View>(lobbyView), LobbyMvp.Presenter
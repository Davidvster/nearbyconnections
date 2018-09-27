package com.nearby.messages.nearbyconnection.ui.lobby

import com.nearby.messages.nearbyconnection.arch.BaseMvp

interface LobbyMvp : BaseMvp {
    interface View : BaseMvp.View

    interface Presenter : BaseMvp.Presenter
}
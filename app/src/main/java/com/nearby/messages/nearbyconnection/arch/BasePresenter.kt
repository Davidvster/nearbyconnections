package com.nearby.messages.nearbyconnection.arch

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<T : BaseMvp.View> protected constructor(protected var view: T?) : BaseMvp.Presenter {
    protected var subscription = CompositeDisposable()

    override fun onDestroy() {
        subscription.clear()
        view = null
    }
}
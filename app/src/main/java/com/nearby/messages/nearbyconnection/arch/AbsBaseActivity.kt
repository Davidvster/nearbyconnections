package com.nearby.messages.nearbyconnection.arch

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

abstract class AbsBaseActivity<T : BaseMvp.Presenter> : RxAppCompatActivity(), BaseMvp.View {
    protected lateinit var presenter: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
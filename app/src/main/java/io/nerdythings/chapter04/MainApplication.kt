package io.nerdythings.chapter04

import android.app.Application

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Flipper.init(this)
    }
}
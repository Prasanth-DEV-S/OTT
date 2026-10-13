package com.dealshot.app

import android.app.Application

class DealShotApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DealShotApplication
            private set
    }
}
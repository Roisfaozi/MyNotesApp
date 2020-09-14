package com.raven.mynotesapp.db

import android.app.Application
import com.facebook.stetho.Stetho

class StethoDatabase: Application(){
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
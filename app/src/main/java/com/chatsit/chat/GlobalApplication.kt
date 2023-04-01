package com.chatsit.chat

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.stipop.Stipop

open class GlobalApplication : Application() {
    companion object {
        lateinit var instance: GlobalApplication
            private set
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        instance = this


        Stipop.configure(this)

    }

    fun context(): Context = applicationContext
}
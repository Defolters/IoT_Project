package io.github.defolters.homeapp

import android.app.Application
import android.content.Context
import io.paperdb.Paper
import java.lang.ref.WeakReference

class App : Application() {

    companion object {
        lateinit var instance: WeakReference<Context>
    }

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)

        instance = WeakReference(this)
    }
}
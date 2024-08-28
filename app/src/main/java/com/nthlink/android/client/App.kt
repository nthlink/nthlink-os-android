package com.nthlink.android.client

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.nthlink.android.client.storage.sql.AppDatabase

class App : Application() {

    companion object {
        const val TAG = "nthlink_app"
    }

    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        db = AppDatabase.getInstance(this)

        // disable night mod
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
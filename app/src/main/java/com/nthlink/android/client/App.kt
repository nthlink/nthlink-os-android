package com.nthlink.android.client

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        const val TAG = "nthlink_app"
        const val POLICY_URL = "https://nthlink.com/policies/"
    }

    override fun onCreate() {
        super.onCreate()

        // disable night mod
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
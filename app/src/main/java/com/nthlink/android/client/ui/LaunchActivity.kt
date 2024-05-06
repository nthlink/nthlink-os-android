package com.nthlink.android.client.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nthlink.android.client.databinding.ActivityLaunchBinding

class LaunchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun moveToMainActivity() {
        MainActivity.start(this)
        finish()
    }
}
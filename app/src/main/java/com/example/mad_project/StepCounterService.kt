package com.example.mad_project

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import java.util.Timer
import java.util.TimerTask

class StepCounterService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, Notification()) // Make the service a foreground service

        // Start your step counting logic (you can use a Timer, SensorEventListener, etc.)
        startStepCounting()

        return START_STICKY
    }

    override fun onDestroy() {
        stopStepCounting()

        super.onDestroy()
    }

    private fun startStepCounting() {
        // Example: You can use a Timer or any other mechanism for continuous step counting
        // For simplicity, we'll use a Toast to simulate incrementing the step count
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Simulate incrementing the step count
                showToast("Step Count incremented!")
            }
        }, 0, 1000) // Increment every second (adjust as needed)
    }

    private fun stopStepCounting() {
        // Stop any ongoing step counting logic here
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

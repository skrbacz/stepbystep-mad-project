package com.example.mad_project.stepcounter_services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mad_project.firestore.UserManager
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.math.sqrt

class StepCountWorker(context: Context, params: WorkerParameters): Worker(context, params), SensorEventListener {

    private var magnitudePreviousStep = 0.0
    private var currentSteps = 0f

    private var previousTotalSteps = 0f
    private var manuallyAddedSteps = 0
    private var running = false
    private var noSensor = false
    private var alreadyAddedManualSteps = false


    override fun doWork(): Result {

        setupSensors()

        manuallyAddedSteps = getManuallyAddedSteps()

        val outputData = workDataOf(
            "currentSteps" to currentSteps.toInt(),
            "manuallyAddedSteps" to manuallyAddedSteps,
            "noSensor" to noSensor
        )

        return Result.success(outputData)
    }


    override fun onSensorChanged(event: SensorEvent?) {

        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val xaccel: Float = event.values[0]
            val yaccel: Float = event.values[1]
            val zaccel: Float = event.values[2]
            val magnitude: Double =
                sqrt((xaccel * xaccel + yaccel * yaccel + zaccel * zaccel).toDouble())

            val magnitudeDelta: Double = magnitude - magnitudePreviousStep
            magnitudePreviousStep = magnitude

            if (magnitudeDelta > 6 && !alreadyAddedManualSteps) {
                currentSteps++
                currentSteps += manuallyAddedSteps
                alreadyAddedManualSteps = true
            } else if (magnitudeDelta > 6) {
                currentSteps++
            }
        } else if (running) {
            val tSteps = event.values[0]
            currentSteps =
                (tSteps.toInt() - previousTotalSteps.toInt() + manuallyAddedSteps).toFloat()
            previousTotalSteps = tSteps
        }

    }

    private fun setupSensors() {
        running = true
        val sensorManager =
            applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager


        val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        when {
            countSensor != null -> {
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI)
            }

            detectorSensor != null -> {
                sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_UI)
            }

            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }

            else -> {
                noSensor = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }


    private fun getManuallyAddedSteps(): Int {

        val currentUser = UserManager().getCurrentUser()
        val currentDate = UserManager().currentMonthYear()
        val currentDay = LocalDate.now().dayOfMonth

        val stepArr: MutableList<Int>
        runBlocking {
            stepArr = UserManager().getManuallyAddedStepsArray(currentUser, currentDate)
        }
        return stepArr[currentDay - 1]

    }

    companion object {
        fun createWorkerRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<StepCountWorker>().build()
        }
    }
}
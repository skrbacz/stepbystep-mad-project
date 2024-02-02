package com.example.mad_project.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mad_project.AddStepsActivity
import com.example.mad_project.R
import com.example.mad_project.firestore.FireStoreData
import com.example.mad_project.firestore.UserManager
import com.example.mad_project.stepcounter_services.SaveStepsWorker
import com.example.mad_project.stepcounter_services.StepCountViewModel
import com.example.mad_project.stepcounter_services.StepCountWorker
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import kotlin.math.sqrt

//TODO- uploading steps to firebase
//TODO- counting steps while the app is turned off
class HomeFragment : Fragment(), SensorEventListener {


    private val ACTIVITY_RECOGNITION_CODE_REQUEST_CODE: Int = 100
    private var db = Firebase.firestore
    private var sensorManager: SensorManager? = null

    private var stepsTaken: TextView? = null
    private var stepsGoalTV: TextView? = null
    private var circularProgressBar: CircularProgressBar? = null
    private var messOfEncouragement: TextView? = null


    private var addStepsBTN: ImageView? = null

    private var welcomeMessageTV: TextView? = null

    private lateinit var currentUser: String
    private lateinit var currentDate: String
    private lateinit var nick: String
    private var dailyGoal: Int? = null

//    private lateinit var viewModel: StepCountViewModel
//    private lateinit var stepCountWorker: StepCountWorker
//    private lateinit var saveStepsWorker: SaveStepsWorker


    private var running = false //checks if the phone is moving
    private var alreadyAddedManualSteps = false
    private var currentSteps = 0
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var magnitudePreviousStep = 0.0
    private var manuallyAddedSteps = 0




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        currentUser = UserManager().getCurrentUser()
        currentDate = UserManager().currentMonthYear()

        messOfEncouragement = rootView.findViewById(R.id.encouragementText)

        addStepsBTN = rootView.findViewById(R.id.addStepsMBTN)

        stepsGoalTV = rootView.findViewById(R.id.dailyGoalTV)

        welcomeMessageTV = rootView.findViewById(R.id.welcomeMessageHF)

        circularProgressBar = rootView.findViewById(R.id.progress_circular)

        runBlocking {
            nick = UserManager().getCurrentUserNick(currentUser, currentDate)
            dailyGoal = getDailyGoal(currentUser, currentDate)
        }

        stepsTaken = rootView.findViewById(R.id.stepsTaken)

        if (isPermissionGranted()) {
            requestPermission()
        }

        updateUI()
        resetSteps()
        loadData()
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return rootView
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)

//        stepCountWorker = StepCountWorker(requireContext(), StepCountWorker.createWorkerRequest())
//
//        // Initialize and enqueue SaveStepsWorker
//        val saveStepsWorkerRequest = SaveStepsWorker.createWorkerRequest()
//        WorkManager.getInstance(requireContext())
//            .beginWith(stepCountWorker)
//            .then(saveStepsWorkerRequest)
//            .enqueue()


//    }

    private fun updateUI() {

        stepsTaken?.text=currentSteps.toString()

        manuallyAddedSteps= getManuallyAddedSteps()


        circularProgressBar?.apply {
            setProgressWithAnimation(currentSteps.toFloat())
            progressMax = dailyGoal!!.toFloat()
        }

        addStepsBTN?.setOnClickListener {
            val intent = Intent(activity, AddStepsActivity::class.java)
            startActivity(intent)
            activity?.finish()

        }


        setMessageOfEncouragament(dailyGoal!!,currentSteps)

        welcomeMessageTV?.text = "Hello $nick!"
        stepsGoalTV?.text = "/ $dailyGoal"

    }


    override fun onResume() {
        super.onResume()

        running = true


        val sensorManager =
            requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager


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
                Toast.makeText(activity, "No sensor detected on this device", Toast.LENGTH_SHORT)
                    .show()
                stepsTaken?.text = "$manuallyAddedSteps" //wrong idea
                circularProgressBar?.apply {
                    setProgressWithAnimation(manuallyAddedSteps.toFloat())
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

//TODO- make it reset when day changes
    private fun resetSteps() {
        stepsTaken?.setOnClickListener {
            Toast.makeText(activity, "long tap to reset", Toast.LENGTH_SHORT).show()
        }
        stepsTaken?.setOnLongClickListener {
            previousTotalSteps = totalSteps
            stepsTaken?.text = "0"
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("step", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("currentSteps", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("step", Context.MODE_PRIVATE)
        val saveNo: Float = sharedPreferences.getFloat("currentSteps", 0f)
        previousTotalSteps = saveNo
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
            val step: Int = totalSteps.toInt() + manuallyAddedSteps
            stepsTaken?.text = step.toString()

            circularProgressBar?.apply {
                setProgressWithAnimation(step.toFloat())
            }


        } else if (running) {
            totalSteps = event.values[0]

            currentSteps = totalSteps.toInt() - previousTotalSteps.toInt() + manuallyAddedSteps
            stepsTaken?.text = "$currentSteps"

            circularProgressBar?.apply {
                setProgressWithAnimation(currentSteps.toFloat())
            }

        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }



    private fun getCurrentDay(): Int {
        val currentDay = LocalDate.now()
        return currentDay.dayOfMonth
    }

    private fun getManuallyAddedSteps(): Int {

        val stepArr: MutableList<Int>
        runBlocking {
            stepArr = UserManager().getManuallyAddedStepsArray(currentUser, currentDate)
        }
        return stepArr[getCurrentDay() - 1]

    }


    private suspend fun getDailyGoal(currentUser: String, currentDate: String): Int {
        val docRef = db.collection(currentUser).document(currentDate)
        var dGoal = 0

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        if (userData != null) {
            dGoal = userData.dailyGoal
        }

        return dGoal
    }





    private fun setMessageOfEncouragament(dailyGoal: Int, currentSteps: Int) {

        val percentage = (currentSteps) / dailyGoal * 100

        if (percentage < 25) {
            messOfEncouragement?.text = "You've got this!"
        } else if (percentage in 25..49) {
            messOfEncouragement?.text = "Keep it up!"
        } else if (percentage in 50..74) {
            messOfEncouragement?.text = "Halfway there!"
        } else if (percentage in 75..99) {
            messOfEncouragement?.text = "You're almost there!"
        } else if (percentage == 100) {
            messOfEncouragement?.text = "Congratulations!"
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_CODE_REQUEST_CODE
                )
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            )
        } != PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_RECOGNITION_CODE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }





}




package com.example.mad_project.stepcounter_services

import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mad_project.firestore.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class SaveStepsWorker (context: Context, params: WorkerParameters) : Worker(context, params){

    private var currentSteps:  Int = 0

    override fun doWork():Result {
        currentSteps = inputData.getInt("currentSteps", 0)

        try {
            var stepList: MutableList<Int>
            val currentUser= UserManager().getCurrentUser()
            val currentDate= UserManager().currentMonthYear()


            runBlocking {
                stepList =
                    UserManager().getStepsArray(currentUser, currentDate)
            }

            val updatedList = updateMutableList(stepList)

            runBlocking {
                updateFireBase(currentDate, currentUser, updatedList)
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("Firestore update", "Error updating document", e)
            return Result.failure()
        }

    }

    private fun updateMutableList(dbList: MutableList<Int>): MutableList<Int>{

        val steps = currentSteps
        val dayOfMonth = LocalDate.now().dayOfMonth

        dbList[dayOfMonth- 1]+= steps
        return dbList

    }



    private fun updateFireBase(dateForDocument: String, currentUser: String, updatedList:MutableList<Int>){

        val updates = mapOf(
            "amountOfSteps" to updatedList
        )

        FirebaseFirestore.getInstance().collection(currentUser)
            .document(dateForDocument)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firestore Update", "Update successful!")
            }
            .addOnFailureListener{ e->
                Log.e("Firestore update", "Error updating document", e)
            }

    }
    companion object {
        fun createWorkerRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SaveStepsWorker>(
                15, TimeUnit.MINUTES // You can set your desired interval here
            ).build()
        }
}
}
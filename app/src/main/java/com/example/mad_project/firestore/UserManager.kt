package com.example.mad_project.firestore

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserManager {
    var db = Firebase.firestore
    val mAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): String{
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.email.toString()
    }

    fun currentMonthYear(): String {
        val currentDateTime = LocalDate.now()
        return currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun lastMonthCurrentYear(): String {
        val currentDateTime = LocalDate.now().minusMonths(1)
        return currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    suspend fun getCurrentUserNick(currentUser: String, currentDate: String): String {
        val docRef = db.collection(currentUser).document(currentDate)
        var nick = ""

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        if (userData != null) {
            nick = userData.nickname
        }

        return nick
    }

    suspend fun getStepsArray(currentUser: String, currentDate: String): MutableList<Int> {
        val docRef = db.collection(currentUser).document(currentDate)
        val stepsArray: MutableList<Int>

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        stepsArray = userData?.amountOfSteps ?: mutableListOf()

        return stepsArray
    }

    suspend fun getManuallyAddedStepsArray(currentUser: String, currentDate: String): MutableList<Int> {
        val docRef = db.collection(currentUser).document(currentDate)
        val stepsArray: MutableList<Int>

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        stepsArray = userData?.stepsAddedManually ?: mutableListOf()

        return stepsArray
    }

    suspend fun getCreationTimeStamp(): Long? {
        val user = mAuth.currentUser
        val creationTimestamp = user?.metadata?.creationTimestamp
        return creationTimestamp
    }
}
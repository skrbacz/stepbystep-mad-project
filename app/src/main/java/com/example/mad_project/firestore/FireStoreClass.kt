package com.example.mad_project.firestore

import com.example.mad_project.login_register_activities.CreateNewAccountActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()
    fun registerUserFS(activity: CreateNewAccountActivity, userInfo: User) {

        mFireStore.collection(userInfo.email)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.registrationSuccess()
            }
            .addOnFailureListener{
            }
    }
}
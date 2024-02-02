package com.example.mad_project.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.mad_project.R
import com.example.mad_project.firestore.FireStoreData
import com.example.mad_project.firestore.UserManager
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    var db = com.google.firebase.Firebase.firestore
    private lateinit var profilePicture: ImageView
    private var progressBar: ProgressBar?=null
    private var nickName: TextView?=null
    private var monthlyGoalTV : TextView?=null
    private var approximateDistanceTV: TextView?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView= inflater.inflate(R.layout.fragment_profile, container, false)
        nickName= rootView.findViewById(R.id.nickNamePRF)
        progressBar= rootView.findViewById(R.id.progressBarProfile)
        monthlyGoalTV=rootView.findViewById(R.id.informAboutMonthlyGoalTV)
        approximateDistanceTV= rootView.findViewById(R.id.informAboutApproximateDistanceTV)


        var nick = ""
        val currentUser = UserManager().getCurrentUser()
        val currentDate = UserManager().currentMonthYear()
        var monthlyGoal: Int

        runBlocking {
            nick = getCurrentUserNick(currentUser, currentDate)
            monthlyGoal= getMonthlyGoal(currentUser,currentDate)
        }

        nickName?.text= nick


        progressBar?.max= monthlyGoal

        val sumOfSteps= getSumOfSteps()

        progressBar?.progress= sumOfSteps

        monthlyGoalTV?.text= "monthly goal: $sumOfSteps/$monthlyGoal"

        val approximateMax= calculateApproximateDistance(monthlyGoal)
        val approximateNow= calculateApproximateDistance(sumOfSteps)


        approximateDistanceTV?.text= "approximate distance: $approximateNow/$approximateMax [km]"







        return rootView
    }

    private suspend fun getCurrentUserNick(currentUser: String, currentDate: String): String {
        val docRef = db.collection(currentUser).document(currentDate)
        var nick = ""

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        if (userData != null) {
            nick = userData.nickname
        }

        return nick
    }

    private suspend fun getMonthlyGoal(currentUser: String, currentDate: String): Int {
        val docRef = db.collection(currentUser).document(currentDate)
        var mGoal = 0

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        if (userData != null) {
            mGoal = userData.monthlyGoal
        }

        return mGoal
    }

    private fun getSumOfSteps(): Int{

        var amountOfSteps : MutableList<Int>
        var manuallyAddedSteps : MutableList<Int>
        runBlocking {
            amountOfSteps= UserManager().getStepsArray(UserManager().getCurrentUser(),UserManager().currentMonthYear())
            manuallyAddedSteps = UserManager().getManuallyAddedStepsArray(UserManager().getCurrentUser(), UserManager().currentMonthYear())
        }


        val totalSum = amountOfSteps.sum() + manuallyAddedSteps.sum()

        return totalSum
    }

    private fun calculateApproximateDistance(amountOfSteps:Int): Int{
        return ((amountOfSteps*0.70)/1000).toInt()
    }
}
package com.example.mad_project.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mad_project.R
import com.example.mad_project.login_register_activities.LoginActivity
import com.example.mad_project.popups.ChangeMonthlyGoalPopUpFragment
import com.example.mad_project.popups.EditProfilePopUpFragment
import com.example.mad_project.popups.SetNotificationsPopUpFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

//TODO- add one more button(about us)
class SettingsFragment : Fragment() {

    var db = Firebase.firestore
    private var mAuth: FirebaseAuth? = null
    private var editProfileBTN: Button? = null
    private var setNotifBTN: Button?= null
    private var changeMontlyGoalBTN: Button?= null
    private var logOutTV : Button?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView= inflater.inflate(R.layout.fragment_settings, container, false)

        logOutTV= rootView.findViewById(R.id.logOutBTN)
        editProfileBTN= rootView.findViewById(R.id.editProfileBTN)
        setNotifBTN= rootView.findViewById(R.id.notificationsBTN)
        changeMontlyGoalBTN= rootView.findViewById(R.id.changeMonthlyGoalBTN)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        editProfileBTN?.setOnClickListener {
            val showPopUp = EditProfilePopUpFragment()
            showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "showEditProfile")
        }

        changeMontlyGoalBTN?.setOnClickListener {
            val showPopUp = ChangeMonthlyGoalPopUpFragment()
            showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "showChangeMonthlyGoal")
        }

        setNotifBTN?.setOnClickListener {
            val showPopUp = SetNotificationsPopUpFragment()
            showPopUp.show((activity as AppCompatActivity).supportFragmentManager, "setNotification")
        }

        logOutTV?.setOnClickListener {

            mAuth?.signOut()
            val intent = Intent(activity,LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Toast.makeText(activity, "You logged out successfully", Toast.LENGTH_SHORT).show()

        }

        return rootView
    }
}
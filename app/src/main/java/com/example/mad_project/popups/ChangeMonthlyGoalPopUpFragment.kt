package com.example.mad_project.popups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mad_project.R
import com.example.mad_project.firestore.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil


class ChangeMonthlyGoalPopUpFragment : DialogFragment() {

    private var newGoal: EditText? =null
    private var updateGoalBTN: Button?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_change_monthly_goal_pop_up, container, false)

        newGoal= rootView.findViewById(R.id.newGoalEDT)
        updateGoalBTN= rootView.findViewById(R.id.saveNewGoalBTN)

        val currentUser = UserManager().getCurrentUser()
        val currentDate= UserManager().currentMonthYear()

        updateGoalBTN?.setOnClickListener {
            if(monthlyGoalProvided()) {

                val monthlyGoal = newGoal?.text.toString().toInt()
                val dailyGoal= getDailyGoal(monthlyGoal,currentDate)
                val updates = mapOf(
                    "monthlyGoal" to monthlyGoal,
                    "dailyGoal" to dailyGoal
                )

                FirebaseFirestore.getInstance().collection(currentUser)
                    .document(currentDate)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("Firestore Update- update monthly and daily goal", "Update successful!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore update", "Error updating document", e)
                    }
                dismiss()
                Toast.makeText(activity, "Your monthly goal has been updated!", Toast.LENGTH_SHORT).show()

            }
        }

        return rootView
    }

    private fun monthlyGoalProvided(): Boolean{
        if(newGoal?.text.isNullOrBlank()){
            newGoal?.error = "Your monthly goal cannot be 0"
            return false
        }else if(newGoal?.text.toString() <= "0") {
            newGoal?.error = "Your monthly goal has to be bigger than 0"
            return false
        }else if(newGoal?.length()!! >= 7){
            newGoal?.error= "Provide a viable goal"
            return false
        }
        return true
    }

    fun getDailyGoal (monthlyGoal: Int, currentDate: String): Int {
        var sum = 0

        val (currentYear, currentMonth) = currentDate.split("-").map { it.toInt() }

        return when (currentMonth) {
            1, 3, 5, 7, 8, 12 -> ceil((monthlyGoal - sum).toDouble() / 31).toInt()
            4, 6, 9, 11 -> ceil((monthlyGoal - sum).toDouble() / 30).toInt()
            2 -> if (currentYear % 4 == 0) ceil((monthlyGoal - sum).toDouble() / 29).toInt()
            else ceil((monthlyGoal - sum).toDouble() / 28).toInt()
            else -> 0
        }
    }

    }

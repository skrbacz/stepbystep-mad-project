package com.example.mad_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mad_project.firestore.FireStoreData
import com.example.mad_project.firestore.UserManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

//TODO(done)- create new document if there is no document with this date and insert there monthly goal and daily goal
//TODO(done)- make prettier layout
//TODO- figure out why it doesn't work
class SetMonthlyGoalActivity : AppCompatActivity() {

    var db = Firebase.firestore

    private var welcomeMessage: TextView? = null
    private var instructionMessage: TextView?= null
    private var amountOfSteps: EditText? = null
    private var goalButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_monthly_goal)
        supportActionBar?.hide()

        welcomeMessage = findViewById(R.id.welcomeMessageSMG)
        amountOfSteps = findViewById(R.id.monthlyGoalOfSteps )
        goalButton = findViewById(R.id.setGoalButton)


        val currentUser= UserManager().getCurrentUser()
        val currentDate= UserManager().currentMonthYear()




        goalButton?.setOnClickListener {


            if(monthlyGoalProvided()) {

                var exsitsOrNot:Boolean
                runBlocking {
                    exsitsOrNot = checkDocumentExistence(currentUser,currentDate)
                }

                val monthlyGoal = amountOfSteps?.text.toString().toInt()
                val dailyGoal= getDailyGoal(monthlyGoal,currentDate)

                if(exsitsOrNot){
                    val updates = mapOf(
                        "monthlyGoal" to monthlyGoal,
                        "dailyGoal" to dailyGoal
                    )

                    FirebaseFirestore.getInstance().collection(currentUser)
                        .document(currentDate)
                        .update(updates)
                        .addOnSuccessListener {
                            Log.d("Firestore Update", "Update successful!")
                            goToMain()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore update", "Error updating document", e)
                        }
                    goToMain()
                }else {

                    welcomeMessage?.text= "New month new goal!"
                    welcomeMessage?.text= "Please provide the goal for the new month!"

                    var nick : String
                    val lastMonth= UserManager().lastMonthCurrentYear()

                    runBlocking {
                        nick= UserManager().getCurrentUserNick(currentUser,lastMonth)
                    }

                    createNewDocument(nick,currentUser,monthlyGoal,dailyGoal)
                    goToMain()
                }




            }

        }

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

    private fun monthlyGoalProvided(): Boolean{
        if(amountOfSteps?.text.isNullOrBlank()){
            amountOfSteps?.error = "Your monthly goal cannot be 0"
            return false
        }else if(amountOfSteps?.text.toString() <= "0") {
            amountOfSteps?.error = "Your monthly goal has to be bigger than 0"
            return false
        }else if(amountOfSteps?.length()!! >= 7){
            amountOfSteps?.error= "Provide a viable goal"
            return false
        }
        return true
    }

    fun goToMain() {
        val intent= Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private suspend fun checkDocumentExistence(currentUser: String, currentDate: String): Boolean {

        val docRef: DocumentReference = db.collection(currentUser).document(currentDate)
        return try {
            val document: DocumentSnapshot = docRef.get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun createNewDocument(nick:String, currentUser: String, monthlyGoal:Int , dailyGoal: Int){
        val currentDate= UserManager().currentMonthYear()
        val userData= FireStoreData(currentUser,nick, monthlyGoal,dailyGoal,MutableList(31) { 0 },MutableList(31) { 0 })
        FirebaseFirestore.getInstance().collection(currentUser)
            .document(currentDate)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
            }
            .addOnFailureListener {

            }

    }


}
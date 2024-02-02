package com.example.mad_project


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mad_project.firestore.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import java.util.Calendar

//TODO(done) - day of added steps is wrong- handle when day is < 10
//TODO(done) - block min date on calendar view
//TODO- check what happens if someone tries to add steps on day before they created account
class AddStepsActivity : AppCompatActivity() {

    private var calendarView: CalendarView? = null
    private var addStepsButton: Button? = null
    private var amountOfStepsET: EditText? = null
    private var dateSelected: String? = null
    private lateinit var dateForDocument: String
    private var goBackIMG: ImageView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_steps)
        supportActionBar?.hide()

        calendarView = findViewById(R.id.calendarView)
        addStepsButton = findViewById(R.id.addStepsButton)
        amountOfStepsET = findViewById(R.id.stepsNumberETV)
        goBackIMG = findViewById(R.id.goBackIMG)

        val currentUser = UserManager().getCurrentUser()
        val currentDate = UserManager().currentMonthYear()

        var stepList: MutableList<Int>

        //blocking going before creation of account
        val creationTimestamp: Long?
        runBlocking {
            creationTimestamp = UserManager().getCreationTimeStamp()
        }

        if (creationTimestamp != null) {
            calendarView?.minDate = creationTimestamp
        }


        calendarView?.setOnDateChangeListener { view, year, month, dayOfMonth ->

            dateSelected = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)

            dateForDocument = String.format("%04d-%02d", year, month + 1)

        }

        addStepsButton?.setOnClickListener {

            if (dateSelected == null) {
                val cd = Calendar.getInstance()
                val day = cd.get(Calendar.DAY_OF_MONTH)
                val month = cd.get(Calendar.MONTH) + 1
                val year = cd.get(Calendar.YEAR)

                dateSelected = String.format("%04d%02d%02d", year, month + 1, day)

                dateForDocument = currentDate

            }
            if (stepsProvided()) {

                runBlocking {
                    stepList =
                        UserManager().getManuallyAddedStepsArray(currentUser, dateForDocument)
                }


                val updatedList = updateMutableList(stepList)

                runBlocking {
                    updateFireBase(dateForDocument, currentUser, updatedList)
                }
                goToMain()
            }
        }


        goBackIMG?.setOnClickListener {
            goToMain()
        }
    }

    private fun stepsProvided(): Boolean {
        if (amountOfStepsET?.text.isNullOrBlank()){
            amountOfStepsET?.error = "provide the amount of steps you want to add!"
            return false
        }else if (amountOfStepsET?.text.toString().toInt() <= 0){
            amountOfStepsET?.error= "you can't add negative amount of steps!"
            return false
        }else if(amountOfStepsET?.text?.length!! > 6){
            amountOfStepsET?.error= "provide a viable amount of steps"
            return false
        }

        return true
    }

    private fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun updateMutableList(dbList: MutableList<Int>): MutableList<Int>{

        val steps = amountOfStepsET?.text.toString().toInt()
        val dayOfMonth = dateSelected?.takeLast(2)?.let {
            if (it.length == 2 && it[0] == '0') {
                it[1].toString().toIntOrNull() ?: 1
            } else {
                it.toIntOrNull() ?: 1
            }
        } ?: 1



        dbList[dayOfMonth- 1]+= steps
        return dbList

    }



    private fun updateFireBase(dateForDocument: String, currentUser: String, updatedList:MutableList<Int>){

        val updates = mapOf(
            "stepsAddedManually" to updatedList
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

}

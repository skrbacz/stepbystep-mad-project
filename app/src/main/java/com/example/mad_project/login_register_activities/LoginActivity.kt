package com.example.mad_project.login_register_activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mad_project.MainActivity
import com.example.mad_project.R
import com.example.mad_project.SetMonthlyGoalActivity
import com.example.mad_project.firestore.FireStoreData
import com.example.mad_project.other.Snack
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class LoginActivity : Snack() {

    var db=  Firebase.firestore
    var mAuth = FirebaseAuth.getInstance()

    private var email: EditText?= null
    private var password: EditText?= null
    private var loginBTN: Button?= null
    private var createNewAccountBTN: Button?= null
    private var forgottenPasswordTV: TextView?= null
    private var gameChangerBTN: TextView?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(2500)
        installSplashScreen()

        setContentView(R.layout.activity_login)

        email= findViewById(R.id.loginEmailEDTV)
        password= findViewById(R.id.loginPasswordEDTV)
        loginBTN= findViewById(R.id.loginLoginBtn)
        createNewAccountBTN= findViewById(R.id.loginCreateNewAccount)
        forgottenPasswordTV= findViewById(R.id.loginForgotYourPasswordTV)
        gameChangerBTN= findViewById(R.id.gameChangerBTN)

        val currentUser= getCurrentUser().toString()
        val currentDate = currentMonthYear()
        var goal: Int


        supportActionBar?.hide()


        val user = mAuth.currentUser

        /*
        https://stackoverflow.com/questions/22262463/firebase-how-to-keep-an-android-user-logged-in
         */
        if (user != null){
            val i = Intent(this@LoginActivity, MainActivity::class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
        }else {

            loginBTN?.setOnClickListener {

                if (validateLogin()) { // Check if login credentials are valid
                    runBlocking {
                        goal = getGoal(currentUser, currentDate)
                    }

                    if (goal == 0) {
                        loginInRegistered()
                        goToSetGoalActivity()
                    } else {
                        loginInRegistered()
                        goToMain()
                    }
                }
            }

            forgottenPasswordTV?.setOnClickListener {
                val intent = Intent(this, ForgottenPasswordActivity::class.java)
                startActivity(intent)
            }

            createNewAccountBTN?.setOnClickListener {
                val intent = Intent(this, CreateNewAccountActivity::class.java)
                startActivity(intent)
            }

            gameChangerBTN?.setOnClickListener {

                runBlocking {
                    goal = getGoal("firstuser@test.com", currentDate)
                }

                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword("firstuser@test.com", "!Qwerty123")
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@LoginActivity,
                                "You logged in successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            if (goal > 0) {
                                goToMain()
                            } else {
                                goToSetGoalActivity()
                            }
                        } else {
                            showSnackbar(task.exception!!.message.toString(), false)
                        }

                    }
            }
        }
    }

    private fun validateLogin(): Boolean {
        if(email?.text.isNullOrBlank()){
            showSnackbar("Please provide your email!",false)
            return false
        }

        if(password?.text.isNullOrBlank()){
            showSnackbar("Please provide your password!", false)
            return false
        }

        return true
    }

    private fun loginInRegistered(){
        if (validateLogin()){
            val em = email?.text.toString().trim() {it <= ' '}
            val pass= password?.text.toString().trim() {it <= ' '}
            val currentDate = currentMonthYear()



            FirebaseAuth.getInstance().signInWithEmailAndPassword(em,pass).addOnCompleteListener {
                task ->

                if(task.isSuccessful){
                    Toast.makeText(this@LoginActivity,"You logged in successfully!", Toast.LENGTH_LONG).show()

                }else{
                    showSnackbar(task.exception!!.message.toString(),false)
                }

            }
        }

    }

open fun goToSetGoalActivity(){
        val user = FirebaseAuth.getInstance().currentUser
        val uid= user?.email.toString()

        val intent= Intent(this, SetMonthlyGoalActivity::class.java)
        intent.putExtra("uID",uid)
        startActivity(intent)
        finish()

    }

    open fun goToMain(){
        val user = FirebaseAuth.getInstance().currentUser
        val uid= user?.email.toString()

        val intent= Intent(this, MainActivity::class.java)
        intent.putExtra("uID",uid)
        startActivity(intent)
        finish()

    }


    private fun currentMonthYear(): String {
        val currentDateTime = LocalDate.now()
        return currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    private fun getCurrentUser(): String? {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.email
    }
    suspend fun getGoal(currentUser:String, currentDate: String): Int {
        val docRef= db.collection(currentUser).document(currentDate)
        var goal= 0

        val userData = docRef.get().await().toObject(FireStoreData::class.java)

        if (userData != null) {
            goal = userData.monthlyGoal
        }

        return goal
    }

}
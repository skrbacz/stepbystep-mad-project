package com.example.mad_project.login_register_activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mad_project.R
import com.example.mad_project.other.Snack
import com.example.mad_project.firestore.FireStoreData
import com.example.mad_project.firestore.UserManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase


class CreateNewAccountActivity : Snack() {

    private lateinit var auth: FirebaseAuth

    private var nickname: EditText?= null
    private var email: EditText? = null
    private var password: EditText? = null
    private var repeatPassword: EditText? = null
    private var loginTV: TextView? = null
    private var registerBtn: Button? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_account)

        supportActionBar?.hide()


        nickname= findViewById(R.id.cnaNickNameEDTV)
        email = findViewById(R.id.cnaEmailEDTV)
        password = findViewById(R.id.cnaPasswordEDTV)
        repeatPassword = findViewById(R.id.cnaRPasswordEDTV)
        loginTV = findViewById(R.id.cnaAlreadyHaveAccountTV)
        registerBtn = findViewById(R.id.cnaRegisterBtn)
        auth = Firebase.auth



        loginTV?.setOnClickListener {
            goToLogin()
        }

        registerBtn?.setOnClickListener {
            if (validRegisterInformation()) {

                val login: String = email?.text.toString().trim() { it <= ' ' }
                val pass: String = password?.text.toString().trim() { it <= ' ' }
                val nick: String = nickname?.text.toString().trim() { it <= ' ' }



                auth.createUserWithEmailAndPassword(login,pass)
                    .addOnCompleteListener(this){ task->
                        if(task.isSuccessful){
                            Log.d(TAG,"createUserWithEmail:success")
                            createDataBaseForUser(nick,login)
                            registrationSuccess()
                            goToLogin()
                        }else{
                            Log.w(TAG,"createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }


                    }
            }



        }



    }

    private fun validRegisterInformation(): Boolean {
        if (nickname?.text.isNullOrBlank()) {
            nickname?.error = "You can't change your nickname to nothing!"
            return false
        } else if ((nickname?.text?.length ?: 0) < 4) {
            nickname?.error = "Your nick can't be shorter than 4 letters"
            return false
        } else if ((nickname?.text?.length ?: 0) >= 10) {
            nickname?.error = "Your nick can't be longer than 10 letters"
            return false
        }

        if (email?.text.isNullOrBlank()) {
            showSnackbar("Email is required", false)
            return false
        }

        var doesItHaveAdd = false

        for (char in email?.text.toString()) {
            if (char == '@') {
                doesItHaveAdd = true
                break
            }
        }

        if (!doesItHaveAdd) {
            showSnackbar("Provide valid email!", false)
            return false
        }


        if (password?.text.isNullOrBlank()) {
            showSnackbar("Password is required", false)
            return false
        }
        if (password!!.length() < 8) {
            showSnackbar("Your password has to be at least 8 characters long!", false)
            return false
        }

        var doesItHaveAtLeastOneCapitalLetter = false
        var atLeastOneSpecialSign = false
        val specialCharacters = setOf(
            '!',
            '@',
            '#',
            '$',
            '%',
            '^',
            '&',
            '*',
            '(',
            ')',
            '-',
            '_',
            '=',
            '+',
            '`',
            '~',
            '{',
            '}',
            ':',
            ';',
            '\"',
            '\'',
            '<',
            '>',
            '.',
            '?',
            '/'
        )

        for (char in password?.text.toString()) {
            if (char.isUpperCase()) {
                doesItHaveAtLeastOneCapitalLetter = true
                break
            }
        }

        for (char in password?.text.toString()) {
            if (specialCharacters.contains(char)) {
                atLeastOneSpecialSign = true
                break
            }
        }

        if (!doesItHaveAtLeastOneCapitalLetter) {
            showSnackbar("Your password should have at least one capital letter!", false)
            return false
        } else if (!atLeastOneSpecialSign) {
            showSnackbar("Password should have at least one special character!", false)
            return false
        }

        if (!password?.text?.toString().equals(repeatPassword?.text?.toString())) {
            showSnackbar("The passwords aren't the same!", false)
            return false
        }

        return true

    }

    fun registrationSuccess() {
        Toast.makeText(
            this@CreateNewAccountActivity, "You are registered successfully",
            Toast.LENGTH_LONG
        ).show()

    }
    fun goToLogin (){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createDataBaseForUser(nickname:String, email: String){
        val currentDate= UserManager().lastMonthCurrentYear()
//        val currentDate= UserManager().currentMonthYear()
        val userData= FireStoreData(email,nickname, 0,0,MutableList(31) { 0 },MutableList(31) { 0 })
        FirebaseFirestore.getInstance().collection(email)
            .document(currentDate)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
            }
            .addOnFailureListener {

            }

    }



}

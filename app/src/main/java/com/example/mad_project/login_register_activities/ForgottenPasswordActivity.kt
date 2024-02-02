package com.example.mad_project.login_register_activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.mad_project.R
import com.example.mad_project.other.Snack

class ForgottenPasswordActivity : Snack() {
    private var sentEmailBTN: Button?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_forgotten)
        supportActionBar?.hide()

        sentEmailBTN= findViewById(R.id.fpSentReactivationLinkBTN)

        sentEmailBTN?.setOnClickListener {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // if the email is in the firebase then the green snackbar shows up saying the mail have been sent
        //if there is no such mail there will pop up red snackbar that says "there is no such account/invalid email"
    }
}
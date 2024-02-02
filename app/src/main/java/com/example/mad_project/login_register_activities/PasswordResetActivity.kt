package com.example.mad_project.login_register_activities

import android.os.Bundle
import com.example.mad_project.R
import com.example.mad_project.other.Snack

class PasswordResetActivity : Snack() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)
        supportActionBar?.hide()
        // from link in the mail we get here and update the password saved in the firebase
        //cant get here without the link

    }
}
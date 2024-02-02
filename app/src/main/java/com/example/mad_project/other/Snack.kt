package com.example.mad_project.other

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mad_project.R
import com.google.android.material.snackbar.Snackbar

open class Snack : AppCompatActivity(){
    fun showSnackbar(message:String,boolean:Boolean){
        val snackbar = Snackbar.make(findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG).setAction("Action", null)
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.black))

        if(boolean){
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.snackbar_success))
            snackbar.setText(message)
            snackbar.show()

        }else{
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.snackbar_failure))
            snackbar.setText(message)
            snackbar.show()
        }
    }

}
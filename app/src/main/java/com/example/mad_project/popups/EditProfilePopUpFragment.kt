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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

class EditProfilePopUpFragment : DialogFragment() {

    private var updateNickBTN: Button? = null
    private var newNickEDT: EditText? = null
    private var doneButton: Button? = null
    private var updateProfilePhoto: Button? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        updateNickBTN = rootView.findViewById(R.id.updateNickBTN)
        newNickEDT = rootView.findViewById(R.id.newNickEDTV)

//        doneButton = rootView.findViewById(R.id.doneBTN)


//
//        doneButton?.setOnClickListener {
//            dismiss()
//        }


        updateNickBTN?.setOnClickListener {
            if (valiableNick()){
                var nick = newNickEDT?.text.toString()
                runBlocking {
                    updateNick(nick)
                }
                Toast.makeText(activity, "Your nick has been changed!", Toast.LENGTH_SHORT).show()
            }

        }



        return rootView
    }


    suspend fun updateNick(nick: String) {

        val updates = mapOf(
            "nickname" to nick
        )

        FirebaseFirestore.getInstance().collection(UserManager().getCurrentUser())
            .document(UserManager().currentMonthYear())
            .update(updates)
            .addOnSuccessListener {
                Log.d("Nickname update success", "nick: $nick")
            }
            .addOnFailureListener { e ->
                Log.e("Nickname update failure", "Error updating document", e)
            }

    }


    private fun valiableNick(): Boolean {

        if (newNickEDT?.text.isNullOrBlank()) {
            newNickEDT?.error = "You can't change your nickname to nothing!"
            return false
        } else if ((newNickEDT?.text?.length ?: 0) < 4) {
            newNickEDT?.error = "Your nick can't be shorter than 4 letters"
            return false
        } else if ((newNickEDT?.text?.length ?: 0) > 10) {
            newNickEDT?.error = "Your nick can't be longer than 10 letters"
            return false
        }
        return true

    }



}
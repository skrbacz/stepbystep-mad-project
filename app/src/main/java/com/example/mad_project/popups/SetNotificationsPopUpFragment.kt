package com.example.mad_project.popups

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.mad_project.R

//TODO: switching off permission to recieve notifications
class SetNotificationsPopUpFragment : DialogFragment() {

    private var thanksBTN: Button?= null

    private val ACTIVITY_POST_NOTIFICATIONS_CODE_REQUEST_CODE: Int = 23

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView= inflater.inflate(R.layout.fragment_set_notifications_pop_up, container, false)


        thanksBTN= rootView.findViewById(R.id.givePermissionBTN)

        thanksBTN?.setOnClickListener {

           dismiss()

        }


        return rootView

        //more advanced: swtich to turn off permitions
        //easier: information that if user wants to get rid off dayily notification they have to turn it off in settings of phone
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    ACTIVITY_POST_NOTIFICATIONS_CODE_REQUEST_CODE
                )
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } != PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_POST_NOTIFICATIONS_CODE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

}
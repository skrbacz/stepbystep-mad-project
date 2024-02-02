package com.example.mad_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mad_project.fragments.HomeFragment
import com.example.mad_project.fragments.ProfileFragment
import com.example.mad_project.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.buttom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.button_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.buttom_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
    // super.onBackPressed();
    // Not calling **super**, disables back button in current screen.
    }
}

package com.example.nmaazreminder.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Retrieve the NavHostFragment wrapper from activity_main.xml
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment

        // 2. Extract the actual navigation controller manager context
        val navController = navHostFragment.navController

        // 3. Automatically sync the bottom menu clicks with your navigation graph!
        binding.bottomNavigation.setupWithNavController(navController)

//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.nav_prayers,
//                R.id.nav_qibla,
//                R.id.nav_tasbeeh,
//                R.id.nav_settings -> {
//                    binding.bottomNavigation.visibility = View.VISIBLE
//                }
//
//                else -> {
//                    binding.bottomNavigation.visibility = View.GONE
//                }
//            }
//        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_prayers,
                R.id.nav_qibla,
                R.id.nav_tasbeeh,
                R.id.nav_settings -> showBottomNav()
                else -> hideBottomNav()
            }
        }
    }
    private fun showBottomNav() {
        if (binding.bottomNavigation.visibility == View.VISIBLE) return
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.bottomNavigation.animate()
            .translationY(0f)
            .setDuration(200)
            .setListener(null)
    }

    private fun hideBottomNav() {
        if (binding.bottomNavigation.visibility == View.GONE) return
        binding.bottomNavigation.animate()
            .translationY(binding.bottomNavigation.height.toFloat())
            .setDuration(200)
            .withEndAction { binding.bottomNavigation.visibility = View.GONE }
    }
}
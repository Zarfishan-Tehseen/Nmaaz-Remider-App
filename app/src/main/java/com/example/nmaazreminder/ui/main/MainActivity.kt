package com.example.nmaazreminder.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ActivityMainBinding

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
    }
}
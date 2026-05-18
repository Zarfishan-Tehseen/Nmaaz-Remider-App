package com.example.nmaazreminder.ui.main // Matches the package root seen in your data files

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nmaazreminder.R
import com.example.nmaazreminder.databinding.ActivityMainBinding
import com.example.nmaazreminder.ui.fragments.HomeFragment
import com.example.nmaazreminder.ui.fragments.QiblaFragment
import com.example.nmaazreminder.ui.fragments.TasbeehFragment
import com.example.nmaazreminder.ui.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Keep instances persistent in memory while the activity is alive to maintain screen state
    private val prayersFragment by lazy { HomeFragment() }
    private val qiblaFragment by lazy { QiblaFragment() }
    private val tasbeehFragment by lazy { TasbeehFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the default landing fragment immediately when the app boots
        if (savedInstanceState == null) {
            switchFragment(prayersFragment)
        }

        // Set up the listener to swap fragments on menu navigation selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_prayers -> {
                    switchFragment(prayersFragment)
                    true
                }
                R.id.nav_qibla -> {
                    switchFragment(qiblaFragment)
                    true
                }
                R.id.nav_tasbeeh -> {
                    switchFragment(tasbeehFragment)
                    true
                }
                R.id.nav_settings -> {
                    switchFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Replaces the content inside the FragmentContainerView smoothly
     */
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
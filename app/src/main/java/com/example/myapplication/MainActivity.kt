package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ActivityLifecycle", "onCreate called")

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom Toolbar as ActionBar
        val toolbar = binding.topAppBar // Ensure this ID matches your XML definition
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))

        // Set up the BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Configure the AppBar with navigation destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_top_class_toilet, R.id.navigation_restroom_nearby, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Apply full-screen layout
//        setFullScreenMode()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to add the language icon and options
        menuInflater.inflate(R.menu.language_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.english -> {
                setAppLocale("en")
                true
            }
            R.id.chinese -> {
                setAppLocale("zh")
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Proper back navigation
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart the activity to apply the language change
        recreate()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                onBackPressed()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun setFullScreenMode() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onStart() {
        super.onStart()
        Log.d("ActivityLifecycle", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ActivityLifecycle", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityLifecycle", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ActivityLifecycle", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ActivityLifecycle", "onDestroy called")
    }
}

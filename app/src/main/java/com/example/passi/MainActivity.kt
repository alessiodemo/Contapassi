package com.example.passi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.passi.data.repository.StepRepository
import com.example.passi.data.sensor.StepCounterManager
import com.example.passi.databinding.ActivityMainBinding
import com.example.passi.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var stepCounterManager: StepCounterManager

    private lateinit var homeViewModel: HomeViewModel



    private val requestActivityRecognitionPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startStepCounter()
            } else {
                Toast.makeText(
                    this,
                    "Senza il permesso 'Attività fisica' il conteggio passi non funziona",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        //val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_goals,R.id.navigation_stats, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val stepRepo = StepRepository(this)
        stepCounterManager = StepCounterManager(this, stepRepo)
        stepCounterManager.setListener { steps ->
            homeViewModel.updateSteps(steps)


        }

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()

        // ACTIVITY_RECOGNITION is required starting from Android 10 (API 29) to read
        // TYPE_STEP_COUNTER; below that the sensor is available without any permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startStepCounter()
        } else {
            requestActivityRecognitionPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    override fun onPause() {
        super.onPause()
        stepCounterManager.stop()
    }

    private fun startStepCounter() {
        val sensorAvailable = stepCounterManager.start()
        if (!sensorAvailable) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetSteps() {
        stepCounterManager.resetSteps()
    }
}
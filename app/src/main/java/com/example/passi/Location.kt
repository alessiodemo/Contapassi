package com.example.passi


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import java.util.*

class Location(private val activity: AppCompatActivity) {
    private val locationRequest: LocationRequest = createLocationRequest()
    val coordinates: MutableList<Double> = Collections.synchronizedList(ArrayList())

    private val ACTIVITY_RECOGNITION_PERMISSION = Manifest.permission.ACTIVITY_RECOGNITION

    fun acquirePosition(c: Context, t: TextView, w: ImageView) {
        val locationPermissionGranted = checkLocalPermission()
        val activityRecognitionPermissionGranted = checkActivityRecognitionPermission()

        if (locationPermissionGranted && activityRecognitionPermissionGranted) {
            if (isGPSEnabled()) {
                LocationServices.getFusedLocationProviderClient(activity)
                    .requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)

                            LocationServices.getFusedLocationProviderClient(activity)
                                .removeLocationUpdates(this)

                            if (locationResult.locations.size > 0) {
                                val index = locationResult.locations.size - 1

                                coordinates.add(locationResult.locations[index].latitude)
                                coordinates.add(locationResult.locations[index].longitude)

                                val url = "https://api.open-meteo.com/v1/forecast?latitude=${coordinates[0]}&longitude=${coordinates[1]}&hourly=temperature_2m,weathercode&forecast_days=1&timezone=Europe%2FBerlin"
                                val httpRequest = HttpWeatherRequest(c, t, w)
                                httpRequest.execute(url)
                            }
                        }
                    }, Looper.getMainLooper())
            } else {
                val permissions = ArrayList<String>()
                if (!locationPermissionGranted) {
                    permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                if (!activityRecognitionPermissionGranted) {
                    permissions.add(ACTIVITY_RECOGNITION_PERMISSION)
                }
                ActivityCompat.requestPermissions(
                    activity,
                    permissions.toTypedArray(),
                    1
                )
            }
        } else {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, ACTIVITY_RECOGNITION_PERMISSION),
                1
            )
        }
    }


    private fun createLocationRequest(): LocationRequest {
        // Create and configure the LocationRequest object
        val locationRequest = LocationRequest()
        // Set the desired interval for location updates (in milliseconds)
        locationRequest.interval = 10000
        // Set the fastest interval for location updates (in milliseconds)
        locationRequest.fastestInterval = 5000
        // Set the priority of the location request
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun checkActivityRecognitionPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, ACTIVITY_RECOGNITION_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkLocalPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}

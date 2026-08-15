package com.example.passi.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class LocationProvider(private val context: Context) {

     companion object {
         private const val MAX_CACHE_AGE_MS = 30 * 60 * 1000L
     }

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getPosition(): Location? {
        if(!hasPermission()) return null
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        return try {
            val cached = fused.lastLocation.await()
            if (cached != null && System.currentTimeMillis() - cached.time < MAX_CACHE_AGE_MS) {
                    return cached
            }

            fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            cts.cancel()
        }
    }

    /*
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
    */


}
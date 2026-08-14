package com.example.passi.data.repository

import android.content.Context
import com.example.passi.data.location.LocationProvider
import com.example.passi.data.network.RetrofitClient

class WeatherRepository(context: Context) {

    private val locationProvider = LocationProvider(context)

    suspend fun getCurrentTemperature(): Double? {
        val location = locationProvider.getCurrentLocation() ?: return null
        return try {
            val response = RetrofitClient.weatherApi.getCurrentWeather(
                location.latitude,
                location.longitude
            )
            response.currentWeather.temperature
        } catch (e: Exception) {
            null
        }
    }
}
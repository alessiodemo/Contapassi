package com.example.passi.data.network
import com.google.gson.annotations.SerializedName

data class WeatherResponse (
    @SerializedName("current_weather")
    val currentWeather : CurrentWeather
)

data class CurrentWeather (
    val temperature: Double
)

package com.example.passi.core.weather

import androidx.annotation.DrawableRes
import com.example.passi.R

data class WeatherData(
    val temperatureC: Int,
    val weatherCode: Int
)

@DrawableRes
fun weatherIcon(weatherCode: Int): Int = when (weatherCode) {
    0, 1 -> R.drawable.sunny
    2, 3 -> R.drawable.cloudy
    45, 48 -> R.drawable.foggy
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99
        -> R.drawable.rain
    71, 73, 75, 77, 85, 86 -> R.drawable.snow
    else -> R.drawable.no_meteo
}

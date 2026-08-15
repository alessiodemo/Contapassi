package com.example.passi.core.weather

import android.content.Context
import com.example.passi.core.utility.Utility
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class WeatherRepository(private val context: Context) {
    private val ut = Utility()

    suspend fun fetch(latitude: Double, longitude: Double): WeatherData? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url =URL(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$latitude&longitude=$longitude" + "&current=temperature_2m,weather_code&timezone=auto"
            )
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
            }
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val dati = WeatherData(
                temperatureC = current.getDouble("temperature_2m").roundToInt(),
                weatherCode = current.getInt("weather_code")
            )

            salva(dati)
            dati
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun salva(dati: WeatherData) {
        ut.saveData(context, "temperatura",
            dati.temperatureC.toFloat())
        ut.saveData(context, "meteo", dati.weatherCode.toFloat())
    }
}

package com.example.passi.core.weather

import android.content.Context
import android.os.AsyncTask
import android.widget.ImageView
import android.widget.TextView
import com.example.passi.R
import com.example.passi.core.utility.Utility
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.math.roundToInt

class HttpWeatherRequest(c: Context, t: TextView?, w: ImageView?) : AsyncTask<String, Void, String>() {
    private val cont = c
    private val temp = t
    private val weath = w

    override fun doInBackground(vararg urls: String): String {
        val url = URL(urls[0])
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            val inputStream = urlConnection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                response.append(line)
                line = reader.readLine()
            }
            return response.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            urlConnection.disconnect()
        }

        return ""
    }

    override fun onPostExecute(result: String) {
        //ottieni gli oggetti JSON necessari
        val JSONogg = JSONObject(result)
        val hourlyOgg = JSONogg.getJSONObject("hourly")
        val temperatureArray = hourlyOgg.getJSONArray("temperature_2m")
        val weatherCodeArray = hourlyOgg.getJSONArray("weathercode")
        //ottieni l'ora attuale
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        //prendi i valori corrispondenti all'ora attuale
        val temperatura = temperatureArray.getDouble(currentHour).roundToInt().toString()
        val meteo = weatherCodeArray.getInt(currentHour).toString()
        //imposta la textView della temperatura se presente
        if(temp != null){
            temp.text = temperatura + " °C"
        }
        //imposta l'immagine del meteo se presente
        if(weath != null){
            val ut = Utility()
            ut.saveData(cont, "meteo", meteo.toFloat())
            ut.saveData(cont, "temperatura", temperatura.toFloat())
            when(meteo){
                "0","1" -> weath.setImageResource(R.drawable.sunny)
                "2","3" -> weath.setImageResource(R.drawable.cloudy)
                "45", "48" -> weath.setImageResource(R.drawable.foggy)
                "51", "53", "55", "56", "57", "61", "63", "65", "66", "67", "80", "81", "82", "95", "96", "99" -> weath.setImageResource(
                    R.drawable.rain)
                "71", "73", "75", "77", "85", "86" -> weath.setImageResource(R.drawable.snow)
                else -> weath.setImageResource(R.drawable.no_meteo)
            }
        }
    }
}
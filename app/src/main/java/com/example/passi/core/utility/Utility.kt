package com.example.passi.core.utility

import android.content.Context
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utility {
    fun getDataOggi(): Date {
        val calendario = Calendar.getInstance() //ottieni il calendario
        val dataOdierna = calendario.time //ottieni la data di oggi
        return dataOdierna;
    }

    fun dataSGMA(data: Date?): String? {
        val formatoData = SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale.getDefault()
        ) //crea il formato per la data
        return data?.let { formatoData.format(it) } //ritorna la data formattata
    }

    fun dataMeseAbbreviato(data: Date?): String? {
        val formatoData =
            SimpleDateFormat("MMM", Locale.getDefault()) //crea il formato per estrarre il mese
        return data?.let { formatoData.format(it) } //ritorna il mese
    }

    fun dataGiorno(data: Date?): Int? {
        val formatoData =
            SimpleDateFormat("dd", Locale.getDefault()) //crea il formato per estrarre il giorno
        return data?.let { formatoData.format(it).toInt() } //ritorna il giorno
    }

    fun getProgress(passi: Int, obiettivo: Int): Int {
        return (passi* 100/obiettivo)
    }

    fun getCalories(passi: Int): String {
        return formatTreCifre(passi * 0.045)
    }

    fun getDistance(passi: Int, altezza: Int): String {

        if(altezza >=170)
            return formatTreCifre(passi * 0.000762)

        else
            return formatTreCifre(passi * 0.0006)
    }

    fun formatTreCifre(num: Double): String{
        val decimalFormat = DecimalFormat()
        if (num < 10) {
            decimalFormat.applyPattern("#.##")
        } else if (num < 100) {
            decimalFormat.applyPattern("#.#")
        } else {
            decimalFormat.applyPattern("#")
        }
        return decimalFormat.format(num)
    }

    fun saveData(context: Context, key: String, value: Float) {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun loadData(context: Context, key: String): Float {
        val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat(key, 0f)
    }
}
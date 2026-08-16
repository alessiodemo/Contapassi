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
        return if (obiettivo == 0)  0 else passi* 100/obiettivo
    }

    /**
     * Distanza in km. La lunghezza del passo si stima come frazione dell'altezza:
     * il coefficiente 0,415 e' quello ACSM, ricavato da uno studio su 1.000 adulti
     * (0,413 per le donne: differenza dello 0,5%, ignorata perche' l'app non chiede il sesso).
     *
     * L'altezza arriva in centimetri, quindi passi * cm da' centimetri: /100_000 porta in km.
     * E' l'unita' che mancava nella versione precedente.
     */
    private fun distanzaKm(passi: Int, altezza: Int): Double =
        passi * altezza * 0.415 / 100_000

    fun getDistance(passi: Int, altezza: Int): String =
        formatTreCifre(distanzaKm(passi, altezza))

    /**
     * Calorie **nette**, cioe' quelle spese in piu' rispetto allo stare fermi:
     * e' la convenzione degli "active calories" di Fitbit e Apple Health, e non
     * accredita all'utente il metabolismo basale che consumerebbe comunque.
     *
     * Il peso non moltiplica i passi ma la distanza: camminare costa ~0,5 kcal
     * per kg di massa corporea per km. Verifica col metodo MET, a 4,8 km/h:
     * camminata in piano = 3,5 MET, 1 MET = 1 kcal/kg/h, quindi
     * (3,5 - 1) * peso / 4,8 = 0,52 kcal/kg/km. Le due strade coincidono.
     */
    fun getCalories(passi: Int, altezza: Int, peso: Int): String =
        formatTreCifre(distanzaKm(passi, altezza) * peso * 0.5)

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
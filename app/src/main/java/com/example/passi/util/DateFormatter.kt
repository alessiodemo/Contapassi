package com.example.passi.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateFormatter {
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
}
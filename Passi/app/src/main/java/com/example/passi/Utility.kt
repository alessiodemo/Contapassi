package com.example.passi

import java.text.SimpleDateFormat
import java.util.*

class Utility {
    fun getPassiObiettivo(dataOggi: Date): Int {
        return 7000
    }

    fun getPassiGiorno(dataOggi: Date): Int {
        return 2500
    }

    fun getDataOggi(): Date {
        val calendario = Calendar.getInstance() //ottieni il calendario
        val dataOdierna = calendario.time //ottieni la data di oggi
        return dataOdierna;
    }

    fun dataSGMA(data: Date?): String? {
        val formatoData = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) //crea il formato per la data
        return data?.let { formatoData.format(it) } //ritorna la data formattata
    }

    fun dataMeseAbbreviato(data: Date?): String? {
        val formatoData = SimpleDateFormat("MMM", Locale.getDefault()) //crea il formato per estrarre il mese
        return data?.let { formatoData.format(it) } //ritorna il mese
    }

    fun dataGiorno(data: Date?): Int? {
        val formatoData = SimpleDateFormat("dd", Locale.getDefault()) //crea il formato per estrarre il giorno
        return data?.let { formatoData.format(it).toInt() } //ritorna il giorno
    }
}
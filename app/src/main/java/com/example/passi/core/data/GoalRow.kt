package com.example.passi.core.data

import java.util.*
import com.example.passi.core.utility.Utility

val ut = Utility()

class GoalRow(d: Date, p: Int, o: Int, km: Double, kc: Double) {
    val passi = p
    val obiettivo = o
    val mese = ut.dataMeseAbbreviato(d)
    val giorno = ut.dataGiorno(d)
    val done = passi >= obiettivo
    val kcal = ut.formatTreCifre(kc)
    val distanza = ut.formatTreCifre(km)
}

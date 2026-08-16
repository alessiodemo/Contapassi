package com.example.passi.core.data

import java.util.*
import com.example.passi.core.utility.Utility

val ut = Utility()

class GoalRow(d: Date, p: Int, o: Int, a: Int, pe: Int) {
    val passi = p
    val obiettivo = o
    val peso = pe
    val mese = ut.dataMeseAbbreviato(d)
    val giorno = ut.dataGiorno(d)
    val done = passi >= obiettivo
    val kcal = ut.getCalories(passi, a, peso)
    val distanza = ut.getDistance(passi, a)
}

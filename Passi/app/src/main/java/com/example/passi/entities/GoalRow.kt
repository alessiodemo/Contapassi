package com.example.passi.entities

import java.util.*
import com.example.passi.Utility
import java.text.DecimalFormat

val ut = Utility()

class GoalRow(d: Date, p: Int, o: Int, k: Int, m: Int) {
    val passi = p
    val obiettivo = o
    val mese = ut.dataMeseAbbreviato(d)
    val giorno = ut.dataGiorno(d)
    val done = passi >= obiettivo
    val kcal = k
    val distanza = DecimalFormat("#.###").format(0.001*m)
}

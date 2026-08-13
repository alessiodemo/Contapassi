package com.example.passi.data.model

import java.util.*
import com.example.passi.util.DateFormatter
import java.text.DecimalFormat

val ut = DateFormatter()

class GoalRow(d: Date, p: Int, o: Int, k: Int, m: Int) {
    val passi = p
    val obiettivo = o
    val mese = ut.dataMeseAbbreviato(d)
    val giorno = ut.dataGiorno(d)
    val done = passi >= obiettivo
    val kcal = k
    val distanza = DecimalFormat("#.###").format(0.001*m)
}

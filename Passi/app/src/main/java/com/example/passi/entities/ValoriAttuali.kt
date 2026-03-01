package com.example.passi.entities
import com.example.passi.Utility
import java.util.Date

object ValoriAttuali {
    private val ut = Utility()
    private var data : Date = ut.getDataOggi()
    private var passi : Int = ut.getPassiGiorno(data)
    private var obiettivo : Int = ut.getPassiObiettivo(data)
    /*private var kcal : Int = ut.getKcal(data)
    private var*/

    fun getPassi(): Int {
        return passi
    }

    fun getObiettivo(): Int {
        return obiettivo
    }

    fun getData(): Date {
        return data
    }
}
package com.example.passi.entities
import android.content.Context
import com.example.passi.Utility
import com.example.passi.Database
import java.text.SimpleDateFormat
import java.util.Date


object ValoriAttuali {
    val ut = Utility()
    var data = ut.getDataOggi()
    var passi : Int = 0
    var obiettivo : Int = 0
    var altezza : Int = 0

    fun initialize(context: Context){
        val db = Database(context)
        db.open()
        val formatoData = SimpleDateFormat("yyyyMMdd")
        val chiave = formatoData.format(data)
        if(!db.contains(chiave)){
            db.inserisciTuplaSteps(0,0,0)
        }
        val valori = db.getValuesFromKey(chiave)
        passi = valori[0]
        obiettivo = valori[1]
        altezza = valori[2]
    }
}
package com.example.passi.core.data
import android.content.Context
import com.example.passi.core.utility.Utility
import java.text.SimpleDateFormat


object ValoriAttuali {
    val ut = Utility()
    var data = ut.getDataOggi()
    var passi : Int = 0
    var obiettivo : Int = 0
    var altezza : Int = 0

    suspend fun initialize(context: Context){
        val repository = StepRepository(AppDatabase.getInstance(context).stepDao())
        val chiave = SimpleDateFormat("yyyyMMdd").format(data)
        if(!repository.contains(chiave)) repository.inserisciTuplaSteps(0,0,0)

        val valori = repository.getValueFromKey(chiave)
        passi = valori[0]
        obiettivo = valori[1]
        altezza = valori[2]
    }
}
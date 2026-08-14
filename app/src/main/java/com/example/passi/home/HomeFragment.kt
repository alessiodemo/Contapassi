package com.example.passi.home

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.passi.core.data.Database
import com.example.passi.core.location.LocationProvider
import com.example.passi.MainActivity
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.utility.Utility


class HomeFragment : Fragment() {

    private val ut = Utility()

    companion object {
        fun newInstance() = HomeFragment()
    }

    val model: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //richiama il database
        val db = Database(requireContext())

        //crea l'observer che aggiorna l'interfaccia
        val obs = Observer<Boolean> { valori ->
            //aggiorna la data
            view.findViewById<TextView>(R.id.giorno).text = ut.dataSGMA(ut.getDataOggi())
            //recupera i passi dal database e aggiornali
            val key = db.formatKey(ut.getDataOggi())
            val valoriPassi = db.getValuesFromKey(key)
            view.findViewById<TextView>(R.id.passiOggi).text = valoriPassi[0].toString()
            view.findViewById<TextView>(R.id.passiObiettivo).text = "/" + valoriPassi[1].toString()
            view.findViewById<ProgressBar>(R.id.passiProgress).setProgress(ut.getProgress(valoriPassi[0], valoriPassi[1]), true)
            view.findViewById<TextView>(R.id.calorieTesto).text = ut.getCalories(valoriPassi[0]) + " kcal"
            view.findViewById<TextView>(R.id.kmTesto).text = ut.getDistance(valoriPassi[0], valoriPassi[2]) + " km"
            view.findViewById<ProgressBar>(R.id.OMSProgress).setProgress(ut.getProgress(valoriPassi[0], 10000), true)

            if(ut.getProgress(valoriPassi[0], 10000) >= 100){
                view.findViewById<TextView>(R.id.OMS).text = "Complimenti! Un altro passo verso una vita più sana :)"
                view.findViewById<TextView>(R.id.OMS).textSize = 18F
                view.findViewById<TextView>(R.id.OMS).setTextColor(Color.parseColor("#E64E1B"))
            }
        }

        //collega l'observer
        model.getData().observe(viewLifecycleOwner, obs)

        //attiva la prima modifica
        model.setData(true)

        //crea l'observer che aggiorna l'interfaccia meteo
        val obsMeteo = Observer<Boolean> { valori ->
            //richiedi la posizione e il meteo
            val loc = LocationProvider((activity as MainActivity?)!!)
            loc.acquirePosition(requireContext(),view.findViewById(R.id.meteoTesto),view.findViewById(R.id.meteoIcona))
        }

        //collega l'observer
        model.getMeteo().observe(viewLifecycleOwner, obsMeteo)

        //attiva la prima modifica
        model.setMeteo(true)



        startRepeatedTask()
    }

    private fun startRepeatedTask() {
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                model.setData(true)
                handler.postDelayed(this, 2000)
            }
        }
        handler.postDelayed(runnable, 2000)
    }

}
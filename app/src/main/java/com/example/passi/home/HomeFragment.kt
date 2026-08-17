package com.example.passi.home

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.passi.core.location.LocationProvider
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import com.example.passi.core.weather.WeatherRepository
import com.example.passi.core.weather.weatherIcon
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private val ut = Utility()

    companion object {
        fun newInstance() = HomeFragment()
    }

    // condiviso con SettingsFragment tramite l'Activity: salvando altezza o peso,
    // Home si aggiorna subito invece di aspettare il prossimo giro di polling
    val model: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //richiama il database
        val repository  = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())

        //crea l'observer che aggiorna l'interfaccia
        val obs = Observer<Boolean> { _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                //aggiorna la data
                view.findViewById<TextView>(R.id.giorno).text = ut.dataSGMA(ut.getDataOggi())
                //recupera i passi dal database e aggiornali
                val key = repository.formatKey(ut.getDataOggi())
                val riga = repository.getRow(key)
                view.findViewById<TextView>(R.id.passiOggi).text = riga.steps.toString()
                view.findViewById<TextView>(R.id.passiObiettivo).text =
                    getString(R.string.formato_obiettivo, riga.goal)
                // setProgressCompat e' l'equivalente Material di setProgress(valore, true):
                // anima la transizione usando le animazioni dell'indicatore M3
                view.findViewById<CircularProgressIndicator>(R.id.passiProgress)
                    .setProgressCompat(ut.getProgress(riga.steps, riga.goal), true)
                view.findViewById<TextView>(R.id.calorieTesto).text =
                    getString(R.string.formato_kcal, ut.formatTreCifre(riga.kcal))
                view.findViewById<TextView>(R.id.kmTesto).text =
                    getString(R.string.formato_km, ut.formatTreCifre(riga.distanzaKm))
                view.findViewById<LinearProgressIndicator>(R.id.OMSProgress)
                    .setProgressCompat(ut.getProgress(riga.steps, 10000), true)

                if (ut.getProgress(riga.steps, 10000) >= 100) {
                    view.findViewById<TextView>(R.id.OMS).text =
                        getString(R.string.complimenti_oms)
                    view.findViewById<TextView>(R.id.OMS).textSize = 18F
                    // colore preso dal tema, non cablato: con i colori dinamici
                    // l'arancione fisso stonerebbe col resto della schermata
                    view.findViewById<TextView>(R.id.OMS).setTextColor(
                        MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimary)
                    )
                }
            }
        }

        //collega l'observer
        model.getData().observe(viewLifecycleOwner, obs)

        //attiva la prima modifica
        model.setData(true)

        //crea l'observer che aggiorna l'interfaccia meteo
        val obsMeteo = Observer<Boolean> {
            viewLifecycleOwner.lifecycleScope.launch {
                val pos = LocationProvider(requireContext()).getPosition()
                    ?: return@launch
                val meteo = WeatherRepository(requireContext())
                    .fetch(pos.latitude, pos.longitude) ?: return@launch

                view.findViewById<TextView>(R.id.meteoTesto).text =
                    getString(R.string.formato_gradi, meteo.temperatureC)
                view.findViewById<ImageView>(R.id.meteoIcona)
                    .setImageResource(weatherIcon(meteo.weatherCode))
            }
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
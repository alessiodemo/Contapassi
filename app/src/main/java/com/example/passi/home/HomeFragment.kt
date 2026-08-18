package com.example.passi.home

import android.os.Bundle
import android.util.TypedValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.passi.core.location.LocationProvider
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import com.example.passi.core.weather.WeatherRepository
import com.example.passi.core.weather.weatherIcon
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private val ut = Utility()

    companion object {
        fun newInstance() = HomeFragment()
    }

    // resta per il meteo, che non nasce dal database e quindi non ha un Flow da
    // osservare: e' una chiamata di rete innescata a mano
    val model: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())

        // risolte una volta sola: il blocco sotto rigira a ogni scrittura sul database
        val giorno = view.findViewById<TextView>(R.id.giorno)
        val passiOggi = view.findViewById<TextView>(R.id.passiOggi)
        val passiObiettivo = view.findViewById<TextView>(R.id.passiObiettivo)
        val passiProgress = view.findViewById<CircularProgressIndicator>(R.id.passiProgress)
        val calorieTesto = view.findViewById<TextView>(R.id.calorieTesto)
        val kmTesto = view.findViewById<TextView>(R.id.kmTesto)
        val omsProgress = view.findViewById<LinearProgressIndicator>(R.id.OMSProgress)
        val oms = view.findViewById<TextView>(R.id.OMS)
        // letti dalla view appena gonfiata, prima che qualcuno li modifichi: cosi' il
        // ripristino non ricabla a mano valori che appartengono allo stile
        val omsDimensioneDefault = oms.textSize
        val omsColoreDefault = oms.currentTextColor

        /*
         * Qui prima c'era un Handler che ogni 2 secondi rileggeva l'intera riga dal
         * database, che ci fossero passi nuovi o no. Ora e' Room a dire quando c'e'
         * qualcosa da rileggere: osservaRiga() e' registrata sull'InvalidationTracker
         * della tabella, quindi la scrittura del ForegroundService o il salvataggio da
         * Settings arrivano qui da soli.
         *
         * repeatOnLifecycle(STARTED) avvia la raccolta quando la schermata diventa
         * visibile e la cancella quando smette di esserlo: niente letture in background
         * e niente Runnable orfani, perche' la coroutine muore col ciclo di vita della
         * view invece di sopravvivergli.
         *
         * La chiave del giorno si ricalcola dentro il blocco e non una volta sola:
         * riaprendo l'app dopo la mezzanotte si finisce a osservare la riga nuova.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val key = repository.formatKey(ut.getDataOggi())
                // crea la riga di oggi se manca, altrimenti il Flow emetterebbe null
                // finche' il servizio non registra il primo passo
                repository.getRow(key)
                giorno.text = ut.dataSGMA(ut.getDataOggi())

                repository.osservaRiga(key).filterNotNull().collect { riga ->
                    passiOggi.text = riga.steps.toString()
                    passiObiettivo.text = getString(R.string.formato_obiettivo, riga.goal)
                    // setProgressCompat e' l'equivalente Material di setProgress(valore, true):
                    // anima la transizione usando le animazioni dell'indicatore M3
                    passiProgress.setProgressCompat(ut.getProgress(riga.steps, riga.goal), true)
                    calorieTesto.text =
                        getString(R.string.formato_kcal, ut.formatTreCifre(riga.kcal))
                    kmTesto.text = getString(R.string.formato_km, ut.formatTreCifre(riga.distanzaKm))
                    omsProgress.setProgressCompat(ut.getProgress(riga.steps, 10000), true)

                    if (ut.getProgress(riga.steps, 10000) >= 100) {
                        oms.text = getString(R.string.complimenti_oms)
                        oms.textSize = 18F
                        // colore preso dal tema, non cablato: con i colori dinamici
                        // l'arancione fisso stonerebbe col resto della schermata
                        oms.setTextColor(
                            MaterialColors.getColor(
                                view,
                                com.google.android.material.R.attr.colorPrimary
                            )
                        )
                    } else {
                        // ramo simmetrico: senza, il testo ingrandito e colorato
                        // resterebbe anche dopo il cambio di giorno, quando i passi
                        // ripartono da zero ma la view e' ancora la stessa
                        oms.setText(R.string.passiStandard)
                        oms.setTextSize(TypedValue.COMPLEX_UNIT_PX, omsDimensioneDefault)
                        oms.setTextColor(omsColoreDefault)
                    }
                }
            }
        }

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
    }

}

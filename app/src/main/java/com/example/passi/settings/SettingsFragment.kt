package com.example.passi.settings

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.materialswitch.MaterialSwitch
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.passi.BuildConfig
import com.example.passi.core.service.ForegroundService
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import com.example.passi.core.widget.StepsWidget
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(), SensorEventListener {

    // activityViewModels, non viewModels: con viewModels ogni Fragment ottiene la PROPRIA
    // istanza, quindi il setData() fatto qui non arrivava mai a HomeFragment. Home si
    // aggiornava solo grazie al polling ogni 2 secondi, cioe' per caso.
    val model: SharedViewModel by activityViewModels()
    val ut = Utility()

    private var sensorManager: SensorManager? = null
    private var passiIniziali = -1
    private var passiContati = 0
    private var inCalibrazione = false

    companion object {
        fun newInstance() = SettingsFragment()
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val repository = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())

        val bottoneAvvia = view.findViewById<Button>(R.id.avviaCalibrazione)
        bottoneAvvia.setOnClickListener {
            if (inCalibrazione) {
                fermaCalibrazione()
                bottoneAvvia.setText(R.string.calibrazione_avvia)
            } else {
                avviaCalibrazione()
                bottoneAvvia.setText(R.string.calibrazione_ferma)
            }
        }

        //crea l'observer che aggiorna l'interfaccia
        val obs = Observer<Boolean> { _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                //recupera i obiettivo e altezza dal database e aggiornali
                val key = repository.formatKey(ut.getDataOggi())
                val riga = repository.getRow(key)
                val calibrato = ut.loadData(requireContext(), "lunghezzaPasso")
                val passo = ut.lunghezzaPassoCm(requireContext(), riga.height)
                view.findViewById<TextView>(R.id.passoAttuale).text = getString(
                    if (calibrato > 0) R.string.passo_calibrato else R.string.passo_stimato,
                    ut.formatTreCifre(passo))
                view.findViewById<EditText>(R.id.inputObiettivo).setText(riga.goal.toString())
                view.findViewById<EditText>(R.id.inputAltezza).setText(riga.height.toString())
                view.findViewById<EditText>(R.id.inputPeso).setText(riga.weight.toString())
            }
        }

            //collega l'observer
            model.getData().observe(viewLifecycleOwner, obs)

            //attiva la prima modifica
            model.setData(true)

            //imposta le azioni alla modifica dell'obiettivo e dell'altezza
            view.findViewById<Button>(R.id.aggiornaValori).setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val altezza = view.findViewById<EditText>(R.id.inputAltezza).text.toString().toIntOrNull()
                    val peso = view.findViewById<EditText>(R.id.inputPeso).text.toString().toIntOrNull()
                    val obiettivo = view.findViewById<EditText>(R.id.inputObiettivo).text.toString().toIntOrNull()

                    if (altezza == null || peso == null || obiettivo == null ||
                        altezza <= 0 || peso <= 0 || obiettivo <= 0) {
                        Toast.makeText(requireContext(), R.string.valori_non_validi,
                            Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val key = repository.formatKey(ut.getDataOggi())
                    repository.updateHeight(key, altezza)
                    repository.updateGoal(key, obiettivo)
                    repository.updateWeight(key, peso)

                    // Il widget legge il database ma nessuno lo avvisa quando cambia:
                    // senza questa chiamata continuerebbe a mostrare km e kcal calcolati
                    // con i valori vecchi fino al primo passo rilevato dal servizio, o
                    // fino al giro giornaliero di updatePeriodMillis. Esce subito se non
                    // ci sono widget installati.
                    StepsWidget.aggiornaTuttiIWidget(requireContext())

                    model.setData(true)
                    Toast.makeText(
                        requireContext(), R.string.modifiche_salvate, Toast.LENGTH_SHORT
                    ).show()
                }
            }

            //imposta le azioni alla modifica dello switch
            // MaterialSwitch NON estende android.widget.Switch ma SwitchCompat:
            // il tipo va aggiornato qui, altrimenti findViewById esplode in ClassCastException
            view.findViewById<MaterialSwitch>(R.id.raccolta_dati_switch)
                .setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        val intent = Intent(requireContext(), ForegroundService::class.java)
                        startForegroundService(requireContext(), intent)
                    } else {
                        val intentStop = Intent(requireContext(), ForegroundService::class.java)
                        intentStop.action = ACTION_STOP
                        startForegroundService(requireContext(), intentStop)
                    }
                }

            view.findViewById<Button>(R.id.salvaCalibrazione).setOnClickListener {
                if (inCalibrazione) {
                    fermaCalibrazione()
                    bottoneAvvia.setText(R.string.calibrazione_avvia)
                }

                val testo = view.findViewById<EditText>(R.id.inputMetri).text.toString().replace(',', '.')
                val metri = testo.toDoubleOrNull()

                if (metri == null || metri < 20 || passiContati < 20) {
                    Toast.makeText(requireContext(), R.string.calibrazione_troppo_corta,
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val passoCm = metri * 100 / passiContati

                if (passoCm < 30 || passoCm > 110) {
                    Toast.makeText(requireContext(), R.string.calibrazione_fuori_range,
                        Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                ut.saveData(requireContext(), "lunghezzaPasso", passoCm.toFloat())
                model.setData(true)
                Toast.makeText(requireContext(),
                    getString(R.string.calibrazione_salvata, ut.formatTreCifre(passoCm)),
                    Toast.LENGTH_LONG).show()
            }

            view.findViewById<Button>(R.id.azzeraCalibrazione).setOnClickListener {
                ut.saveData(requireContext(), "lunghezzaPasso", 0f)
                model.setData(true)
            }


        //imposta lo stato iniziale dello switch
            val status = isForegroundServiceRunning(ForegroundService::class.java)
            view.findViewById<MaterialSwitch>(R.id.raccolta_dati_switch).isChecked = status
    }

    //controlla se il foreground service è attivo
    private fun isForegroundServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in runningServices) {
            val className = serviceInfo.service.className
            if (className == serviceClass.name) {
                return true
            }
        }
        return false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val conteggio = event?.values?.get(0)?.toInt() ?: return
        if (passiIniziali < 0) passiIniziali = conteggio
        passiContati = conteggio - passiIniziali
        view?.findViewById<TextView>(R.id.passiCalibrazione)?.text = passiContati.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun avviaCalibrazione() {
        passiIniziali = -1
        passiContati = 0
        val sensore = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        sensorManager?.registerListener(this, sensore, SensorManager.SENSOR_DELAY_UI)
        inCalibrazione = true
    }

    private fun fermaCalibrazione() {
        sensorManager?.unregisterListener(this)
        inCalibrazione = false

    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager?.unregisterListener(this)
    }


}
package com.example.passi.settings

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.passi.BuildConfig
import com.example.passi.core.service.ForegroundService
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    val model: SharedViewModel by viewModels()
    val ut = Utility()

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

        val repository = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())

        //crea l'observer che aggiorna l'interfaccia
        val obs = Observer<Boolean> { _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                //recupera i obiettivo e altezza dal database e aggiornali
                val key = repository.formatKey(ut.getDataOggi())
                val valoriPassi = repository.getValueFromKey(key)
                view.findViewById<EditText>(R.id.inputObiettivo).setText(valoriPassi[1].toString())
                view.findViewById<EditText>(R.id.inputAltezza).setText(valoriPassi[2].toString())
            }
        }

            //collega l'observer
            model.getData().observe(viewLifecycleOwner, obs)

            //attiva la prima modifica
            model.setData(true)

            //imposta le azioni alla modifica dell'obiettivo e dell'altezza
            view.findViewById<Button>(R.id.aggiornaValori).setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val key = repository.formatKey(ut.getDataOggi())
                    repository.updateHeight(
                        key,
                        view.findViewById<EditText>(R.id.inputAltezza).text.toString().toInt()
                    )
                    repository.updateGoal(
                        key,
                        view.findViewById<EditText>(R.id.inputObiettivo).text.toString().toInt()
                    )
                    model.setData(true)
                    Toast.makeText(requireContext(), "Modifiche salvate", Toast.LENGTH_SHORT).show()
                }
            }

            //imposta le azioni alla modifica dello switch
            view.findViewById<Switch>(R.id.raccolta_dati_switch)
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

            //imposta lo stato iniziale dello switch
            val status = isForegroundServiceRunning(ForegroundService::class.java)
            view.findViewById<Switch>(R.id.raccolta_dati_switch).isChecked = status
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


}
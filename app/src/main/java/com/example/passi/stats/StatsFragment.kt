package com.example.passi.stats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.passi.R
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.data.ut
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    companion object {
        fun newInstance() = StatsFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repository  = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())


        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            view.findViewById<TextView>(R.id.mediaKmSettimana).text =
                getString(R.string.formato_km,
                    ut.formatTreCifre(repository.kmSettimana()))
            view.findViewById<TextView>(R.id.mediaKmMese).text =
                getString(R.string.formato_km, ut.formatTreCifre(repository.kmMese()))
            view.findViewById<TextView>(R.id.obiettiviRaggiunti).text =
                repository.goalsReached().toString()

            var days = mutableListOf<Int>()
            days =
                repository.getWeekSteps() //per riempire la lista dei valori da mettere nel grafico
            val mainLayout: FrameLayout = view.findViewById(R.id.graficoContainer)
            val histogramView = HistogramView(requireContext(), days)
            mainLayout.addView(histogramView)
        }
    }

}
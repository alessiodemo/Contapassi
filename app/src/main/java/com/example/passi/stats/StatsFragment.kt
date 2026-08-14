package com.example.passi.stats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.passi.core.data.Database
import com.example.passi.R

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
        super.onViewCreated(view, savedInstanceState)
        val db = Database(requireContext())
        view.findViewById<TextView>(R.id.mediaPassiSettimana).text = db.totalWeeklySteps().toString()
        view.findViewById<TextView>(R.id.mediaPassiMese).text = db.totalMonthlySteps().toString()
        view.findViewById<TextView>(R.id.obiettiviRaggiunti).text = db.goalsReached().toString()

        var days = mutableListOf<Int>()
        days = db!!.getWeekSteps() //per riempire la lista dei valori da mettere nel grafico
        val mainLayout: FrameLayout = view.findViewById(R.id.frame)
        val histogramView = HistogramView(requireContext(), days)
        mainLayout.addView(histogramView)
    }

}
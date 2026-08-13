package com.example.passi.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.passi.MainActivity
import com.example.passi.R
import com.example.passi.util.DateFormatter

class HomeFragment : Fragment() {

    private val ut = DateFormatter()

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.currentSteps.observe(viewLifecycleOwner) { steps ->
            view.findViewById<TextView>(R.id.passiOggi).text = "$steps"
        }
        view.findViewById<TextView>(R.id.passiOggi).setOnLongClickListener {
            (requireActivity() as MainActivity).resetSteps()
            true
        }


        //quando i dati giornalieri vengono modificati
        /*viewModel.datiGiornalieri.observe(viewLifecycleOwner, Observer {

        })*/
    }


}
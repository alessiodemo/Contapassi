package com.example.passi.home

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.passi.MainActivity
import com.example.passi.R
import com.example.passi.data.repository.StepRepository
import com.example.passi.data.repository.WeatherRepository
import com.example.passi.util.DateFormatter
import android.Manifest

class HomeFragment : Fragment() {

    private val ut = DateFormatter()

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            loadWeather()
        }
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

        val stepRepository = StepRepository(requireContext())
        view.findViewById<TextView>(R.id.passiObiettivo).text = "${stepRepository.getDailyGoal()}"

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.currentSteps.observe(viewLifecycleOwner) { steps ->
            view.findViewById<TextView>(R.id.passiOggi).text = "$steps"
        }
        view.findViewById<TextView>(R.id.passiOggi).setOnLongClickListener {
            (requireActivity() as MainActivity).resetSteps()
            true
        }

        homeViewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            view.findViewById<TextView>(R.id.meteoTesto).text = temperature
        }

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadWeather()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

    }
    private fun loadWeather() {
        val weatherRepository = WeatherRepository(requireContext())
        homeViewModel.loadWeather(weatherRepository)
    }

}
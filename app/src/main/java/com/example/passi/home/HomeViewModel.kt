package com.example.passi.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passi.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    //LiveData for the current steps
    private val _currentSteps = MutableLiveData<Int>()
    val currentSteps: LiveData<Int> = _currentSteps

    fun updateSteps(steps: Int) {
        _currentSteps.value = steps
    }

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> = _temperature
    private var weatherLoadStarted = false

    fun loadWeather(weatherRepository: WeatherRepository) {
        if (weatherLoadStarted) return
        weatherLoadStarted = true
        viewModelScope.launch {
            val temp = weatherRepository.getCurrentTemperature()
            _temperature.value = if (temp != null) "${temp.toInt()}° C" else "--° C"
        }
    }
}
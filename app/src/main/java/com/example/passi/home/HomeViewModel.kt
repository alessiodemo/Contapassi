package com.example.passi.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    //LiveData for the current steps
    private val _currentSteps = MutableLiveData<Int>()
    val currentSteps: LiveData<Int> = _currentSteps

    fun updateSteps(steps: Int) {
        _currentSteps.value = steps
    }
}
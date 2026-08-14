package com.example.passi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val modifiche = MutableLiveData<Boolean>()

    fun setData(value: Boolean) {
        modifiche.value = value
    }

    fun getData(): LiveData<Boolean> {
        return modifiche
    }

    private val meteo = MutableLiveData<Boolean>()

    fun setMeteo(value: Boolean){
        meteo.value = value
    }

    fun getMeteo(): LiveData<Boolean>{
        return meteo
    }
}
package com.example.passi.data.repository

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepRepository(context: Context) {

    private val appContext = context.applicationContext

    private fun todayAsString(): String =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    fun getPreviousTotalSteps(): Float {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        var previousTotalSteps = sharedPreferences.getFloat("key1", 0f)
        return previousTotalSteps
    }
    fun savePreviousTotalSteps(steps: Float) {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putFloat("key1", steps).apply()
    }
    fun getDailyGoal(): Int {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        var dailyGoal = sharedPreferences.getInt("key2", 7000)
        return dailyGoal
    }

    fun setDailyGoal(goal: Int) {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("key2", goal).apply()
    }

    fun isNewDay():Boolean {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs",
            Context.MODE_PRIVATE)
        val ultimoReset = sharedPreferences.getString("lastResetDate", "")
        return todayAsString() != ultimoReset
    }

    fun saveResetDate() {
        val sharedPreferences = appContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("lastResetDate", todayAsString()).apply()
    }

}
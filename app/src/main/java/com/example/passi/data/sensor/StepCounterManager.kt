package com.example.passi.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.passi.data.repository.StepRepository

class StepCounterManager(context: Context, private val repository: StepRepository) : SensorEventListener {

    fun interface Listener {
        fun onStepsChanged(currentSteps: Int)
    }

    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var listener: Listener? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    init {
        previousTotalSteps= repository.getPreviousTotalSteps()
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /** Registers the step sensor. Call from onResume. */
    fun start(): Boolean {
        running = true
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: return false

        // Rate suitable for the user interface
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        return true
    }

    /** Unregisters the step sensor. Call from onPause. */
    fun stop() {
        running = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]

            if(repository.isNewDay()) {
                previousTotalSteps = totalSteps
                repository.savePreviousTotalSteps(previousTotalSteps)
                repository.saveResetDate()
            }

            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            listener?.onStepsChanged(currentSteps)
        }
    }

    fun resetSteps() {
        previousTotalSteps = totalSteps
        repository.savePreviousTotalSteps(previousTotalSteps)
        listener?.onStepsChanged(0)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We do not have to write anything in this function for this app
    }
}
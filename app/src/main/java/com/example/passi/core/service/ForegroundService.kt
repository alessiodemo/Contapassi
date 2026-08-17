package com.example.passi.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.passi.BuildConfig
import com.example.passi.MainActivity
import com.example.passi.R
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import com.example.passi.core.widget.StepsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ForegroundService: Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() +
            Dispatchers.IO)
    private lateinit var repository: StepRepository
    private var sensorManager: SensorManager? = null

    private var ultimoConteggio = 0
    private var ultimoTimestamp = 0L

    companion object {
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
        const val SOGLIA_CORSA = 140.0
    }

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository(AppDatabase.getInstance(this).stepDao())
        ultimoConteggio = Utility().loadData(this, "passiPr").toInt()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, R.string.nessun_sensore, Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Il disegno del widget vive tutto in StepsWidget: qui c'era una copia della stessa
     * logica che pubblicava cinque RemoteViews di fila sullo stesso widget, e siccome
     * updateAppWidget sostituisce invece di aggiornare, sopravviveva solo l'ultima.
     */
    suspend fun updateWidget() {
        StepsWidget.aggiornaTuttiIWidget(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val e = event ?: return
        val conteggio = e.values[0].toInt()
        val timestamp = e.timestamp

        val precedente = ultimoConteggio
        val tsPrecedente = ultimoTimestamp
        ultimoConteggio = conteggio
        ultimoTimestamp = timestamp

        val deltaPassi = when {
            precedente == 0 -> 0
            conteggio < precedente -> conteggio
            else -> conteggio - precedente
        }

        val secondi = if (tsPrecedente > 0) (timestamp - tsPrecedente) / 1_000_000_000.0 else 0.0
        val corsa = deltaPassi >= 5 && secondi > 0 && deltaPassi / secondi * 60 >= SOGLIA_CORSA

        serviceScope.launch {
            val ut = Utility()
            ut.saveData(this@ForegroundService, "passiPr", conteggio.toFloat())
            if (deltaPassi <= 0) return@launch

            val key = repository.formatKey(ut.getDataOggi())
            val riga = repository.getRow(key)
            val altezza = riga.height
            val peso = riga.weight

            val passoCm = ut.lunghezzaPassoCm(this@ForegroundService, altezza)* (if (corsa) 1.55 else 1.0)
            val deltaKm = deltaPassi * passoCm / 100_000
            val deltaKcal = deltaKm * peso * (if (corsa) 1.0 else 0.5)

            repository.accumula(key, deltaPassi, deltaKm, deltaKcal)
            updateWidget()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //nulla, serve per implementare l'interfaccia richiesta
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if ((intent?.action != null) && intent.action.equals(ACTION_STOP, ignoreCase = true)) {
            stopForeground(true)
            stopSelf()
        }
        foregroundNotification()
        return START_STICKY
    }

    //notifica che avvisa del servizio
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 777

    private fun foregroundNotification() {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, PendingIntent.FLAG_IMMUTABLE)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager =
                    this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            assert(mNotificationManager != null)
            mNotificationManager?.createNotificationChannelGroup(
                NotificationChannelGroup("chats_group", "Chats")
            )
            val notificationChannel =
                NotificationChannel(
                    "service_channel", "Service Notifications",
                    NotificationManager.IMPORTANCE_MIN
                )
            notificationChannel.enableLights(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            mNotificationManager?.createNotificationChannel(notificationChannel)
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(
                StringBuilder(resources.getString(R.string.app_name)).append(" service is running")
                    .toString()
            )
                .setTicker(
                    StringBuilder(resources.getString(R.string.app_name)).append("service is running")
                        .toString()
                )
                .setContentText("Tocca per aprire")
                .setSmallIcon(R.drawable.walk_outline)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.black)
            notification = builder.build()
            startForeground(mNotificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
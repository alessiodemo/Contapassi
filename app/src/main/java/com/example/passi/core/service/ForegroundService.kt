package com.example.passi.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.passi.BuildConfig
import com.example.passi.MainActivity
import com.example.passi.R
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.Database
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
    private lateinit var context: Context

    companion object {
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository(AppDatabase.getInstance(this).stepDao())
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        context = this
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    suspend fun updateWidget(){
        val ut = Utility()
        val meteo = ut.loadData(context, "meteo").toInt().toString()
        val temperatura = ut.loadData(context, "temperatura").toString()
        val key = repository.formatKey(ut.getDataOggi())
        val valori = repository.getValueFromKey(key)
        val passiSettimana = repository.totalWeeklySteps().toString()
        val passiMese = repository.totalMonthlySteps().toString()
        val obiettivi = repository.goalsReached().toString()
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val stepsWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                applicationContext,
                StepsWidget::class.java
            )
        )
        for (widgetId in stepsWidgetIds) {
            //small
            val remoteViewsSmall = RemoteViews(packageName, R.layout.widget_layout_small)
            remoteViewsSmall.setTextViewText(R.id.textView, valori[0].toString())
            appWidgetManager.updateAppWidget(widgetId, remoteViewsSmall)
            //small1
            val remoteViewsSmall1 = RemoteViews(packageName, R.layout.widget_layout_small1)
            remoteViewsSmall1.setTextViewText(R.id.textView, valori[0].toString())
            remoteViewsSmall1.setTextViewText(R.id.km, ut.getDistance(valori[0],valori[2]))
            appWidgetManager.updateAppWidget(widgetId, remoteViewsSmall1)
            //small2
            val remoteViewsSmall2 = RemoteViews(packageName, R.layout.widget_layout_small2)
            remoteViewsSmall2.setTextViewText(R.id.textView3, valori[0].toString())
            remoteViewsSmall2.setTextViewText(R.id.textView4, ut.getDistance(valori[0],valori[2]))
            appWidgetManager.updateAppWidget(widgetId, remoteViewsSmall2)
            //normal
            val remoteViewsNormal = RemoteViews(packageName, R.layout.widget_layout_normal)
            remoteViewsNormal.setTextViewText(R.id.textView5, valori[0].toString())
            remoteViewsNormal.setTextViewText(R.id.textView6, ut.getCalories(valori[0]))
            remoteViewsNormal.setTextViewText(R.id.textView7, temperatura+" °C" )
            when(meteo){
                "0","1" -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.sunny,0,0,0)
                "2","3" -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.cloudy,0,0,0)
                "45", "48" -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.foggy,0,0,0)
                "51", "53", "55", "56", "57", "61", "63", "65", "66", "67", "80", "81", "82", "95", "96", "99" -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.rain,0,0,0)
                "71", "73", "75", "77", "85", "86" -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.snow,0,0,0)
                else -> remoteViewsNormal.setTextViewCompoundDrawables(
                    R.id.textView7,
                    R.drawable.no_meteo,0,0,0)
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViewsNormal)
            //large
            val remoteViewsLarge = RemoteViews(packageName, R.layout.widget_layout_large)
            remoteViewsLarge.setTextViewText(R.id.textView8, valori[0].toString())
            remoteViewsLarge.setTextViewText(R.id.textView9, ut.getCalories(valori[0]))
            remoteViewsLarge.setTextViewText(R.id.textView10, temperatura+" °C" )
            when(meteo){
                "0","1" -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.sunny,0,0,0)
                "2","3" -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.cloudy,0,0,0)
                "45", "48" -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.foggy,0,0,0)
                "51", "53", "55", "56", "57", "61", "63", "65", "66", "67", "80", "81", "82", "95", "96", "99" -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.rain,0,0,0)
                "71", "73", "75", "77", "85", "86" -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.snow,0,0,0)
                else -> remoteViewsLarge.setTextViewCompoundDrawables(
                    R.id.textView10,
                    R.drawable.no_meteo,0,0,0)
            }
            remoteViewsLarge.setTextViewText(R.id.mediaPassiSettimana, passiSettimana)
            remoteViewsLarge.setTextViewText(R.id.mediaPassiMese, passiMese)
            remoteViewsLarge.setTextViewText(R.id.obiettiviRaggiunti, obiettivi)
            appWidgetManager.updateAppWidget(widgetId, remoteViewsLarge)
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val sensorSteps = event!!.values[0].toInt()
        serviceScope.launch {
            //prepara il database
            val ut = Utility()
            val key = repository.formatKey(ut.getDataOggi())
            val previousDbSteps = repository.getValueFromKey(key)[0]
            val previousSteps = ut.loadData(this@ForegroundService, "passiPr").toInt()
            if(sensorSteps >= previousSteps) {
                if(previousSteps != 0){
                    repository.updateSteps(key, previousDbSteps + (sensorSteps - previousSteps))
                }
            } else {
                repository.updateSteps(key, previousDbSteps + sensorSteps)
            }
            ut.saveData(this@ForegroundService, "passiPr", sensorSteps.toFloat())
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
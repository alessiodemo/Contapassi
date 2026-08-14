package com.example.passi.core.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.ArrayMap
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.passi.R

class StepsWidget : AppWidgetProvider() {
    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {

            // creo varie remote views, ognuna viene "referenziata" al layout di riferimento.
            val smallView = RemoteViews(context.packageName, R.layout.widget_layout_small)
            val smallView1 = RemoteViews(context.packageName, R.layout.widget_layout_small1)
            val smallView2 = RemoteViews(context.packageName, R.layout.widget_layout_small2)
            val mediumView = RemoteViews(context.packageName, R.layout.widget_layout_normal)
            val largeView = RemoteViews(context.packageName, R.layout.widget_layout_large)

            //costruisco una struttura Map al cui interno verranno inserite varie view ( con la struttura sizeF() ) con differenti dimensioni
            val viewMapping: MutableMap<SizeF, RemoteViews> = ArrayMap()
            viewMapping[SizeF(130f, 0f)] = smallView
            viewMapping[SizeF(203f, 0f)] = smallView1
            viewMapping[SizeF(267f, 0f)] = smallView2
            viewMapping[SizeF(130f, 220f)] = mediumView
            viewMapping[SizeF(130f, 337f)] = largeView


            //creo remoteView basata sulla struttura mappa
            val remoteViews = RemoteViews(viewMapping)
            //smallView.setTextViewText(R.id.appwidget_text, "PASSI")
            //mediumView.setTextViewText(R.id.appwidget_text, "PASSI")

            //aggiorno i widget esistenti
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)



            // Creazione dell'intent per l'azione onClick del pulsante passiKm
            var intent = Intent(context, StepsWidget::class.java)
            intent.action = "com.example.PASSI_KM"
            var pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            var views = RemoteViews(context.packageName, R.layout.widget_layout_small1)
            views.setOnClickPendingIntent(R.id.widgetButtonPassi, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetIds, views)

            intent = Intent(context, StepsWidget::class.java)
            intent.action = "com.example.KM_PASSI"
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views = RemoteViews(context.packageName, R.layout.widget_layout_small1)
            views.setOnClickPendingIntent(R.id.widgetButtonKm, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.example.PASSI_KM") {
            val remoteViewsSmall1 = RemoteViews(context.packageName, R.layout.widget_layout_small1)
            remoteViewsSmall1.setViewVisibility(R.id.textView, View.VISIBLE)
            remoteViewsSmall1.setViewVisibility(R.id.km, View.GONE)
            remoteViewsSmall1.setViewVisibility(R.id.widgetButtonKm, View.VISIBLE)
            remoteViewsSmall1.setViewVisibility(R.id.widgetButtonPassi, View.GONE)
        }
        if (intent.action == "com.example.KM_PASSI") {
            val remoteViewsSmall1 = RemoteViews(context.packageName, R.layout.widget_layout_small1)
            remoteViewsSmall1.setViewVisibility(R.id.textView, View.GONE)
            remoteViewsSmall1.setViewVisibility(R.id.km, View.VISIBLE)
            remoteViewsSmall1.setViewVisibility(R.id.widgetButtonKm, View.GONE)
            remoteViewsSmall1.setViewVisibility(R.id.widgetButtonPassi, View.VISIBLE)
        }
    }
}
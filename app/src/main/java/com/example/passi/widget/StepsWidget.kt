package com.example.passi.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Build
import android.util.ArrayMap
import android.util.SizeF
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

            val smallView = RemoteViews(context.getPackageName(), R.layout.widget_layout_small)
            val smallView1 = RemoteViews(context.getPackageName(), R.layout.widget_layout_small1)
            val smallView2 = RemoteViews(context.getPackageName(), R.layout.widget_layout_small2)
            val mediumView = RemoteViews(context.getPackageName(), R.layout.widget_layout_normal)
            val largeView = RemoteViews(context.getPackageName(), R.layout.widget_layout_large)

            //costruisco una struttura Map al cui interno verranno inserite varie view ( con la struttura sizeF() ) con differenti dimensioni
            val viewMapping: MutableMap<SizeF?, RemoteViews?> = ArrayMap<SizeF?, RemoteViews?>()
            viewMapping.put(SizeF(130f, 0f), smallView)
            viewMapping.put(SizeF(203f, 0f), smallView1)
            viewMapping.put(SizeF(276f, 0f), smallView2)

            viewMapping.put(SizeF(130f, 220f), mediumView)

            viewMapping.put(SizeF(130f, 337f), largeView)


            //creo remoteView basata sulla struttura mappa
            val remoteViews = RemoteViews(viewMapping)


            smallView.setTextViewText(R.id.appwidget_text, "PASSI")
            mediumView.setTextViewText(R.id.appwidget_text, "PASSI")

            //aggiorno i widget esistenti
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
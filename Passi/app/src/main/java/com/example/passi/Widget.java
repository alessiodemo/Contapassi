package com.example.passi;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.util.ArrayMap;
import android.util.SizeF;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.util.Map;

public class Widget extends AppWidgetProvider {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("RemoteViewLayout")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            // creo varie remote views, ognuna viene "referenziata" al layout di riferimento.
            RemoteViews smallView = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
            RemoteViews smallView1 = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small1);
            RemoteViews smallView2 = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small2);
            RemoteViews mediumView = new RemoteViews(context.getPackageName(), R.layout.widget_layout_normal);
            RemoteViews largeView = new RemoteViews(context.getPackageName(), R.layout.widget_layout_large);

            //costruisco una struttura Map al cui interno verranno inserite varie view ( con la struttura sizeF() ) con differenti dimensioni
            Map<SizeF, RemoteViews> viewMapping = new ArrayMap<>();
            viewMapping.put(new SizeF(130f, 0f), smallView);
            viewMapping.put(new SizeF(203f, 0f), smallView1);
            viewMapping.put(new SizeF(276f, 0f), smallView2);

            viewMapping.put(new SizeF(130f, 220f), mediumView);

            viewMapping.put(new SizeF(130f, 337f), largeView);


            //creo remoteView basata sulla struttura mappa
            RemoteViews remoteViews = new RemoteViews(viewMapping);


            smallView.setTextViewText(R.id.appwidget_text, "PASSI");
            mediumView.setTextViewText(R.id.appwidget_text,"PASSI");

            //aggiorno i widget esistenti
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

}
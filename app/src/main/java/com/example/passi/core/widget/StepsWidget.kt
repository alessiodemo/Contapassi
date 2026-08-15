package com.example.passi.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.example.passi.R
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import com.example.passi.core.utility.Utility
import com.example.passi.core.weather.weatherIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * REGOLA CENTRALE DI QUESTO FILE
 *
 * appWidgetManager.updateAppWidget() SOSTITUISCE integralmente cio' che il widget
 * mostra: non aggiorna, non fonde. L'ultima chiamata cancella tutte le precedenti.
 *
 * Di conseguenza esiste un solo punto in cui il widget viene pubblicato
 * (aggiornaTuttiIWidget) e un solo punto in cui viene costruito (buildWidgetViews),
 * con dentro gia' tutto: layout, dati e pulsanti. Prima erano tre percorsi separati
 * — onUpdate, onReceive e ForegroundService — che si sovrascrivevano a vicenda.
 */
class StepsWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // un BroadcastReceiver viene ucciso appena onUpdate ritorna: goAsync() chiede
        // ad Android di tenerlo vivo finche' non chiamiamo finish(), il tempo di
        // leggere il database
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                aggiornaTuttiIWidget(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Chiamato dal sistema quando l'utente ridimensiona il widget (da API 16).
     *
     * Su Android 12+ non servirebbe: la mappa SizeF fa scegliere il layout al launcher
     * senza coinvolgerci. Sotto, invece, e' l'UNICO modo per accorgersi del nuovo
     * ingombro e ripubblicare il layout adatto.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                aggiornaTuttiIWidget(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // lo scambio passi/km non modifica direttamente le view: salva una preferenza
        // e fa ridisegnare tutto dallo stesso costruttore. Se pubblicasse un layout
        // per conto suo cancellerebbe la mappa responsive, che e' il bug di partenza.
        val mostraKm = when (intent.action) {
            ACTION_KM_PASSI -> true
            ACTION_PASSI_KM -> false
            else -> return
        }
        Utility().saveData(context, KEY_MOSTRA_KM, if (mostraKm) 1f else 0f)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                aggiornaTuttiIWidget(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {

        const val ACTION_PASSI_KM = "com.example.passi.PASSI_KM"
        const val ACTION_KM_PASSI = "com.example.passi.KM_PASSI"
        private const val KEY_MOSTRA_KM = "widgetMostraKm"

        /** Unico punto in cui il widget viene pubblicato. */
        suspend fun aggiornaTuttiIWidget(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, StepsWidget::class.java)
            )
            if (ids.isEmpty()) return
            val dati = caricaContenuto(context)
            // un widget per volta: sotto Android 12 la scelta del layout dipende
            // dall'ingombro del singolo widget, che puo' essere diverso per ciascuno
            for (id in ids) {
                manager.updateAppWidget(
                    id, buildWidgetViews(context, dati, manager.getAppWidgetOptions(id))
                )
            }
        }

        /** Legge i dati dal database e dalle preferenze. */
        suspend fun caricaContenuto(context: Context): WidgetContent {
            val ut = Utility()
            val repository = StepRepository(AppDatabase.getInstance(context).stepDao())
            val valori = repository.getValueFromKey(repository.formatKey(ut.getDataOggi()))
            val passi = valori[0]
            val obiettivo = valori[1]
            val altezza = valori[2]
            val settimana = repository.totalWeeklySteps()
            val mese = repository.totalMonthlySteps()

            return WidgetContent(
                passi = passi,
                // il controllo su obiettivo > 0 non e' formale: Utility.getProgress fa una
                // divisione intera e con obiettivo a 0 lancerebbe ArithmeticException,
                // uccidendo l'aggiornamento del widget
                progresso = if (obiettivo > 0) minOf(passi * 100 / obiettivo, 100) else 0,
                km = ut.getDistance(passi, altezza),
                kcal = ut.getCalories(passi),
                temperatura = ut.loadData(context, "temperatura").toInt(),
                codiceMeteo = ut.loadData(context, "meteo").toInt(),
                passiSettimana = settimana.toString(),
                kmSettimana = ut.getDistance(settimana, altezza),
                passiMese = mese.toString(),
                kmMese = ut.getDistance(mese, altezza),
                obiettiviRaggiunti = repository.goalsReached().toString(),
                mostraKm = ut.loadData(context, KEY_MOSTRA_KM) == 1f
            )
        }

        /**
         * Costruisce la RemoteViews completa.
         *
         * ATTENZIONE agli id: ogni layout ha i suoi, e RemoteViews non ha modo di
         * verificarli. Un id sbagliato compila senza problemi e viene poi ignorato
         * in silenzio dal launcher, lasciando il segnaposto al suo posto.
         *   small  -> textView
         *   small1 -> textView, km, widgetButtonPassi, widgetButtonKm
         *   small2 -> textView3, textView4
         *   normal -> textView5, textView6, textView7
         *   large  -> textView8, textView9, textView10, media*, obiettivi*
         */
        fun buildWidgetViews(
            context: Context,
            dati: WidgetContent,
            opzioni: Bundle? = null
        ): RemoteViews {
            val passi = dati.passi.toString()
            val km = context.getString(R.string.formato_km, dati.km)
            val kcal = context.getString(R.string.formato_kcal, dati.kcal)
            val gradi = context.getString(R.string.formato_gradi, dati.temperatura)
            val icona = weatherIcon(dati.codiceMeteo)

            val small = RemoteViews(context.packageName, R.layout.widget_layout_small).apply {
                setTextViewText(R.id.textView, passi)
                setProgressBar(R.id.progressPassi, 100, dati.progresso, false)
            }

            val small1 = RemoteViews(context.packageName, R.layout.widget_layout_small1).apply {
                setTextViewText(R.id.textView, passi)
                setTextViewText(R.id.km, km)
                // si vede un valore per volta, con il pulsante che porta all'altro
                setViewVisibility(R.id.textView, if (dati.mostraKm) View.GONE else View.VISIBLE)
                setViewVisibility(R.id.km, if (dati.mostraKm) View.VISIBLE else View.GONE)
                setViewVisibility(
                    R.id.widgetButtonPassi, if (dati.mostraKm) View.VISIBLE else View.GONE
                )
                setViewVisibility(
                    R.id.widgetButtonKm, if (dati.mostraKm) View.GONE else View.VISIBLE
                )
                setOnClickPendingIntent(R.id.widgetButtonPassi, intentPassiKm(context))
                setOnClickPendingIntent(R.id.widgetButtonKm, intentKmPassi(context))
                setProgressBar(R.id.progressPassi, 100, dati.progresso, false)
            }

            val small2 = RemoteViews(context.packageName, R.layout.widget_layout_small2).apply {
                setTextViewText(R.id.textView3, passi)
                setTextViewText(R.id.textView4, km)
                setProgressBar(R.id.progressPassi, 100, dati.progresso, false)
            }

            val normal = RemoteViews(context.packageName, R.layout.widget_layout_normal).apply {
                setTextViewText(R.id.textView5, passi)
                setTextViewText(R.id.textView6, kcal)
                setTextViewText(R.id.textView7, gradi)
                setTextViewCompoundDrawables(R.id.textView7, icona, 0, 0, 0)
                setProgressBar(R.id.progressPassi, 100, dati.progresso, false)
            }

            val large = RemoteViews(context.packageName, R.layout.widget_layout_large).apply {
                setTextViewText(R.id.textView8, passi)
                setTextViewText(R.id.textView9, kcal)
                setTextViewText(R.id.textView10, gradi)
                setTextViewCompoundDrawables(R.id.textView10, icona, 0, 0, 0)
                setTextViewText(R.id.mediaPassiSettimana, dati.passiSettimana)
                setTextViewText(
                    R.id.mediaKmSettimana,
                    context.getString(R.string.formato_km, dati.kmSettimana)
                )
                setTextViewText(R.id.mediaPassiMese, dati.passiMese)
                setTextViewText(
                    R.id.mediaKmMese,
                    context.getString(R.string.formato_km, dati.kmMese)
                )
                setTextViewText(R.id.obiettiviRaggiunti, dati.obiettiviRaggiunti)
                setProgressBar(R.id.progressPassi, 100, dati.progresso, false)
            }

            // Android 12+: si consegnano TUTTI i layout in una mappa e sara' il launcher
            // a scegliere, istantaneamente, a ogni ridimensionamento. La mappa associa a
            // ogni layout la dimensione MINIMA a cui usarlo.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RemoteViews(
                    mapOf(
                        SizeF(130f, 52f) to small,
                        SizeF(203f, 52f) to small1,
                        SizeF(267f, 52f) to small2,
                        SizeF(130f, 120f) to normal,
                        SizeF(130f, 220f) to large
                    )
                )
            }

            // Sotto Android 12 quella mappa non esiste: si consegna UN solo layout,
            // scelto da noi in base all'ingombro attuale letto dalle opzioni del widget.
            // Le soglie sono le stesse della mappa, per avere lo stesso comportamento.
            return scegliPerDimensione(context, opzioni, small, small1, small2, normal, large)
        }

        /**
         * Ingombro attuale del widget, in dp, dal Bundle delle opzioni.
         *
         * I quattro valori MIN/MAX non sono un intervallo di tolleranza: descrivono
         * l'ingombro nelle DUE orientazioni della home. In verticale la larghezza reale
         * e' MIN_WIDTH e l'altezza reale e' MAX_HEIGHT; in orizzontale si invertono.
         * Prendere la coppia sbagliata significa scegliere il layout per l'orientazione
         * in cui l'utente non si trova.
         */
        private fun scegliPerDimensione(
            context: Context,
            opzioni: Bundle?,
            small: RemoteViews,
            small1: RemoteViews,
            small2: RemoteViews,
            normal: RemoteViews,
            large: RemoteViews
        ): RemoteViews {
            if (opzioni == null) return small

            val verticale = context.resources.configuration.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
            val larghezza = opzioni.getInt(
                if (verticale) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
                else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
            )
            val altezza = opzioni.getInt(
                if (verticale) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
                else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
            )

            return when {
                altezza >= 220 -> large
                altezza >= 120 -> normal
                larghezza >= 267 -> small2
                larghezza >= 203 -> small1
                else -> small
            }
        }

        fun intentPassiKm(context: Context) = widgetIntent(context, ACTION_PASSI_KM, 1)

        fun intentKmPassi(context: Context) = widgetIntent(context, ACTION_KM_PASSI, 2)

        /**
         * FLAG_IMMUTABLE non e' opzionale: con targetSdk >= 31 creare un PendingIntent
         * senza dichiararne la mutabilita' lancia IllegalArgumentException.
         */
        private fun widgetIntent(context: Context, action: String, requestCode: Int) =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, StepsWidget::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}

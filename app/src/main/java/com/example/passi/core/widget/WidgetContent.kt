package com.example.passi.core.widget

/**
 * Tutti i dati che il widget mostra, gia' pronti da scrivere nelle view.
 *
 * Serve a separare due responsabilita' che prima erano intrecciate: DA DOVE arrivano i
 * dati (database, SharedPreferences) e COME vengono disegnati. buildWidgetViews riceve
 * questo oggetto e non deve sapere nient'altro, quindi la stessa funzione va bene sia
 * per il servizio sia per l'AppWidgetProvider.
 */
data class WidgetContent(
    val passi: Int,
    /** avanzamento verso l'obiettivo del giorno, gia' limitato a 0..100 */
    val progresso: Int,
    val km: String,
    val kcal: String,
    val temperatura: Int,
    val codiceMeteo: Int,
    val passiSettimana: String,
    val kmSettimana: String,
    val passiMese: String,
    val kmMese: String,
    val obiettiviRaggiunti: String,
    /** true = il widget piccolo mostra i km al posto dei passi (pulsante di scambio) */
    val mostraKm: Boolean
)

package com.example.passi.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.passi.R
import com.example.passi.core.data.StepRepository

class HistogramView(context: Context, passi: MutableList<Int>) : View(context) {

    private var data: List<Int> = passi.toList()

    /** Da dp a pixel: le misure scritte a mano in px cambiano dimensione con la densita' dello schermo. */
    private fun dp(valore: Float) = valore * resources.displayMetrics.density

    /**
     * Legge un colore dal TEMA corrente (es. ?attr/colorOnSurface) invece di usare
     * una costante come Color.BLACK: cosi' il grafico resta leggibile anche in tema
     * scuro, dove il nero su fondo scuro sparirebbe.
     */
    private fun themeColor(attr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return value.data
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(12f)
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        textAlign = Paint.Align.CENTER
    }
    private val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(1.5f)
        color = ContextCompat.getColor(context, R.color.app_success)
    }
    private val goalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(10f)
        textAlign = Paint.Align.LEFT
    }

    fun setValue(passi: MutableList<Int>) {
        // assegnazione, non append: chiamando due volte setValue la lista raddoppiava.
        // Nessuna rotazione: getWeekSteps() restituisce gia' sette slot indicizzati
        // lunedi..domenica, allineati con l'array di getWeekday().
        data = passi.toList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty() || width == 0 || height == 0) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        labelPaint.color = themeColor(com.google.android.material.R.attr.colorOnSurface)
        valuePaint.color = themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
        goalLabelPaint.color = themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
        barPaint.color = themeColor(com.google.android.material.R.attr.colorPrimary)
        borderPaint.color = themeColor(com.google.android.material.R.attr.colorOutline)

        // Lo spazio sopra e sotto si ricava dalle metriche reali dei Paint, non da una
        // percentuale dell'altezza: e' questo che prima tagliava le etichette, perche'
        // il 10% dell'altezza era meno dello spazio che il testo occupa davvero.
        val labelMetrics = labelPaint.fontMetrics
        val spazioSotto = (labelMetrics.descent - labelMetrics.ascent) + dp(6f)

        val valueMetrics = valuePaint.fontMetrics
        val spazioSopra = (valueMetrics.descent - valueMetrics.ascent) + dp(4f)

        val asseY = viewHeight - spazioSotto
        val altezzaMax = asseY - spazioSopra
        if (altezzaMax <= 0f) return

        // fondo scala: sempre almeno il 150% dell'obiettivo, cosi' la linea del goal
        // resta visibile anche in una settimana con pochi passi
        val fondoScala = maxOf(StepRepository.default_goal * 1.5f, (data.maxOrNull() ?: 0) * 1.15f)

        val passoX = viewWidth / (data.size + 1)
        val larghezzaBarra = passoX * 0.55f

        // linea dell'obiettivo
        val goalY = asseY - (StepRepository.default_goal / fondoScala * altezzaMax)
        canvas.drawLine(0f, goalY, viewWidth, goalY, goalPaint)
        canvas.drawText("Goal", dp(2f), goalY - dp(3f), goalLabelPaint)

        for (i in data.indices) {
            val centroX = passoX * (i + 1)
            val sinistra = centroX - larghezzaBarra / 2f
            val destra = centroX + larghezzaBarra / 2f
            val altezzaBarra = (data[i] / fondoScala * altezzaMax).coerceIn(0f, altezzaMax)
            val cima = asseY - altezzaBarra

            if (altezzaBarra > 0f) {
                canvas.drawRect(sinistra, cima, destra, asseY, barPaint)
                canvas.drawRect(sinistra, cima, destra, asseY, borderPaint)
            }

            // valore sopra la barra, mai sopra il bordo della view
            val valoreY = (cima - dp(3f)).coerceAtLeast(-valueMetrics.ascent)
            canvas.drawText(data[i].toString(), centroX, valoreY, valuePaint)

            // etichetta orizzontale: "Mon" a 12dp occupa ~30dp, mentre ogni barra ne ha
            // circa 45. La rotazione di -90 gradi non serviva e faceva uscire il testo
            // dal bordo inferiore.
            canvas.drawText(getWeekday(i), centroX, asseY - labelMetrics.ascent + dp(4f), labelPaint)
        }

        canvas.drawLine(0f, asseY, viewWidth, asseY, borderPaint)
    }

    private fun getWeekday(index: Int): String {
        val weekdays = arrayOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
        return weekdays.getOrElse(index) { "" }
    }
}

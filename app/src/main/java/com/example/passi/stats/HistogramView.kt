package com.example.passi.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.passi.core.data.StepRepository
import java.time.LocalDate

class HistogramView(context: Context, passi:MutableList<Int>) : View(context) {

    private var data: MutableList<Int> = mutableListOf()

    init {
        setValue(passi)
    }

    fun setValue(passi: MutableList<Int>) {

        for (element in passi) {
            data.add(element)
        }

        var count = countNonZeroValues(data)
        data = shiftListElements(data, -getCurrentDayOfWeek()+count )


    }

    fun countNonZeroValues(list: List<Int>): Int {
        var count = 0
        for (value in list) {
            if (value != 0) {
                count++
            }
        }
        return count
    }


    fun shiftListElements(list: MutableList<Int>, positions: Int): MutableList<Int> {
        val size = list.size
        val shiftAmount = positions % size
        val adjustedShift = if (shiftAmount < 0) shiftAmount + size else shiftAmount
        if (adjustedShift == 0) {
            return list // No need to shift if positions is a multiple of size
        }
        val shiftedList = mutableListOf<Int>()
        for (i in 0 until size) {
            val newIndex = (i + adjustedShift) % size
            shiftedList.add(list[newIndex])
        }
        return shiftedList
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width
        val viewHeight = -1000f
        val numBars = data.size
        val barSpacing = viewWidth / (numBars + 1)
        val barWidth = barSpacing / 2

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#01796f")

        val barMaxHeight = viewHeight * 0.9f

        val max = 20000

        val maxHeight = max * 1.5f

        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3f
        borderPaint.color = Color.BLACK

        val axisHeight = viewHeight - barMaxHeight

        val textPaint = Paint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 50f
        textPaint.textAlign = Paint.Align.CENTER

        val valuePaint = Paint()
        valuePaint.color = Color.DKGRAY
        valuePaint.textSize = 50f
        valuePaint.textAlign = Paint.Align.CENTER

        val backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.FILL
        val backgroundColor = Color.WHITE
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), backgroundPaint)

        val textOffset = 60f
        val valueOffset = 20f

        val lineY = viewHeight - (StepRepository.default_goal / maxHeight * barMaxHeight) - axisHeight
        val linePaint = Paint()
        linePaint.color = Color.parseColor("#00bb2d")
        linePaint.strokeWidth = 5f
        canvas.drawLine(0f, lineY, viewWidth.toFloat(), lineY, linePaint)

        val label = "Goal"
        val labelPaint = Paint()
        labelPaint.color = Color.DKGRAY
        labelPaint.textSize = 40f
        labelPaint.textAlign = Paint.Align.CENTER
        val labelX = viewWidth / 2f
        val labelY = lineY - 20f
        canvas.drawText(label, labelX, labelY, labelPaint)

        for (i in 0 until numBars) {
            val barHeight = data[i] / maxHeight * barMaxHeight

            val top = viewHeight - barHeight - axisHeight
            val bottom = viewHeight - axisHeight
            val left = barSpacing * (i + 1) - barWidth / 2f
            val right = left + barWidth

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawRect(left, top, right, bottom, borderPaint)

            val value = data[i]
            val centerX = (left + right) / 2f
            val valueY = top - valueOffset
            canvas.drawText(value.toString(), centerX, valueY, valuePaint)

            val weekday = getWeekday(i)
            val centerY = bottom + textOffset

            canvas.save()
            canvas.rotate(-90f, centerX, centerY)
            canvas.drawText(weekday, centerX, centerY, textPaint)
            canvas.restore()
        }

        val axisY = viewHeight - axisHeight
        canvas.drawLine(0f, axisY, viewWidth.toFloat(), axisY, borderPaint)
    }

    private fun getWeekday(index: Int): String {
        val weekdays = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return weekdays[index]
    }

    fun getCurrentDayOfWeek(): Int {
        val currentDate = LocalDate.now()
        val currentDayOfWeek = currentDate.dayOfWeek

        return currentDayOfWeek.value
        //return 5 //friday
    }
}
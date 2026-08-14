package com.example.passi.core.data

import com.example.passi.core.data.DAO.StepDao
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Calendar

class StepRepository(private val dao: StepDao) {

    companion object {
        @JvmField var default_goal = 10000
        @JvmField var default_height = 170
    }
    fun formatKey(d: Date): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(d)

    fun formatKey(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    private fun getDayWeekFromDataString(data: String): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(data)
        val calendar = Calendar.getInstance()
        if(date != null) calendar.time = date
        val daysOfWeek = arrayOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        return daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }

    suspend fun contains(key: String): Boolean = dao.getById(key) != null

    suspend fun inserisciTuplaSteps(passi: Int, obiettivo: Int, altezza: Int) {
        val currentDate = formatKey(Date())
        if (!contains(currentDate)) {
            var ob = obiettivo
            var al = altezza

            if(obiettivo == 0) {
                val lastKey = dao.getLastId()
                al = if(lastKey == null) default_height else dao.getById(lastKey)!!.height
            }
            if(altezza == 0) {
                val lastKey = dao.getLastId()
                al = if (lastKey == null) default_height else dao.getById(lastKey)!!.height
            }
            dao.insert(StepEntity(currentDate, getDayWeekFromDataString(currentDate), passi, ob, al))

        } else {
            setSteps(currentDate, passi, obiettivo, altezza)
        }
    }

    suspend fun setSteps(key: String, steps: Int, goal: Int, height: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0)
        dao.updateSteps(key, steps)
        dao.updateGoal(key, goal)
        dao.updateHeight(key, height)
    }

    suspend fun updateSteps(key: String, steps: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0)
        dao.updateSteps(key, steps)
    }

    suspend fun updateGoal(key: String, goal: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0)
        dao.updateGoal(key, goal)
    }

    suspend fun updateHeight(key: String, height: Int) = dao.updateHeight(key, height)

    suspend fun getValueFromKey(data: String): Array<Int> {
        if (!contains(data)) inserisciTuplaSteps(0, 0, 0)
        val e = dao.getById(data) ?: return arrayOf(-1)
        return arrayOf(e.steps, e.goal, e.height)
    }

    suspend fun getWeeklySteps(): MutableList<Int> {
        var day = LocalDate.now().dayOfWeek.value
        val steps = mutableListOf<Int>()
        while (day > 0) {
            val key = formatKey(LocalDate.now().minusDays((day - 1).toLong()))
            if (contains(key)) steps.add(getValueFromKey(key)[0])
            day--
        }
        return steps
    }

    suspend fun totalWeeklySteps(): Int = getWeeklySteps().sum()

    suspend fun totalMonthlySteps(): Int {
        var day = LocalDate.now().dayOfMonth
        var steps = 0
        while (day > 0) {
            val key = formatKey(LocalDate.now().minusDays((day - 1).toLong()))
            if (contains(key)) steps += getValueFromKey(key)[0]
            day--
        }
        return steps
    }

    suspend fun goalsReached(): Int = dao.countGoalsReached()

    fun getCurrentDayOfWeek(): Int = LocalDate.now().dayOfWeek.value

    suspend fun getWeekSteps(): MutableList<Int> {
        val day = "saturday"
        val dati = MutableList(7) {0}
        val rows = dao.getAllOrderedById()
        val names = mutableListOf<String>()

        when (day) {
            "monday" -> {
                if(rows.isNotEmpty())
                    dati[0] = rows.getOrNull(formatKey(Date()).toInt() - 1)?.steps ?: 0
                return dati
            }
            "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" -> {
                val size = rows.size
                if(size == 1) {
                    dati[0] = rows[0].steps
                    return dati
                } else if (size > 1) {
                    var u = 0
                    val idx = formatKey(Date()).toInt().coerceAtLeast(size)-1
                    if(getCurrentDayOfWeek()>=size) {
                        val start = (idx - size +1).coerceAtLeast(0)
                        while (u < size && start + u < rows.size) {
                            names.add(rows[start + u].steps.toString())
                            u++
                        }
                        for (i in names.indices) dati[i] = names[i].toInt()
                    } else {
                        val start = (idx - getCurrentDayOfWeek() + 1).coerceAtLeast(0)
                        while (u < getCurrentDayOfWeek() && start + u < rows.size) {
                            names.add(rows[start + u].steps.toString())
                            u++
                        }
                        for (i in names.indices) dati[i] = names[i].toInt()
                    }
                }
            }
        }
        return dati
    }

    suspend fun getGoalRows(): MutableList<GoalRow> {
        val a = mutableListOf<GoalRow>()
        for (e in dao.getAllOrderedById()) {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(e.id) ?: continue
            a.add(GoalRow(date, e.steps, e.goal, e.height))
        }
        return a
    }

    fun modifyGoal(newValue: Int) { default_goal = newValue }
    fun modifyHeight(newValue: Int) { default_height = newValue }
}
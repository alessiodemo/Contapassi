package com.example.passi.core.data

import com.example.passi.core.data.DAO.StepDao
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class StepRepository(private val dao: StepDao) {

    companion object {
        @JvmField var default_goal = 10000
        @JvmField var default_height = 170
        @JvmField var default_weight = 70
    }
    fun formatKey(d: Date): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(d)

    fun formatKey(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    suspend fun contains(key: String): Boolean = dao.getById(key) != null

    suspend fun inserisciTuplaSteps(passi: Int, obiettivo: Int, altezza: Int, peso: Int) {
        val currentDate = formatKey(Date())
        if (!contains(currentDate)) {
            var ob = obiettivo
            var al = altezza
            var pe = peso

            if(obiettivo == 0) {
                val lastKey = dao.getLastId()
                ob = if(lastKey == null) default_goal else dao.getById(lastKey)!!.goal
            }
            if(altezza == 0) {
                val lastKey = dao.getLastId()
                al = if (lastKey == null) default_height else dao.getById(lastKey)!!.height
            }
            if(peso == 0) {
                val lastKey = dao.getLastId()
                pe = if (lastKey == null) default_weight else dao.getById(lastKey)!!.weight
            }
            dao.insert(StepEntity(currentDate, passi, ob, al, pe))

        } else {
            setSteps(currentDate, passi, obiettivo, altezza, peso)
        }
    }

    suspend fun setSteps(key: String, steps: Int, goal: Int, height: Int, weight: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0, 0)
        dao.updateSteps(key, steps)
        dao.updateGoal(key, goal)
        dao.updateHeight(key, height)
        dao.updateWeight(key ,weight)
    }

    suspend fun updateSteps(key: String, steps: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0,0)
        dao.updateSteps(key, steps)
    }

    suspend fun updateGoal(key: String, goal: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0,0)
        dao.updateGoal(key, goal)
    }

    suspend fun updateHeight(key: String, height: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0,0)
        dao.updateHeight(key, height)
    }

    suspend fun updateWeight(key: String, weight: Int) {
        if(!contains(key)) inserisciTuplaSteps(0, 0, 0,0)
        dao.updateWeight(key, weight)
    }

    suspend fun getValueFromKey(data: String): Array<Int> {
        if (!contains(data)) inserisciTuplaSteps(0, 0, 0,0)
        val e = dao.getById(data) ?: return arrayOf(-1)
        return arrayOf(e.steps, e.goal, e.height, e.weight)
    }

    suspend fun totalWeeklySteps(): Int = getWeekSteps().sum()

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
        val dati = MutableList(7) { 0 }
        val oggi = LocalDate.now()
        // dayOfWeek.value: 1 = lunedi ... 7 = domenica
        val lunedi = oggi.minusDays((oggi.dayOfWeek.value - 1).toLong())
        for (i in 0 until oggi.dayOfWeek.value) {
            val key = formatKey(lunedi.plusDays(i.toLong()))
            dati[i] = dao.getById(key)?.steps ?: 0
        }
        return dati
    }

    suspend fun getGoalRows(): MutableList<GoalRow> {
        val a = mutableListOf<GoalRow>()
        for (e in dao.getAllOrderedById()) {
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(e.id) ?: continue
            a.add(GoalRow(date, e.steps, e.goal, e.height, e.weight))
        }
        return a
    }
}
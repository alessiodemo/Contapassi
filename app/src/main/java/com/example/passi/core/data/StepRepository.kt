package com.example.passi.core.data

import com.example.passi.core.data.DAO.StepDao
import kotlinx.coroutines.flow.Flow
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

    suspend fun accumula(key: String, dPassi: Int, dKm: Double, dKcal: Double) {
        if (!contains(key)) inserisciTuplaSteps(0, 0, 0, 0)
        dao.accumula(key, dPassi, dKm, dKcal)
    }

    suspend fun goalsReached(): Int = dao.countGoalsReached()

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
            a.add(GoalRow(date, e.steps, e.goal, e.distanzaKm, e.kcal))
        }
        return a
    }

    suspend fun getRow(key: String): StepEntity {
        if (!contains(key)) inserisciTuplaSteps(0, 0, 0,0)
        return dao.getById(key) ?: StepEntity(key, 0, default_goal, default_height, default_weight)
    }

    private fun chiaveOggi() = formatKey(LocalDate.now())
    private fun chiaveLunedi() = LocalDate.now().let {
        formatKey(it.minusDays((it.dayOfWeek.value - 1).toLong())) }
    private fun chiavePrimoMese() = formatKey(LocalDate.now().withDayOfMonth(1))
    suspend fun passiSettimana() = dao.sommaPassi(chiaveLunedi(), chiaveOggi())
    suspend fun kmSettimana() = dao.sommaDistanza(chiaveLunedi(), chiaveOggi())
    suspend fun passiMese() = dao.sommaPassi(chiavePrimoMese(), chiaveOggi())
    suspend fun kmMese() = dao.sommaDistanza(chiavePrimoMese(), chiaveOggi())

    /**
     * Non e' suspend: non esegue niente, restituisce il Flow che Room riemettera' a
     * ogni scrittura sulla tabella. Il lavoro parte quando qualcuno lo raccoglie.
     */
    fun osservaRiga(key: String): Flow<StepEntity?> = dao.osservaRiga(key)
}

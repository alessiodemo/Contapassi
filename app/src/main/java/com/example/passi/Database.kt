package com.example.passi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.passi.entities.GoalRow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class Database(context: Context) {
    companion object {
        const val KEY_RIGAID = "id"
        const val KEY_DAY = "day"
        const val KEY_STEPS = "steps"
        const val KEY_GOAL = "goal"
        const val KEY_HEIGHT = "height"
        const val DATABASE_NOME = "TestDB"
        const val DATABASE_TABELLA = "StepCounterDatabase"
        const val DATABASE_VERSIONE = 1
        const val DATABASE_CREAZIONE = "CREATE TABLE StepCounterDatabase (id text primary key, " +
                "day text not null, steps int not null, goal int not null, height int not null);"

        @JvmField
        public var default_goal = 10000
        @JvmField
        public var default_height = 170
    }

    private var db: SQLiteDatabase
    private var dbHelper: DatabaseHelper

    init {
        dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
    }


    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NOME, null, DATABASE_VERSIONE) {
        override fun onCreate(db: SQLiteDatabase) {
            try {
                db.execSQL(DATABASE_CREAZIONE)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(DatabaseHelper::class.java.name, "Aggiornamento database dalla versione $oldVersion alla $newVersion. I dati esistenti verranno eliminati.")
            db.execSQL("DROP TABLE IF EXISTS StepCounterDatabase")
            onCreate(db)
        }
    }

    fun open(): Database {
        db = dbHelper.writableDatabase
        return this;
    }

    //method to close the database
    fun close(): Boolean {
        dbHelper!!.close()
        return true
    }


    fun inserisciTuplaSteps(passi: Int, obiettivo: Int, altezza: Int) {
        val currentDate = date2()
        if (!contains(currentDate)) { //se non c'è già una riga per la data attuale viene creata
            var ob = obiettivo
            var al = altezza
            if(obiettivo == 0){
                val lastDate = getLastDate()
                if(lastDate == "-1"){
                    ob = default_goal
                } else {
                    ob = getValuesFromKey(lastDate)[1]
                }
            }
            if(altezza == 0){
                val lastDate = getLastDate()
                if(lastDate == "-1"){
                    al = default_height
                } else {
                    al = getValuesFromKey(lastDate)[2]
                }
            }
            inserisciTuplaStepsForced(currentDate, passi, ob, al)
        } else { //se c'è già una riga per la data attuale viene sovrascritta
            setSteps(date2(), passi, obiettivo, altezza)
        }
    }

    //method to insert a new row in a forced way
    fun inserisciTuplaStepsForced(data: String?, passi: Int, goal: Int, altezza: Int) {
        val initialValues = ContentValues()
        val giorno = data?.let { getDayWeekFromDateString(it) }
        initialValues.put(KEY_RIGAID, data)
        initialValues.put(KEY_DAY, giorno)
        initialValues.put(KEY_STEPS, passi)
        initialValues.put(KEY_GOAL, goal)
        initialValues.put(KEY_HEIGHT, altezza)
        db.insert(DATABASE_TABELLA, null, initialValues)
    }

    //method to update steps and other values of a row
    fun setSteps(key: String, steps: Int, goal: Int, height: Int) {
        if(!contains(key)){
            inserisciTuplaSteps(0, 0, 0)
        }
        val values = ContentValues()
        values.put(KEY_STEPS, steps)
        values.put(KEY_GOAL, goal)
        values.put(KEY_HEIGHT, height)
        db.update(DATABASE_TABELLA, values, "$KEY_RIGAID = ?", Array(1){key})
    }

    fun updateSteps(key: String, steps: Int){
        if(!contains(key)){
            inserisciTuplaSteps(0, 0, 0)
        }
        val values = ContentValues()
        values.put(KEY_STEPS, steps)
        db.update(DATABASE_TABELLA, values, "$KEY_RIGAID = ?", Array(1){key})
    }

    fun updateGoal(key: String, goal: Int){
        if(!contains(key)){
            inserisciTuplaSteps(0, 0, 0)
        }
        val values = ContentValues()
        values.put(KEY_GOAL, goal)
        db.update(DATABASE_TABELLA, values, "$KEY_RIGAID = ?", Array(1){key})
    }

    fun updateHeight(key: String, height: Int){
        val values = ContentValues()
        values.put(KEY_HEIGHT, height)
        db.update(DATABASE_TABELLA, values, "$KEY_RIGAID = ?", Array(1){key})
    }

    fun obtainAllSteps(): Cursor {
        return db.query(DATABASE_TABELLA, arrayOf(KEY_RIGAID), null, null, null, null, null)
    }

    fun size(): Int {
        var count = 0
        val countQuery = "SELECT * FROM StepCounterDatabase"
        val cursor = db.rawQuery(countQuery, null)

        cursor?.use {
            if (!it.isClosed) {
                count = it.count
            }
        }
        return count
    }


    fun contains(key: String): Boolean {
        val c = obtainAllSteps()
        var b = false

        if (c.moveToFirst()) {
            do {
                b = c.getString(0) == key
                if (b) {
                    return true
                }
            } while (c.moveToNext())
        }
        return false
    }

    fun clear() {
        db.delete("StepCounterDatabase", null, null)
    }

    fun getStepsfromKey(id: String): String {
        val a = ArrayList<String>()

        if (contains(id)) {
            val query = "SELECT * FROM StepCounterDatabase WHERE id = ?"
            val s = db.rawQuery(query, arrayOf(id))

            if (s.moveToFirst()) {
                val stepsValue = s.getString(s.getColumnIndexOrThrow("steps"))
                a.add(stepsValue)
                return a[0]
            }
        }

        return "-1"
    }

    fun getGoalFromKey(id: String): String {
        val a = ArrayList<String>()

        if (contains(id)) {
            val s = db.rawQuery("SELECT * FROM StepCounterDatabase WHERE id = $id", null) ?: return "-1"

            try {
                if (s.moveToFirst()) {
                    a.add(s.getString(5))
                }
            } finally {
                s.close()
            }

            return if (a.isNotEmpty()) a[0] else "-1"
        }

        return "-1"
    }


    fun date(): Int {
        return Integer.parseInt(date2())
    }

    fun date2(): String {
        val c = Calendar.getInstance().time

        val df = SimpleDateFormat("yyyy", Locale.getDefault())
        val year = df.format(c)

        val df1 = SimpleDateFormat("MM", Locale.getDefault())
        val month = df1.format(c)

        val df2 = SimpleDateFormat("dd", Locale.getDefault())
        val day = df2.format(c)

        return "$year$month$day"
    }

    fun formatKey(d: Date): String {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return format.format(d)
    }

    fun formatKey(d: LocalDate): String {
        val format = DateTimeFormatter.ofPattern("yyyyMMdd")
        return d.format(format)
    }

    fun getWeeklySteps(): MutableList<Int> {
        var day = LocalDate.now().dayOfWeek.value
        val steps = mutableListOf<Int>()

        while(day>0){
            val data = LocalDate.now().minusDays((day-1).toLong())
            val key = formatKey(data)
            if(contains(key)){
                steps.add(getStepsfromKey(key).toInt())
            }
            day--
        }
        return steps
    }

    fun getMondayOfCurrentWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var daysUntilMonday = Calendar.MONDAY - dayOfWeek

        // Check if the current day is Monday
        if (daysUntilMonday == 0) {
            // Format the date as "dd/MM"
            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            return dateFormat.format(calendar.time)
        }

        // Adjust the calendar to the Monday of the current week
        if (daysUntilMonday > 0) {
            daysUntilMonday -= 7
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilMonday)

        // Format the date as "dd/MM"
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getSundayOfCurrentWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysUntilSunday = Calendar.SUNDAY - dayOfWeek + 7

        // Adjust the calendar to the Sunday of the current week
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilSunday)

        // Format the date as "dd/MM"
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun modifyGoal(newValue: Int) {
        default_goal = newValue
    }

    fun modifyHeight(newValue: Int) {
        default_height = newValue
    }

    fun totalWeeklySteps(): Int {
        val totalSteps = getWeeklySteps()
        var counter = 0
        for (element in totalSteps) {
            counter += element
        }
        return counter
    }

    fun getCurrentDayOfWeek(): Int {
        val currentDate = LocalDate.now()
        val currentDayOfWeek = currentDate.dayOfWeek

        return currentDayOfWeek.value
    }

    fun getWeekSteps(): MutableList<Int> {
        //val day = LocalDate.now().dayOfWeek.name.toLowerCase(Locale.ROOT)
        val day = "saturday"
        val steps = MutableList<Int>(7){0}

        val names = mutableListOf<String>()
        val dati = MutableList<Int>(7){0}

        when (day) {
            "monday" -> {
                val s = db.rawQuery("SELECT id, day, steps FROM StepCounterDatabase", null)
                if (s.moveToFirst()) {
                    s.moveToPosition(date())
                    s.move(-1)
                    steps[0] = (s.getInt(2))
                    return steps
                }
            }

            "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" -> {
                if (size() == 1) {
                    val s = db.rawQuery("SELECT id, day, steps FROM StepCounterDatabase", null)
                    if (s.moveToFirst()) {
                        s.moveToPosition(date())
                        s.move(-1)
                        steps[0] = (s.getInt(2))
                        return steps
                    }
                } else if (size() > 1) {

                    val s: Cursor = db.rawQuery(
                        "select id, day, steps " + "from " + "StepCounterDatabase",
                        null
                    )

                    var u = 0


                    if (s.moveToFirst()) {
                        s.moveToPosition(date())

                        if(getCurrentDayOfWeek()>=size())
                        {
                            s.move(-(size()))

                            while (u < size()) {
                                names.add(s.getString(2))
                                s.moveToNext()
                                u++

                            }
                            for (i in 0 until size() ) {
                                dati[i] = ((names[i]).toInt())
                            }
                        }
                        else
                        {
                            s.move(-(getCurrentDayOfWeek()))

                            while (u < getCurrentDayOfWeek()) {
                                names.add(s.getString(2))
                                s.moveToNext()
                                u++

                            }
                            for (i in 0 until getCurrentDayOfWeek() ) {
                                dati[i] = ((names[i]).toInt())
                            }
                        }
                    }
                }
            }
        }

        return dati
    }

    fun getDayWeekFromDateString(data: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.parse(data)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysOfWeek = arrayOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        return daysOfWeek[dayOfWeek - 1]
    }

    //method to get values (steps, goal, height) from a row given the date
    fun getValuesFromKey(data: String): Array<Int>{
        if(!contains(data)) {
            inserisciTuplaSteps(0,0,0)
        }
        val s = db.rawQuery("SELECT $KEY_STEPS, $KEY_GOAL, $KEY_HEIGHT FROM StepCounterDatabase WHERE id = $data", null)
            ?: return Array(1){-1}
        if (s.moveToFirst()) {
            val a = arrayOf(s.getInt(0), s.getInt(1), s.getInt(2))
            s.close()
            return a
        } else {
            s.close()
            return Array(1){-1}
        }
    }

    //method to get the last date in database
    fun getLastDate(): String{
        val s = db.rawQuery("SELECT $KEY_RIGAID FROM StepCounterDatabase ORDER BY $KEY_RIGAID DESC LIMIT 1", null)
            ?: return "-1"
        if (s.moveToFirst()) {
            val a = s.getString(0)
            s.close()
            return a
        } else {
            s.close()
            return "-1"
        }
    }

    //method to get the date from key
    fun keyToDate(k: String): Date? {
        val format = SimpleDateFormat("yyyyMMdd")
        return format.parse(k)
    }

    //method to get a list of GoalRow
    fun getGoalRows(): MutableList<GoalRow>{
        val a = mutableListOf<GoalRow>()
        val c = obtainAllSteps()
        if (c.moveToFirst()) {
            do {
                val b = getValuesFromKey(c.getString(0))
                val newRow = keyToDate(c.getString(0))?.let { GoalRow(it,b[0],b[1],b[2]) }
                if (newRow != null) {
                    a.add(newRow)
                }
            } while (c.moveToNext())
        }

        return a
    }

    //method to get total steps of current month
    fun totalMonthlySteps(): Int {
        var day = LocalDate.now().dayOfMonth
        var steps = 0

        while(day>0){
            val data = LocalDate.now().minusDays((day-1).toLong())
            val key = formatKey(data)
            if(contains(key)){
                steps += getStepsfromKey(key).toInt()
            }
            day--
        }
        return steps
    }

    //method to get number of goals reachedà
    fun goalsReached(): Int{
        val s = db.rawQuery("SELECT COUNT($KEY_RIGAID) FROM StepCounterDatabase WHERE $KEY_STEPS>$KEY_GOAL", null)
            ?: return -1
        if (s.moveToFirst()) {
            val a = s.getInt(0)
            s.close()
            return a
        } else {
            s.close()
            return -1
        }
    }
}


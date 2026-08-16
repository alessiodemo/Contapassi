package com.example.passi.core.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.passi.core.data.DAO.StepDao

// version 2: aggiunta la colonna weight e rimossa day (scritta e mai riletta).
// Con fallbackToDestructiveMigration il DB viene ricreato da zero al primo avvio:
// nessuna Migration da scrivere, ma lo storico dei passi precedente viene perso.
@Database(entities = [StepEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passi.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it}
            }
        }
    }
}
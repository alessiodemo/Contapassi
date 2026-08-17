package com.example.passi.core.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.passi.core.data.DAO.StepDao

// version 3: aggiunte distanzaKm e kcal, accumulate dal servizio invece che
// ricalcolate a ogni lettura (la cadenza esiste solo nell'istante del rilevamento).
// Niente fallbackToDestructiveMigration: da qui in poi ogni salto di versione
// vuole la sua Migration, e se manca l'app deve fallire, non cancellare i dati.
@Database(entities = [StepEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE steps ADD COLUMN distanzaKm REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE steps ADD COLUMN kcal REAL NOT NULL DEFAULT 0")
                db.execSQL("UPDATE steps SET distanzaKm = steps * height * 0.415 / 100000")
                db.execSQL("UPDATE steps SET kcal = distanzaKm * weight * 0.5")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passi.db"
                ).addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it}
            }
        }
    }
}
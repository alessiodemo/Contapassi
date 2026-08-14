package com.example.passi.core.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.passi.core.data.DAO.StepDao

@Database(entities = [StepEntity::class], version = 1)
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
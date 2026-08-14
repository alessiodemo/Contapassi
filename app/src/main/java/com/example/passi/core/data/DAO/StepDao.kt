package com.example.passi.core.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passi.core.data.StepEntity

@Dao
interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: StepEntity)

    @Query("UPDATE steps SET steps = :steps WHERE id = :id")
    suspend fun updateSteps(id: String, steps: Int)

    @Query("UPDATE steps SET goal = :goal WHERE id= :id")
    suspend fun updateGoal(id: String, goal: Int)

    @Query ("UPDATE steps SET height = :height WHERE id = :id")
    suspend fun updateHeight(id: String, height: Int)

    @Query("SELECT * FROM steps WHERE id = :id")
    suspend fun getById(id: String): StepEntity?

    @Query("SELECT * FROM steps ORDER BY id ASC")
    suspend fun getAllOrderedById(): List<StepEntity>

    @Query("SELECT COUNT(*) FROM steps")
    suspend fun count(): Int

    @Query("SELECT id FROM steps ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): String?

    @Query("SELECT COUNT(id) FROM steps WHERE steps > goal")
    suspend fun countGoalsReached(): Int
}
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

    @Query ("UPDATE steps SET weight = :weight WHERE id = :id")
    suspend fun updateWeight(id: String, weight: Int)

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

    @Query("UPDATE steps SET steps = steps + :dPassi, distanzaKm = distanzaKm + :dKm, kcal = kcal + :dKcal WHERE id = :id")
    suspend fun accumula(id: String, dPassi: Int, dKm: Double, dKcal: Double)

    @Query("SELECT COALESCE(SUM(steps),0) FROM steps WHERE id BETWEEN :da AND :a")
    suspend fun sommaPassi(da: String, a: String): Int

    @Query("SELECT COALESCE(SUM(distanzaKm),0) FROM steps WHERE id BETWEEN :da  AND :a")
    suspend fun sommaDistanza(da: String, a: String): Double
}
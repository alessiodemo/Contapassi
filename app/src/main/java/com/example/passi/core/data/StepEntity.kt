package com.example.passi.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val id: String,
    val steps: Int,
    val goal: Int,
    val height: Int,
    val weight: Int
)


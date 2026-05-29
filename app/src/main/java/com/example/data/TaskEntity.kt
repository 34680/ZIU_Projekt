package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
@TypeConverters(SubtaskConverter::class)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDateLong: Long = System.currentTimeMillis(),
    val category: String, // e.g. "Praca", "Osobiste", "Nauka", "Zakupy"
    val priority: String, // "NISKI", "ŚREDNI", "WYSOKI"
    val isCompleted: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val synced: Boolean = false // Track local vs cloud-backed-up state
)

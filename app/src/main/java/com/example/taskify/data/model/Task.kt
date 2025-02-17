package com.example.taskify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dueDate: Date,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdDate: Date = Date()
)
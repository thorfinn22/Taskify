package com.example.taskify.data.repository

import com.example.taskify.data.dao.TaskDao
import com.example.taskify.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    @Suppress("unused")
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()

    fun getTaskById(taskId: Long): Flow<Task> = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    @Suppress("unused")
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)

    @Suppress("unused")
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)
}
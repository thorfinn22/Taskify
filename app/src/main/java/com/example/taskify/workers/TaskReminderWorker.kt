package com.example.taskify.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskify.data.repository.TaskRepository
import com.example.taskify.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("TaskReminder", "Worker started execution")

        val taskId = inputData.getLong("taskId", -1)
        val taskTitle = inputData.getString("taskTitle")
        val taskDescription = inputData.getString("taskDescription")

        Log.d("TaskReminder", "Received task data - ID: $taskId, Title: $taskTitle")

        if (taskId == -1L || taskTitle == null || taskDescription == null) {
            Log.e("TaskReminder", "Missing required task data")
            return Result.failure()
        }

        return try {
            taskRepository.getTaskById(taskId).firstOrNull()?.let { task ->
                Log.d("TaskReminder", "Found task in database: ${task.title}")
                if (!task.isCompleted) {
                    Log.d("TaskReminder", "Showing notification for task: ${task.title}")
                    NotificationHelper(context).showTaskReminder(
                        taskId = taskId,
                        title = taskTitle,
                        description = taskDescription
                    )
                    Result.success()
                } else {
                    Log.d("TaskReminder", "Task is already completed, skipping notification")
                    Result.success()
                }
            } ?: run {
                Log.e("TaskReminder", "Task not found in database")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("TaskReminder", "Error processing task", e)
            Result.failure()
        }
    }
}
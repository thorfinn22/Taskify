package com.example.taskify.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.taskify.data.model.Task
import com.example.taskify.data.model.Priority
import com.example.taskify.data.repository.TaskRepository
import com.example.taskify.ui.events.AddEditTaskEvent
import com.example.taskify.workers.TaskReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private var taskId: Long = -1L

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _events = Channel<AddEditTaskEvent>()
    val events = _events.receiveAsFlow()

    private var title = ""
    private var description = ""
    private var dueDate = Date()
    private var priority = Priority.MEDIUM

    fun init(id: Long) {
        Log.d("TaskReminder", "Initializing ViewModel with taskId: $id")
        taskId = id
        if (taskId != -1L) {
            loadTask()
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId)
                .collect { task ->
                    Log.d("TaskReminder", "Loaded task: ${task.title}")
                    _task.value = task
                    title = task.title
                    description = task.description
                    dueDate = task.dueDate
                    priority = task.priority
                }
        }
    }

    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateDueDate(newDueDate: Date) {
        Log.d("TaskReminder", "Updating due date to: $newDueDate")
        dueDate = newDueDate
    }

    fun updatePriority(newPriority: Priority) {
        priority = newPriority
    }

    private fun scheduleReminder(taskId: Long, dueDate: Date) {
        val currentTime = System.currentTimeMillis()
        val dueTime = dueDate.time

        if (dueTime > currentTime) {
            val delay = dueTime - currentTime
            Log.d("TaskReminder", "Scheduling reminder for task $taskId in $delay ms")

            val data = workDataOf(
                "taskId" to taskId,
                "taskTitle" to title,
                "taskDescription" to description
            )

            val reminderWork = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                "task_reminder_$taskId",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )

            Log.d("TaskReminder", "Work request enqueued with ID: ${reminderWork.id}")

            // Observe work status
            workManager.getWorkInfoByIdLiveData(reminderWork.id)
                .observeForever { workInfo ->
                    Log.d("TaskReminder", "Work status for task $taskId: ${workInfo?.state}")
                }
        } else {
            Log.d("TaskReminder", "Not scheduling reminder - due time is in the past")
        }
    }

    fun saveTask() {
        Log.d("TaskReminder", "Attempting to save task with title: $title")

        if (title.isBlank()) {
            viewModelScope.launch {
                _events.send(AddEditTaskEvent.ShowInvalidInputMessage("Title cannot be empty"))
            }
            return
        }

        if (dueDate.before(Date())) {
            viewModelScope.launch {
                _events.send(AddEditTaskEvent.ShowInvalidInputMessage("Due date cannot be in the past"))
            }
            return
        }

        viewModelScope.launch {
            val task = Task(
                id = taskId.takeIf { it != -1L } ?: 0,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                isCompleted = false
            )

            val newTaskId = if (taskId == -1L) {
                Log.d("TaskReminder", "Creating new task")
                taskRepository.insertTask(task)
            } else {
                Log.d("TaskReminder", "Updating existing task")
                taskRepository.updateTask(task)
                taskId
            }

            Log.d("TaskReminder", "Task saved with ID: $newTaskId")
            scheduleReminder(newTaskId, dueDate)

            _events.send(AddEditTaskEvent.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TaskReminder", "ViewModel cleared, canceling work for taskId: $taskId")
        workManager.cancelUniqueWork("task_reminder_$taskId")
    }
}
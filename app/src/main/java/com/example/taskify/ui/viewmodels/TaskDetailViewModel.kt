package com.example.taskify.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskify.data.repository.TaskRepository
import com.example.taskify.data.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle  // Removed 'private' since it's only used in init
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle["taskId"])

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    init {
        loadTask()  // Call private method directly
    }

    private fun loadTask() {  // Made private since it's only used internally
        viewModelScope.launch {
            taskRepository.getTaskById(taskId)
                .collect { task ->
                    _task.value = task
                }
        }
    }

    fun updateTaskCompletionStatus(isCompleted: Boolean) {
        viewModelScope.launch {
            _task.value?.let { currentTask ->
                taskRepository.updateTask(currentTask.copy(isCompleted = isCompleted))
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            taskRepository.deleteTaskById(taskId)
        }
    }
}
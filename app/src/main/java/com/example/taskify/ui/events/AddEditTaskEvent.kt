package com.example.taskify.ui.events

sealed class AddEditTaskEvent {
    data object NavigateBack : AddEditTaskEvent()
    data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
}
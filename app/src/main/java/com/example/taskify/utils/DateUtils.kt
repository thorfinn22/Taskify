package com.example.taskify.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private fun getDateFormat() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private fun getTimeFormat() = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatDate(date: Date): String = getDateFormat().format(date)
    fun formatTime(date: Date): String = getTimeFormat().format(date)

    fun formatDueDate(dueDate: Date): String {
        val due = Calendar.getInstance().apply { time = dueDate }
        val now = Calendar.getInstance()

        return when {
            isToday(due) -> "Today"
            isTomorrow(due) -> "Tomorrow"
            isWithinWeek(now, due) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(dueDate)
            else -> getDateFormat().format(dueDate)
        }
    }

    private fun isToday(due: Calendar): Boolean {
        val now = Calendar.getInstance()
        return now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(due: Calendar): Boolean {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return tomorrow.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinWeek(now: Calendar, due: Calendar): Boolean {
        val differenceInMillis = due.timeInMillis - now.timeInMillis
        val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
        return differenceInDays in 0..6
    }
}
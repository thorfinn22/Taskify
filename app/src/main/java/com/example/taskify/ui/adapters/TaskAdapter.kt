package com.example.taskify.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskify.R
import com.example.taskify.databinding.ItemTaskBinding
import com.example.taskify.data.model.Task
import com.example.taskify.data.model.Priority
import com.example.taskify.utils.DateUtils

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClick(getItem(position))
                }
            }

            binding.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskCheckedChange(getItem(position), isChecked)
                }
            }
        }

        fun bind(task: Task) {
            with(binding) {
                tvTaskTitle.text = task.title
                tvTaskDescription.text = task.description
                checkboxTask.isChecked = task.isCompleted

                // Set priority chip
                chipPriority.text = task.priority.name
                val chipColorRes = when (task.priority) {
                    Priority.HIGH -> R.color.priority_high
                    Priority.MEDIUM -> R.color.priority_medium
                    Priority.LOW -> R.color.priority_low
                }
                chipPriority.setChipBackgroundColorResource(chipColorRes)

                // Set due date
                tvDueDate.text = DateUtils.formatDueDate(task.dueDate)

                // Set strikethrough if completed
                val textFlags = if (task.isCompleted) {
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    0
                }
                tvTaskTitle.paintFlags = textFlags
                tvTaskDescription.paintFlags = textFlags
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
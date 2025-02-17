package com.example.taskify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskify.R
import com.example.taskify.databinding.FragmentAddEditTaskBinding
import com.example.taskify.data.model.Priority
import com.example.taskify.ui.events.AddEditTaskEvent
import com.example.taskify.ui.viewmodels.AddEditTaskViewModel
import com.example.taskify.utils.DateUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {
    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditTaskFragmentArgs by navArgs()
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init(args.taskId)

        setupPriorityDropdown()
        setupDatePicker()
        setupTextWatchers()
        setupSaveButton()
        observeTask()
        observeEvents()
    }

    private fun setupPriorityDropdown() {
        val priorities = Priority.entries.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priorities
        )
        binding.actPriority.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_due_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
            }
            showTimePicker(calendar)
        }

        datePicker.show(parentFragmentManager, "date_picker")
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(getString(R.string.select_due_time))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)

            binding.etDueDate.setText(getString(
                R.string.date_time_format,
                DateUtils.formatDate(calendar.time),
                DateUtils.formatTime(calendar.time)
            ))
            viewModel.updateDueDate(calendar.time)
        }

        timePicker.show(parentFragmentManager, "time_picker")
    }

    private fun setupTextWatchers() {
        binding.apply {
            etTitle.doAfterTextChanged {
                viewModel.updateTitle(it?.toString() ?: "")
            }
            etDescription.doAfterTextChanged {
                viewModel.updateDescription(it?.toString() ?: "")
            }
            actPriority.setOnItemClickListener { _, _, position, _ ->
                viewModel.updatePriority(Priority.entries[position])
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            viewModel.saveTask()
        }
    }

    private fun observeTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let { currentTask ->
                    binding.apply {
                        etTitle.setText(currentTask.title)
                        etDescription.setText(currentTask.description)
                        actPriority.setText(currentTask.priority.name, false)
                        etDueDate.setText(getString(
                            R.string.date_time_format,
                            DateUtils.formatDate(currentTask.dueDate),
                            DateUtils.formatTime(currentTask.dueDate)
                        ))
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    AddEditTaskEvent.NavigateBack -> {
                        findNavController().popBackStack()
                    }
                    is AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(
                            requireView(),
                            event.msg,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
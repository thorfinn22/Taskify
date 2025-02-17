package com.example.taskify.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskify.R
import com.example.taskify.databinding.FragmentTaskDetailBinding
import com.example.taskify.utils.DateUtils
import com.example.taskify.ui.viewmodels.TaskDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val args: TaskDetailFragmentArgs by navArgs()
    private val viewModel: TaskDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupTaskCompletionCheckbox()
        observeTask()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_task_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        val action = TaskDetailFragmentDirections
                            .actionTaskDetailFragmentToAddEditTaskFragment(
                                taskId = args.taskId,
                                title = getString(R.string.edit_task)
                            )
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupTaskCompletionCheckbox() {
        binding.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTaskCompletionStatus(isChecked)
        }
    }

    private fun observeTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let { currentTask ->
                    binding.apply {
                        tvTaskTitle.text = currentTask.title
                        tvTaskDescription.text = currentTask.description
                        tvDueDate.text = getString(
                            R.string.due_date_format,
                            DateUtils.formatDueDate(currentTask.dueDate)
                        )
                        chipPriority.text = currentTask.priority.name
                        checkboxComplete.isChecked = currentTask.isCompleted
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_task)
            .setMessage(R.string.delete_task_confirmation)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTask()
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
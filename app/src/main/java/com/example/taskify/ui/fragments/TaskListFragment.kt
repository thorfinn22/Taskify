package com.example.taskify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskify.databinding.FragmentTaskListBinding
import com.example.taskify.ui.adapters.TaskAdapter
import com.example.taskify.ui.viewmodels.TaskListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskListFragment : Fragment() {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels()

    private val taskAdapter = TaskAdapter(
        onTaskClick = { task ->
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskDetailFragment(task.id)
            findNavController().navigate(action)
        },
        onTaskCheckedChange = { task, isChecked ->
            viewModel.updateTaskCompletionStatus(task, isChecked)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeTasks()
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        adapter = taskAdapter
        layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true)
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collectLatest { tasks ->
                taskAdapter.submitList(tasks)
                binding.emptyView.visibility = if (tasks.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
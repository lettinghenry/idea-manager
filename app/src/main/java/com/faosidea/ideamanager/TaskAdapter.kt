package com.faosidea.ideamanager

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskCheckedChange: (Task) -> Unit
) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, onTaskCheckedChange)
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val taskTextView: TextView = itemView.findViewById(R.id.task_text)
        private val taskCheckbox: CheckBox = itemView.findViewById(R.id.task_checkbox)

        fun bind(task: Task, onTaskCheckedChange: (Task) -> Unit) {

            taskTextView.text = task.title + ""

            if (task.isCompleted) {

            }

            taskCheckbox.setOnCheckedChangeListener(null)

            // Set the new listener
            taskCheckbox.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    val updatedTask = task.copy(isCompleted = isChecked)
                    onTaskCheckedChange(updatedTask)
                }
            }
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    private fun navigateToViewEditTaskActivity() {
        // Create an Intent to start the ViewEditActivity
//        val intent = Intent(this, ViewEditActivity::class.java)
//         intent.putExtra("MODE", "EDIT")
//        startActivity(intent)
    }
}
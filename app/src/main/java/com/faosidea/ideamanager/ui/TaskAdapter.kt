package com.faosidea.ideamanager.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faosidea.ideamanager.R
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.Utils.setStrikeThrough

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskCheckedChange: (Task) -> Unit
) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(context, task, onTaskCheckedChange)
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val taskTextView: TextView = itemView.findViewById(R.id.task_text)
        private val taskCheckbox: CheckBox = itemView.findViewById(R.id.task_checkbox)

        fun bind(context: Context, task: Task, onTaskCheckedChange: (Task) -> Unit) {

            taskTextView.text = task.title + ""

            //remove the checkchange listener before changing check state to avoid feedback loop
            taskCheckbox.setOnCheckedChangeListener(null)

            taskCheckbox.isChecked = task.isCompleted
            taskTextView.setStrikeThrough(task.isCompleted)

            // Set the checkchange listener
            taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (task.isCompleted != isChecked) {
                    val updatedTask = task.copy(isCompleted = isChecked)
                    onTaskCheckedChange(updatedTask)
                }
            }

            taskTextView.setOnClickListener {
                // Create an Intent to start the ViewEditActivity
                val intent = Intent(context, ViewEditActivity::class.java)
                intent.putExtra("TASK_ID", task.id)
                context.startActivity(intent)
            }

        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }


}
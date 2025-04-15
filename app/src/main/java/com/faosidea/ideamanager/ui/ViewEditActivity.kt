package com.faosidea.ideamanager.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.faosidea.ideamanager.R
import com.faosidea.ideamanager.Utils
import com.faosidea.ideamanager.Utils.setStrikeThrough
import com.faosidea.ideamanager.Utils.showDeleteConfirmationDialog
import com.faosidea.ideamanager.Utils.validateText
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskViewModel
import com.faosidea.ideamanager.databinding.ActivityCreateTaskBinding
import com.faosidea.ideamanager.databinding.ActivityViewEditBinding

class ViewEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewEditBinding
    val taskViewModel: TaskViewModel by viewModels()
    var selectedDate = 0L
     var taskId = 0L
     var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //get the ID
        taskId = intent.getLongExtra("TASK_ID", 0)
        try {
            taskViewModel.getTaskById(taskId).observe(this) { task ->
                if (task != null) {
                    completed = task.isCompleted
                    binding.titleInputLayout.setText(task.title)
                    binding.contentInputLayout.setText(task.description)
                    binding.dateInputLayout.setText(Utils.formatDate(task.dueDate))
                    markAllViewsComplete(completed)

                    //set up listeners
                    setUpUIListeners(task)
                }
            }
        } catch (e: Exception) {
            Log.d("Data Error", "Error while loading task :$taskId:\n $e")
            Toast.makeText(this, "Task Manager Encountered an Issue!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

    }


    fun setUpUIListeners(task: Task) {

        //back
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        //delete
        binding.deleteButton.setOnClickListener {
            handleDelete(task)
        }
        //mark complete
        binding.completeButton.setOnClickListener {
            handleMarkComplete(task)
        }

        binding.dateInputLayout.setOnClickListener {
            selectDate()
        }

    }

    /**
     * Strikethrough all texts
     */
    fun markAllViewsComplete(isComplete: Boolean) {
        binding.titleInputLayout.setStrikeThrough(isComplete)
        binding.dateInputLayout.setStrikeThrough(isComplete)
        binding.contentInputLayout.setStrikeThrough(isComplete)

        if(isComplete){
            binding.completeButton.visibility = View.GONE
        }

    }

    /**
     * to handle the marking of a task as complete from within the activity
     */
    fun handleMarkComplete(task: Task) {

        taskViewModel.toggleTaskCompleted(task)
        binding.completeButton.visibility = View.GONE

    }

    /**
     * execute item deletion
     */
    fun handleDelete(task: Task) {
        showDeleteConfirmationDialog(this) {
           taskViewModel.delete(task)
            Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }

    /**
     * due date selection from calendar
     */
    fun selectDate() {
        selectedDate = Utils.selectDate(binding.dateInputLayout, this@ViewEditActivity)
    }


    override fun onBackPressed() {
        //validate before insert
        if (validateText(binding.titleInputLayout) && validateText(binding.dateInputLayout)) {
            val title = binding.titleInputLayout.text.toString()
            val description = binding.contentInputLayout.text.toString()
            val dueDate = selectedDate

            taskViewModel.update(
                Task(
                    id = taskId ,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    isCompleted = completed
                )
            )
            //show update
            Toast.makeText(this, "Edited!", Toast.LENGTH_SHORT).show()
        }

        super.onBackPressed()
    }

}
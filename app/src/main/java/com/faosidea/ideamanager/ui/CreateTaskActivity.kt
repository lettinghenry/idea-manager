package com.faosidea.ideamanager.ui

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.faosidea.ideamanager.R
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskViewModel
import com.faosidea.ideamanager.Utils
import com.faosidea.ideamanager.Utils.validateText
import com.faosidea.ideamanager.databinding.ActivityCreateTaskBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import java.util.Locale

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    val taskViewModel: TaskViewModel by viewModels()
    var selectedDate = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //call to initialize listeners
        setUpUI()

    }


    fun setUpUI() {

        //click listeners
        binding.createButton.setOnClickListener {

            //validate before insert
            if (validateText(binding.titleEditText) && validateText(binding.dateEditText)) {

                val title = binding.titleEditText.text.toString()
                val description = binding.contentEditText.text.toString()
                val dueDate = selectedDate

                taskViewModel.insert(
                    Task(
                        title = title,
                        description = description,
                        dueDate = dueDate
                    )
                )

                //show update
                Toast.makeText(this, "Created Successfully!", Toast.LENGTH_SHORT).show()

                onBackPressed()

            }

        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.dateInputLayout.setOnClickListener {
            selectDate()
        }

        binding.dateEditText.setOnClickListener {
            selectDate()
        }

    }

    /**
     * due date selection from calendar
     */
    fun selectDate() {
        selectedDate = Utils.selectDate(binding.dateEditText, this@CreateTaskActivity)
    }


}
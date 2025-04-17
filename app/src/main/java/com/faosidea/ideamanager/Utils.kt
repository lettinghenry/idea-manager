package com.faosidea.ideamanager

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskDao
import com.faosidea.ideamanager.data.TaskRepository
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object Utils {

    /**
     * An elaborate check for string nullness and emptiness
     */
    fun isEmpty(string: String?): Boolean {
        return string.isNullOrEmpty() ||
                string.isEmpty() ||
                string.trim() == "" ||
                string.equals("null", true)
    }


    fun TextView.setStrikeThrough(strikeThrough: Boolean) {
        if (strikeThrough) {
            // Add the strike-through flag while keeping other flags
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            // Remove the strike-through flag while keeping other flags
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun EditText.setStrikeThrough(strikeThrough: Boolean) {
        if (strikeThrough) {
            // Add the strike-through flag while keeping other flags
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            // Remove the strike-through flag while keeping other flags
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    /**
     *  date selection from calendar
     */
    fun selectDate(view: View, activity: FragmentActivity,onDateSelected: (Long) -> Unit) {

        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Due date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { millis ->
            Log.d("DatePicker", "Selected millis :: $millis")

            //update UI
            (view as TextView).text = formatDate(millis)

            onDateSelected(millis)
        }

        picker.show(activity.supportFragmentManager, "DATE_PICKER")
    }

    // Helper function to format milliseconds into a readable date string
     fun formatDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formated = sdf.format(Date(milliseconds))
        Log.d("DatePicker", "Formated date :: $formated")
        return formated
    }

    /**
     * helper to delete task item
     */
    fun showDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirm_deletion))
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete))
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.delete)) { dialog, which ->
                onConfirm()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * validate input
     */
    fun validateText(textView: TextView): Boolean {
        val text = (textView.text.toString() + "")
        val isValid = !Utils.isEmpty(text)

        if (!isValid) {
            textView.error = "invalid input length!"
        }
        return isValid
    }


    /**
     * Helper function to help in tests
     */
    fun <T> LiveData<T>.getOrAwaitValue(): T {
        var data: T? = null
        val latch = CountDownLatch(1)

        val observer = object : Observer<T> {
            override fun onChanged(t: T) {
                data = t
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw TimeoutException("LiveData value was never set.")
        }

        return data!!
    }


    class FakeTaskRepository : ITaskRepository {

        private val tasks = mutableListOf<Task>()
        private val taskList = MutableLiveData<List<Task>>()

        override val allTasks: LiveData<List<Task>> get() = taskList

        fun setTasks(taskItems: List<Task>) {
            tasks.clear()
            tasks.addAll(taskItems)
            taskList.value = taskItems
        }

        override suspend fun insert(task: Task) {
            tasks.add(task)
            taskList.postValue(tasks.toList())
        }

        override suspend fun update(task: Task) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = task
                taskList.postValue(tasks.toList())
            }
        }

        override suspend fun delete(task: Task) {
            tasks.removeIf { it.id == task.id }
            taskList.postValue(tasks.toList())
        }

        override suspend fun getTaskById(taskId: Long): Task? {
            return tasks.find { it.id == taskId }
        }

        override fun getTaskByIdLive(taskId: Long): LiveData<Task?> {
            TODO("Not yet implemented")
        }
    }

    interface ITaskRepository {
        val allTasks: LiveData<List<Task>>
        suspend fun insert(task: Task)
        suspend fun update(task: Task)
        suspend fun delete(task: Task)
        suspend fun getTaskById(taskId: Long): Task?
         fun getTaskByIdLive(taskId: Long): LiveData<Task?>
    }

    class FakeTaskDao : TaskDao {

        private val tasks = mutableListOf<Task>()
        private val tasksLiveData = MutableLiveData<List<Task>>()

        init {
            tasksLiveData.value = tasks
        }

        override fun getAllTasks(): LiveData<List<Task>> = tasksLiveData

        override suspend fun insertTask(task: Task) {
            tasks.add(task)
            refresh()
        }

        override suspend fun updateTask(task: Task) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = task
                refresh()
            }
        }

        override suspend fun deleteTask(task: Task) {
            tasks.removeIf { it.id == task.id }
            refresh()
        }

        override suspend fun getTaskById(taskId: Long): Task? {
            return tasks.find { it.id == taskId }
        }

        override fun getTaskByIdLive(taskId: Long): LiveData<Task?> {
            TODO("Not yet implemented")
        }

        override fun getTasksByCompletion(completed: Boolean): LiveData<List<Task>> {
            TODO("Not yet implemented")
        }

        override suspend fun getTasksDueWithinDay(now: Long, nextDay: Long): List<Task> {
            return tasks.filter { it.dueDate in now..nextDay }
        }

        private fun refresh() {
            tasksLiveData.postValue(tasks.toList())
        }
    }


}
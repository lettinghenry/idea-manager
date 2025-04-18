package com.faosidea.ideamanager.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.faosidea.ideamanager.data.FilterState
import com.faosidea.ideamanager.R
import com.faosidea.ideamanager.ReminderWorker
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskViewModel
import com.faosidea.ideamanager.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_variant)
        supportActionBar?.title = getString(R.string.tasks)

        // Initialize the adapter, initially with an empty list
        taskAdapter = TaskAdapter(emptyList(), ::onTaskCheckedChange)
        binding.recyclerView.adapter = taskAdapter

        binding.fab.setOnClickListener {
            // Handle FAB
            navigateToCreateTaskActivity()
        }


        // Observe the tasks LiveData from the ViewModel
        taskViewModel.filteredTasks.observe(this) { filteredList ->
            taskAdapter.updateTasks(filteredList)

            //empty view toggle
            toggleEmptyState(filteredList.isEmpty())
        }

        //create notification channel
        createNotificationChannel()

        //Start reminder worker
        scheduleReminderWorker()
    }

    fun toggleEmptyState(isEmpty: Boolean) {

        //Hide or show the empty state view
        showEmptyState(isEmpty)

        //change text to match the filter
        if (isEmpty) {

            var emptyStateText =
                getString(R.string.no_tasks_yet_n_nclick_on_the_button_nto_add_a_new_task)

            when (taskViewModel._filterState.value.toString()
            ) {
                FilterState.ALL.toString() -> {
                    //do nothing, rely on the default
                }

                FilterState.PENDING.toString() -> {
                    emptyStateText = getString(R.string.you_have_no_pending_tasks)
                }

                FilterState.COMPLETED.toString() -> {
                    emptyStateText = getString(R.string.no_tasks_have_been_completed)
                }

                null -> {
                    //do nothing, rely on the default
                }
            }

            binding.emptyStateTextview.text = emptyStateText

        }
    }

    fun showEmptyState(show: Boolean) {
        if (show) {
            binding.emptyStateTextview.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateTextview.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * function to perform task status change
     */
    fun onTaskCheckedChange(task: Task) {
        taskViewModel.update(task)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_all_tasks -> {
                onFilterItems(FilterState.ALL)
                true
            }

            R.id.action_pending_tasks -> {
                onFilterItems(FilterState.PENDING)
                true
            }

            R.id.action_complete_tasks -> {
                onFilterItems(FilterState.COMPLETED)
                true
            }

            else -> false
        }
    }

    /**
     * function to filter tasks from the toolbar menu
     */
    fun onFilterItems(state: FilterState) {
        taskViewModel.setFilter(state) // or ALL, or PENDING
    }

    /**
     * navigation
     */
    private fun navigateToCreateTaskActivity() {
        val intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
    }

    /**
     * Notification channel
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming tasks"
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule reminder to check after a day
     */
    fun scheduleReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TaskReminderWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }


}
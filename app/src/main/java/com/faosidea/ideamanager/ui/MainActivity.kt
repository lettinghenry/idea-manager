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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.faosidea.ideamanager.ui.theme.AndroidComposeTheme
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_variant)
        supportActionBar?.title = getString(R.string.tasks)


        binding.fab.setOnClickListener {
            // Handle FAB
            navigateToCreateTaskActivity()
        }

        //attaching composable
        binding.mainScreenComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainScreen(
                    viewModel = taskViewModel,
                    onNavigateToTaskDetail = { task ->
                        onTaskItemClick(task)
                    }
                )
            }
        }

        // Observe the tasks LiveData from the ViewModel - Handled by composable

        //create notification channel
        createNotificationChannel()

        //Start reminder worker
        scheduleReminderWorker()
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

    fun onTaskItemClick(task: Task) {
        // Create an Intent to start the ViewEditActivity
        val intent = Intent(this@MainActivity, ViewEditActivity::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }

}


@OptIn(ExperimentalMaterial3Api::class) // If using Material 3 Experimental APIs
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel,
    onNavigateToTaskDetail: (Task) -> Unit

) {

    val tasks by viewModel.filteredTasks.observeAsState(initial = emptyList())

    AndroidComposeTheme {
        Scaffold { paddingValues ->
            TaskList(
                tasks = tasks,
                onTaskCheckedChange = { taskToUpdate,
                                        newCompletionState ->
                    viewModel.toggleTaskCompleted(taskToUpdate)

                },
                onTaskItemClick = { task ->
                    onNavigateToTaskDetail(task)
                },
                modifier = Modifier.padding(paddingValues)
            )
        }

    }
}

@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    taskName: String,
    isCompleted: Boolean,
    onCompletedChange: (Boolean) -> Unit,
    onItemClick: () -> Unit
) {
    //item_task.txt
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
            .clickable(onClick = onItemClick),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = isCompleted,
            onCheckedChange = onCompletedChange
        )
        Text(
            text = taskName,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            style = if (isCompleted) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun TaskItemPreview(modifier: Modifier = Modifier) {
    //item_task.txt
    MaterialTheme {
        TaskItem(
            taskName = "Task Title",
            isCompleted = false,
            onCompletedChange = {},
            onItemClick = {})
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onTaskItemClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        //Display a message when the list is empty
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_tasks_yet_n_nclick_on_the_button_nto_add_a_new_task),
                fontSize = 18.sp

            )
        }

    } else {
        LazyColumn(modifier = modifier) {
            items(items = tasks, key = { task -> task.id }) { task ->

                TaskItem(
                    taskName = task.title,
                    isCompleted = task.isCompleted,
                    onCompletedChange = { isNowCompleted ->
                        onTaskCheckedChange(
                            task,
                            isNowCompleted
                        )
                    },
                    onItemClick = { onTaskItemClick(task) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            }
        }
    }
}

// Preview for your TaskList
@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    // Sample data for the preview
    val sampleTasks = listOf(
        Task(
            id = 1,
            title = "Grocery Shopping",
            description = "",
            dueDate = 0L,
            isCompleted = false
        ),
        Task(
            id = 2,
            title = "Book Doctor Appointment",
            description = "",
            dueDate = 0L,
            isCompleted = true
        ),
        Task(
            id = 3,
            title = "Work on Compose Project",
            description = "",
            dueDate = 0L,
            isCompleted = false
        )
    )
    MaterialTheme {
        TaskList(
            tasks = sampleTasks,
            onTaskCheckedChange = { task, isChecked ->
                println("Preview: Task '${task.title}' checked: $isChecked")
            },
            onTaskItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyTaskListPreview() {
    MaterialTheme {
        TaskList(
            tasks = emptyList(),
            onTaskCheckedChange = { _, _ -> },
            onTaskItemClick = {}
        )
    }
}

package com.faosidea.ideamanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.faosidea.ideamanager.data.FilterState
import com.faosidea.ideamanager.R
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskViewModel
import com.faosidea.ideamanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter_variant)

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

    private fun navigateToCreateTaskActivity() {
        val intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
    }


}
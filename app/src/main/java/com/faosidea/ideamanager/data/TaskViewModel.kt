package com.faosidea.ideamanager.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.faosidea.ideamanager.Utils
import kotlinx.coroutines.launch

//enum to hold possible filter states that will be selected by the UI
enum class FilterState { ALL, PENDING, COMPLETED }

open class TaskViewModel(
    application: Application,
    private val repository: Utils.ITaskRepository = TaskRepository(
        IdeaDatabase.getDatabase(
            application
        ).taskDao()
    )
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        TaskRepository(IdeaDatabase.getDatabase(application).taskDao())
    )

    val _allTasks: LiveData<List<Task>>

    // Filter state
    val _filterState = MutableLiveData(FilterState.ALL)
    val filterState: LiveData<FilterState> = _filterState

    // A MediatorLiveData to combine allTasks and filterState
    val filteredTasks = MediatorLiveData<List<Task>>()

    init {

        _allTasks = repository.allTasks

        // Add sources to the MediatorLiveData
        filteredTasks.addSource(_allTasks) { tasks ->
            filteredTasks.value = filterTasks(tasks, _filterState.value ?: FilterState.ALL)
        }
        filteredTasks.addSource(_filterState) { filter ->
            filteredTasks.value = filterTasks(_allTasks.value, filter)
        }
    }

    fun filterTasks(tasks: List<Task>?, filter: FilterState): List<Task> {
        return tasks?.filter {
            when (filter) {
                FilterState.ALL -> true
                FilterState.PENDING -> !it.isCompleted
                FilterState.COMPLETED -> it.isCompleted
            }
        } ?: listOf()
    }

    fun setFilter(filter: FilterState) {
        _filterState.value = filter
    }

    /**
     * non-blocking coroutines
     */
    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun toggleTaskCompleted(task: Task) = viewModelScope.launch {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        repository.update(updatedTask)
    }

    // fetch a single task for editing in ViewModel
    fun getTaskById(taskId: Long): LiveData<Task?> {

        val result = MutableLiveData<Task?>()
        viewModelScope.launch {
            result.postValue(repository.getTaskById(taskId))
            // Use postValue from background
        }
        return result
    }

    // fetch a single task for editing in ViewModel (Live)
    fun getTaskByIdLive(taskId: Long): LiveData<Task?> {
        return repository.getTaskByIdLive(taskId)
    }
}

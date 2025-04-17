package com.faosidea.ideamanager.data

import androidx.lifecycle.LiveData
import com.faosidea.ideamanager.Utils

open class TaskRepository(private val taskDao: TaskDao):Utils.ITaskRepository {

    override val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    override suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    override suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun getTasksDueWithinDay(now: Long, nextDay: Long): List<Task> {
        return taskDao.getTasksDueWithinDay(now, nextDay)
    }
}
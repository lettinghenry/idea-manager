package com.faosidea.ideamanager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    // Return Live Tasks
//    @Query("SELECT * FROM tasks ORDER BY isCompleted DESC, dueDate ASC")
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<Task>>

    //Return Id upon successful insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :taskId")
     fun getTaskByIdLive(taskId: Long): LiveData<Task?>

    //Query for filtering
    @Query("SELECT * FROM tasks WHERE isCompleted = :completed ORDER BY dueDate ASC")
    fun getTasksByCompletion(completed: Boolean): LiveData<List<Task>>

    //For CoroutineWorker
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :now AND :nextDay")
    suspend fun getTasksDueWithinDay(now: Long, nextDay: Long): List<Task>
}
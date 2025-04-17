package com.faosidea.ideamanager

import com.faosidea.ideamanager.data.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.faosidea.ideamanager.Utils.FakeTaskDao
import com.faosidea.ideamanager.Utils.getOrAwaitValue
import com.faosidea.ideamanager.data.Task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class TaskRepositoryTest {

    private lateinit var repository: TaskRepository
    private lateinit var fakeDao: FakeTaskDao

    @Before
    fun setUp() {
        fakeDao = FakeTaskDao()
        repository = TaskRepository(fakeDao)
    }

    @Test
    fun insertTask_savesTask() = runTest {
        val task = Task(id = 1, title = "Test", description = "description", isCompleted = false, dueDate = System.currentTimeMillis())
        repository.insert(task)
        val result = repository.allTasks.getOrAwaitValue()
        assertTrue(result.contains(task))
    }

    @Test
    fun updateTask_changesTask() = runTest {
        val task = Task(id = 1, title = "Before",description = "description2", isCompleted = false, dueDate = 0L)
        repository.insert(task)

        val updated = task.copy(title = "After")
        repository.update(updated)

        val result = repository.allTasks.getOrAwaitValue()
        assertTrue(result.any { it.title == "After" })
    }

    @Test
    fun deleteTask_removesTask() = runTest {
        val task = Task(id = 2, title = "To Delete",description = "description2", isCompleted = false, dueDate = 0L)
        repository.insert(task)
        repository.delete(task)
        val result = repository.allTasks.getOrAwaitValue()
        assertFalse(result.contains(task))
    }

    @Test
    fun getTaskById_returnsCorrectTask() = runTest {
        val task = Task(id = 3, title = "Find Me",description = "description3", isCompleted = true, dueDate = 0L)
        repository.insert(task)
        val result = repository.getTaskById(3)
        assertEquals(task, result)
    }

    @Test
    fun getTasksDueWithinDay_returnsFilteredTasks() = runTest {
        val now = System.currentTimeMillis()
        val later = now + 1000 * 60 * 60 * 24
        val taskToday = Task(id = 4, title = "Today",description = "description4", isCompleted = false, dueDate = now + 1000)
        val taskLater = Task(id = 5, title = "Later",description = "description5", isCompleted = false, dueDate = later + 100000)

        repository.insert(taskToday)
        repository.insert(taskLater)

        val result = repository.getTasksDueWithinDay(now, later)
        assertEquals(listOf(taskToday), result)
    }
}

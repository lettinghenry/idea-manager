package com.faosidea.ideamanager

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.faosidea.ideamanager.Utils.FakeTaskRepository
import com.faosidea.ideamanager.Utils.getOrAwaitValue
import com.faosidea.ideamanager.data.FilterState
import com.faosidea.ideamanager.data.Task
import com.faosidea.ideamanager.data.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    // Forces LiveData to execute instantly
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var viewModel: TaskViewModel

    private val task1 = Task( title = "Task 1", description = "Description 1",dueDate = 1L, isCompleted = false)
    private val task2 = Task(title = "Task 2", description = "Description 2",dueDate = 2L, isCompleted = true)
    private val task3 = Task( title = "Task 3", description = "Description 3",dueDate = 3L, isCompleted = false)

    @Before
    fun setUp() {
        fakeRepository = FakeTaskRepository()
        fakeRepository.setTasks(listOf(task1, task2, task3))
        val fakeApp = Application()

        viewModel = TaskViewModel(
            fakeApp,
            fakeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun testFilterAllTasks() {
        viewModel.setFilter(FilterState.ALL)
        val result = viewModel.filteredTasks.getOrAwaitValue()
        assertEquals(3, result.size)
    }

    @Test
    fun testFilterPendingTasks() {
        viewModel.setFilter(FilterState.PENDING)
        val result = viewModel.filteredTasks.getOrAwaitValue()
        assertEquals(2, result.size)
        assert(result.all { !it.isCompleted })
    }

    @Test
    fun testFilterCompletedTasks() {
        viewModel.setFilter(FilterState.COMPLETED)
        val result = viewModel.filteredTasks.getOrAwaitValue()
        assertEquals(1, result.size)
        assert(result.all { it.isCompleted })
    }
}

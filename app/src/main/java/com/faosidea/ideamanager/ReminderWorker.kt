package com.faosidea.ideamanager

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.faosidea.ideamanager.data.IdeaDatabase
import com.faosidea.ideamanager.data.TaskRepository
import com.faosidea.ideamanager.ui.MainActivity
import com.faosidea.ideamanager.Utils.showNotification

class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskDao = IdeaDatabase.getDatabase(applicationContext).taskDao()
        val repository = TaskRepository(taskDao)

        val now = System.currentTimeMillis()
        val oneDayLater = now + 24 * 60 * 60 * 1000

        val upcomingTasks = repository.getTasksDueWithinDay(now, oneDayLater)

        if(upcomingTasks.isNotEmpty()){
            showNotification(applicationContext)
        }

        return Result.success()
    }
}
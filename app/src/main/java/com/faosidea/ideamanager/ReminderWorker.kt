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

class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskDao = IdeaDatabase.getDatabase(applicationContext).taskDao()
        val repository = TaskRepository(taskDao)

        val now = System.currentTimeMillis()
        val oneDayLater = now + 24 * 60 * 60 * 1000

        val upcomingTasks = repository.getTasksDueWithinDay(now, oneDayLater)

        if(upcomingTasks.isNotEmpty()){
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "task_channel")
            .setSmallIcon(R.drawable.ic_clock_check_outline)
            .setContentTitle("Upcoming Tasks")
            .setContentText("You have upcoming tasks within the day")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
}
package com.satwik.oodapplication.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import android.app.PendingIntent
import android.content.Intent
import com.satwik.oodapplication.MainActivity
import com.satwik.oodapplication.R
import java.util.*
import java.util.concurrent.TimeUnit

class FoodReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "meal_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Food Count Reminder")
            .setContentText("Don't forget to update tomorrow's food count before 10:00 PM!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
        
        // Reschedule for next day
        scheduleNext(applicationContext)
        
        return Result.success()
    }

    companion object {
        fun scheduleNext(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21) // 9:00 PM
                set(Calendar.MINUTE, 30)      // 30 mins
                set(Calendar.SECOND, 0)
            }

            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }

            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            
            val workRequest = OneTimeWorkRequestBuilder<FoodReminderWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .addTag("food_reminder")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "food_reminder_task",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}

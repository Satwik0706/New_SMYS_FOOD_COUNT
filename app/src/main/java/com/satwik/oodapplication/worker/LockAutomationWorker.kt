package com.satwik.oodapplication.worker

import android.content.Context
import androidx.work.*
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class LockAutomationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LockAutomationEntryPoint {
        fun foodCountRepository(): FoodCountRepository
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            LockAutomationEntryPoint::class.java
        )
        val repository = entryPoint.foodCountRepository()

        // Wait for actual data, skip Loading state
        val lockResource = repository.getLockStatus(Constants.ACTIVE_LOCK_ID)
            .filterIsInstance<Resource.Success<LockStatus>>()
            .first()
            
        val currentLock = lockResource.data ?: return Result.success()

        if (!currentLock.automationEnabled) {
            return Result.success()
        }

        val now = Calendar.getInstance()
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]
        val m = hour * 60 + minute

        // LOGIC REWRITE: Atomic Field Comparison
        // This ensures Snack is NEVER touched by automation.
        
        // 5:00 AM (300) to 8:00 PM (1200) -> Breakfast should be LOCKED
        val targetB = m in 300..1199
        // 9:00 AM (540) to 8:00 PM (1200) -> Lunch should be LOCKED
        val targetL = m in 540..1199
        // 4:30 PM (990) to 8:00 PM (1200) -> Dinner should be LOCKED
        val targetD = m in 990..1199
        
        // Portal Unlock at 8:00 PM (1200)
        if (m in 1200..1205) {
            if (currentLock.locked) repository.updateSingleLock("locked", false)
            if (currentLock.breakfastLocked) repository.updateSingleLock("breakfastLocked", false)
            if (currentLock.lunchLocked) repository.updateSingleLock("lunchLocked", false)
            if (currentLock.dinnerLocked) repository.updateSingleLock("dinnerLocked", false)
        }

        // Apply state-based locking
        if (m < 1200) {
            if (currentLock.breakfastLocked != targetB) {
                repository.updateSingleLock("breakfastLocked", targetB)
            }
            if (currentLock.lunchLocked != targetL) {
                repository.updateSingleLock("lunchLocked", targetL)
            }
            if (currentLock.dinnerLocked != targetD) {
                repository.updateSingleLock("dinnerLocked", targetD)
            }
        }

        scheduleNext(applicationContext)
        return Result.success()
    }

    companion object {
        fun scheduleNext(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<LockAutomationWorker>()
                .setInitialDelay(5, TimeUnit.MINUTES)
                .addTag("lock_automation")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "lock_automation_task_v2",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun startImmediately(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<LockAutomationWorker>()
                .addTag("lock_automation")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "lock_automation_task_v2",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}

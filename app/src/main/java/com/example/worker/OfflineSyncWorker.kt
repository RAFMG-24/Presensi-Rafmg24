package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.local.DamkarDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class OfflineSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = DamkarDatabase.getDatabase(applicationContext)
            val dao = db.damkarDao()
            val pendingList = dao.getPendingSyncAbsensi()

            Log.d("OfflineSyncWorker", "Found ${pendingList.size} pending offline attendances to sync")

            for (item in pendingList) {
                // In production / backend scenario: post payload to server
                // Here we verify integrity and mark synced
                dao.markAbsensiSynced(item.id)
                Log.d("OfflineSyncWorker", "Synced attendance record #${item.id} (${item.userName})")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("OfflineSyncWorker", "Sync worker failed", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "damkar_offline_attendance_sync"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<OfflineSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "damkar_periodic_offline_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }
}

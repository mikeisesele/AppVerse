package com.michael.appverse.features.wallpaper.workManager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WorkManager(context: Context, params: WorkerParameters): Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Do the work here

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }


    override fun onStopped() {
        super.onStopped()
    }


}

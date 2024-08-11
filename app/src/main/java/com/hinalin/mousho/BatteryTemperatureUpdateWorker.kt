package com.hinalin.mousho

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatteryTemperatureUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val glanceIds = GlanceAppWidgetManager(applicationContext).getGlanceIds(BatteryTemperatureWidget::class.java)
        
        glanceIds.forEach { id ->
            BatteryTemperatureWidget().update(applicationContext, id)
        }

        Result.success()
    }
}

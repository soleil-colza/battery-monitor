package com.hinalin.mousho

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.hinalin.mousho.data.model.OverheatEvent
import java.time.LocalDateTime

class BatteryTemperatureMonitor(private val context: Context) {

    var isOverheated: Boolean = false
        private set

    var currentTemperature: Float = 0f
        private set

    var overheatThreshold: Float = 40f

    var onTemperatureChanged: ((Float) -> Unit)? = null
    var onOverheatedChanged: ((Boolean, Float) -> Unit)? = null
    var onOverheatDetected: ((Float) -> Unit)? = null

    private val overheatEvents = mutableListOf<OverheatEvent>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                currentTemperature = temperature
                val wasOverheated = isOverheated
                isOverheated = temperature > overheatThreshold
                onTemperatureChanged?.invoke(temperature)
                if (isOverheated != wasOverheated) {
                    onOverheatedChanged?.invoke(isOverheated, temperature)
                    if (isOverheated) {
                        onOverheatDetected?.invoke(temperature)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getOverheatEvents(): List<OverheatEvent> {
        val twentyFourHoursAgo = LocalDateTime.now().minusHours(24)
        return overheatEvents.filter { it.timestamp.isAfter(twentyFourHoursAgo) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayOverheatEvents(): List<OverheatEvent> {
        val startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        return overheatEvents.filter { it.timestamp.isAfter(startOfDay) }
    }

    fun getCurrentBatteryTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10.0f) ?: 0f
    }

    init {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun stopMonitoring(context: Context) {
        context.unregisterReceiver(broadcastReceiver)
    }
}
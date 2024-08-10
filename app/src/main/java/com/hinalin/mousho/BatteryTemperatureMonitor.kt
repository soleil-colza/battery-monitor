package com.hinalin.mousho

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class BatteryTemperatureMonitor(context: Context) {

    var isOverheating: Boolean = false
        private set

    var onOverheatingChanged: ((Boolean) -> Unit)? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // バッテリー温度を取得 (温度は℃x10の値で返される)
            val batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
            val wasOverheating = isOverheating
            isOverheating = batteryTemperature > 40.0f

            if (isOverheating != wasOverheating) {
                onOverheatingChanged?.invoke(isOverheating)
            }

            Log.d("BatteryTemperatureMonitor", "Battery temperature: $batteryTemperature°C, Overheating: $isOverheating")
        }
    }

    init {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun stopMonitoring(context: Context) {
        context.unregisterReceiver(broadcastReceiver)
    }
}
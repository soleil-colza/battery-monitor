package com.hinalin.mousho

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.hinalin.mousho.data.model.OverheatEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

class BatteryTemperatureMonitor(private val context: Context) {
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo

    var overheatThreshold: Float = 40f

    var onTemperatureChanged: ((Float) -> Unit)? = null
    var onOverheatedChanged: ((Boolean, Float) -> Unit)? = null
    var onOverheatDetected: ((Float) -> Unit)? = null

    private val overheatEvents = mutableListOf<OverheatEvent>()

    private val broadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

                    _batteryInfo.value =
                        BatteryInfo(
                            temperature = temperature,
                            isOverheated = temperature > overheatThreshold,
                            status = getBatteryStatusString(status),
                            health = getBatteryHealthString(health),
                            isPluggedIn = plugged != 0,
                            voltage = voltage / 1000.0f, // Convert mV to V
                        )

                    onTemperatureChanged?.invoke(temperature)
                    if (_batteryInfo.value.isOverheated) {
                        onOverheatedChanged?.invoke(true, temperature)
                        onOverheatDetected?.invoke(temperature)
                    }
                }
            }
        }

    init {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(broadcastReceiver, intentFilter)
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

    fun stopMonitoring() {
        context.unregisterReceiver(broadcastReceiver)
    }

    private fun getBatteryStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getBatteryHealthString(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }
}

data class BatteryInfo(
    val temperature: Float = 0f,
    val isOverheated: Boolean = false,
    val status: String = "",
    val health: String = "",
    val isPluggedIn: Boolean = false,
    val voltage: Float = 0f,
)

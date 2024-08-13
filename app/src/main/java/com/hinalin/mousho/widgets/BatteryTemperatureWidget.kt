package com.hinalin.mousho

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BatteryTemperatureWidget : GlanceAppWidget() {
    private val temperatureFlow = MutableStateFlow(0f)
    private val isOverheatedFlow = MutableStateFlow(false)
    var broadcastReceiver: BroadcastReceiver? = null

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                    val isOverheated = temperature > 40.0f // 実際の閾値設定を使用することをお勧めします
                    temperatureFlow.value = temperature
                    isOverheatedFlow.value = isOverheated
                    MainScope().launch {
                        updateWidget(context, id)
                    }
                }
            }
        }

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(broadcastReceiver, intentFilter)

        provideContent {
            val temperature by temperatureFlow.collectAsState()
            val isOverheated by isOverheatedFlow.collectAsState()
            BatteryTemperatureWidgetContent(temperature, isOverheated)
        }
    }

    private suspend fun updateWidget(context: Context, id: GlanceId) {
        update(context, id)
    }
}

@Composable
private fun BatteryTemperatureWidgetContent(temperature: Float, isOverheated: Boolean) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverheated) "Burning out!🔋" else "Comfy！🔋",
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = ColorProvider(
                    day = if (isOverheated) Color.Red else Color.Green,
                    night = if (isOverheated) Color.Red else Color.Green
                )
            )
        )
        Text(
            text = "Temperature: ${String.format("%.1f", temperature)}°C",
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(
                    day = Color.Black,
                    night = Color.White
                )
            )
        )
    }
}

class BatteryTemperatureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryTemperatureWidget()

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.unregisterReceiver((glanceAppWidget as BatteryTemperatureWidget).broadcastReceiver)
    }
}

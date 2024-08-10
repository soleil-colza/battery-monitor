package com.hinalin.mousho

import android.content.Context
import androidx.compose.runtime.Composable
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

class BatteryTemperatureWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val batteryMonitor = BatteryTemperatureMonitor(context)
        val temperature = batteryMonitor.currentTemperature
        val isOverheated = batteryMonitor.isOverheated

        provideContent {
            BatteryTemperatureWidgetContent(temperature, isOverheated)
        }
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
}

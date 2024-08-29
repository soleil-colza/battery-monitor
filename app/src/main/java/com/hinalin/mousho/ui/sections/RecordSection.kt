package com.hinalin.mousho.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinalin.mousho.BatteryTemperatureMonitor
import com.hinalin.mousho.ui.components.OverheatEventCard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordSection(batteryMonitor: BatteryTemperatureMonitor) {
    Text(
        text = "Today's overheat record",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    val todayOverheatEvents by remember { mutableStateOf(batteryMonitor.getTodayOverheatEvents()) }

    if (todayOverheatEvents.isEmpty()) {
        Text(
            text = "No overheat today, stay cool!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    } else {
        LazyColumn {
            items(todayOverheatEvents) { event ->
                OverheatEventCard(event)
            }
        }
    }
}

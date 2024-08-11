package com.hinalin.mousho

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(batteryMonitor: BatteryTemperatureMonitor) {
    val todayOverheatEvents by remember { mutableStateOf(batteryMonitor.getTodayOverheatEvents()) }
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(todayOverheatEvents) { event ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Time: ${event.timestamp.format(dateFormatter)}")
                    Text(text = "Temperature: ${String.format("%.1f", event.temperature)}°C")
                }
            }
        }
        if (todayOverheatEvents.isEmpty()) {
            item {
                Text(
                    text = "No overheat events today. Stay cool!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

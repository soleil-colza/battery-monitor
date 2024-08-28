package com.hinalin.mousho.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinalin.mousho.BatteryTemperatureMonitor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordSection(batteryMonitor: BatteryTemperatureMonitor) {
    Text("Today's overheat record", style = MaterialTheme.typography.titleLarge)
    val todayOverheatEvents by remember { mutableStateOf(batteryMonitor.getTodayOverheatEvents()) }

    if (todayOverheatEvents.isEmpty()) {
        Text(
            text = "No overheat today, stay cool!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    } else {
        Column {
            todayOverheatEvents.forEach { event ->
                ElevatedCard(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    ) {
                        Text(
                            text = "Time: ${
                                event.timestamp.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                        "HH:mm:ss",
                                    ),
                                )
                            }",
                        )
                        Text(text = "Temperature: ${String.format("%.1f", event.temperature)}°C")
                    }
                }
            }
        }
    }
}

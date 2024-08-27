package com.hinalin.mousho.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.hinalin.mousho.BatteryTemperatureMonitor
import com.hinalin.mousho.R
import com.hinalin.mousho.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
private val OVERHEAT_THRESHOLD = floatPreferencesKey("overheat_threshold")

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MasterScreen(batteryMonitor: BatteryTemperatureMonitor) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationEnabled by remember { mutableStateOf(false) }
    var overheatThreshold by remember { mutableStateOf(40f) }
    var currentTemperature by remember { mutableStateOf(batteryMonitor.currentTemperature) }
    var isOverheated by remember { mutableStateOf(batteryMonitor.isOverheated) }

    LaunchedEffect(Unit) {
        context.dataStore.data.map { preferences ->
            Pair(
                preferences[NOTIFICATION_ENABLED] ?: true,
                preferences[OVERHEAT_THRESHOLD] ?: 40f,
            )
        }.collect { (enabled, threshold) ->
            notificationEnabled = enabled
            overheatThreshold = threshold
        }
    }

    batteryMonitor.onOverheatedChanged = { newIsOverheated, temperature ->
        isOverheated = newIsOverheated
        currentTemperature = temperature
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.cool_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )
        BatteryTemperatureDisplay(temperature = currentTemperature)
        HorizontalDivider()
        SettingsSection(
            notificationEnabled = notificationEnabled,
            overheatThreshold = overheatThreshold,
            onNotificationEnabledChange = { isEnabled ->
                notificationEnabled = isEnabled
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[NOTIFICATION_ENABLED] = isEnabled
                    }
                }
            },
            onThresholdChange = { newThreshold ->
                overheatThreshold = newThreshold
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[OVERHEAT_THRESHOLD] = newThreshold
                    }
                }
            },
        )
        HorizontalDivider()
        RecordSection(batteryMonitor = batteryMonitor)
    }
}

@Composable
fun BatteryTemperatureDisplay(temperature: Float) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${String.format("%.1f", temperature)}°C",
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
fun SettingsSection(
    notificationEnabled: Boolean,
    overheatThreshold: Float,
    onNotificationEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit,
) {
    Text("Settings", style = MaterialTheme.typography.titleLarge)
    NotificationSetting(
        notificationEnabled = notificationEnabled,
        onNotificationEnabledChange = onNotificationEnabledChange,
    )
    TemperatureThresholdSetting(
        overheatThreshold = overheatThreshold,
        onThresholdChange = onThresholdChange,
    )
}

@Composable
fun NotificationSetting(
    notificationEnabled: Boolean,
    onNotificationEnabledChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Receive notifications",
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = notificationEnabled,
            onCheckedChange = onNotificationEnabledChange,
        )
    }
}

@Composable
fun TemperatureThresholdSetting(
    overheatThreshold: Float,
    onThresholdChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Overheat threshold",
            style = MaterialTheme.typography.bodyLarge,
        )
        Slider(
            value = overheatThreshold,
            onValueChange = { onThresholdChange(it) },
            valueRange = 30f..50f,
            steps = 20,
        )
        Text(
            text = String.format("%.1f°C", overheatThreshold),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

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

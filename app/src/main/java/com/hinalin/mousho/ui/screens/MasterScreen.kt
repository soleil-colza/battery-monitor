package com.hinalin.mousho.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.hinalin.mousho.BatteryInfo
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
    val batteryInfo by batteryMonitor.batteryInfo.collectAsState()
    val scrollState = rememberScrollState()

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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShrinkableHeaderImage(
            imageRes = R.drawable.cool_bg,
            scrollState = scrollState,
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BatteryTemperatureDisplay(temperature = batteryInfo.temperature)
            StatusSection(batteryInfo)
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
fun StatusSection(batteryInfo: BatteryInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusCard(
                title = "Status",
                value = batteryInfo.status,
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                title = "Health",
                value = batteryInfo.health,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusCard(
                title = "Plugged In",
                value = if (batteryInfo.isPluggedIn) "Yes" else "No",
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                title = "Voltage",
                value = "${String.format("%.1f", batteryInfo.voltage)}V",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
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

@Composable
fun ShrinkableHeaderImage(
    imageRes: Int,
    scrollState: ScrollState,
) {
    val maxHeight = 200.dp
    val minHeight = 100.dp
    val height by remember {
        derivedStateOf {
            maxHeight - (scrollState.value / 2f).dp.coerceAtMost(maxHeight - minHeight)
        }
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height),
        contentScale = ContentScale.Crop,
    )
}

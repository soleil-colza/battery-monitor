package com.hinalin.mousho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.hinalin.mousho.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
private val OVERHEAT_THRESHOLD = floatPreferencesKey("overheat_threshold")

@Composable
fun SettingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationEnabled by remember { mutableStateOf(false) }
    var overheatThreshold by remember { mutableStateOf(40f) }

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
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotificationSetting(
            notificationEnabled = notificationEnabled,
            onNotificationEnabledChange = { isEnabled ->
                notificationEnabled = isEnabled
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[NOTIFICATION_ENABLED] = isEnabled
                    }
                }
            },
        )

        TemperatureThresholdSetting(
            overheatThreshold = overheatThreshold,
            onThresholdChange = { newThreshold ->
                overheatThreshold = newThreshold
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[OVERHEAT_THRESHOLD] = newThreshold
                    }
                }
            },
        )
    }
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
            text = "Overheat threshold temperature",
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

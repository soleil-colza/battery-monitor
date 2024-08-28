package com.hinalin.mousho.ui.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.hinalin.mousho.ui.components.TemperatureThresholdSetting
import com.hinalin.mousho.ui.screens.NotificationSetting

@Composable
fun SettingSection(
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

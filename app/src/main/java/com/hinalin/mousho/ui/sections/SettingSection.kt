package com.hinalin.mousho.ui.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinalin.mousho.ui.components.NotificationSetting
import com.hinalin.mousho.ui.components.TemperatureThresholdSetting

@Composable
fun SettingSection(
    notificationEnabled: Boolean,
    overheatThreshold: Float,
    onNotificationEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit,
) {
    Text(
        text = "Settings",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    TemperatureThresholdSetting(
        overheatThreshold = overheatThreshold,
        onThresholdChange = onThresholdChange,
    )

    NotificationSetting(
        notificationEnabled = notificationEnabled,
        onNotificationEnabledChange = onNotificationEnabledChange,
    )
}

package com.hinalin.mousho.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TemperatureThresholdSetting(
    overheatThreshold: Float,
    onThresholdChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Overheat threshold",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = String.format("%.1f°C", overheatThreshold),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Slider(
            value = overheatThreshold,
            onValueChange = { onThresholdChange(it) },
            valueRange = 30f..50f,
            steps = 20,
        )
    }
}

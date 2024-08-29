package com.hinalin.mousho.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hinalin.mousho.data.model.OverheatEvent
import com.hinalin.mousho.ui.theme.MoushoTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OverheatEventCard(event: OverheatEvent) {
    val temperature = event.temperature
    val color =
        remember(temperature) {
            when {
                temperature >= 45f -> Color(0xFFFF5252)
                temperature >= 42f -> Color(0xFFFFAB40)
                else -> Color(0xFFFFD740)
            }
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .alpha(0.7f)
                .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = color,
                contentColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "${String.format("%.1f", event.temperature)}°C",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                text = "${event.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun OverheatEventCardPreview() {
    val sampleEvents =
        listOf(
            OverheatEvent(
                timestamp = LocalDateTime.of(2023, 7, 1, 14, 30),
                temperature = 46.8f,
            ),
            OverheatEvent(
                timestamp = LocalDateTime.of(2023, 7, 1, 15, 45),
                temperature = 43.2f,
            ),
            OverheatEvent(
                timestamp = LocalDateTime.of(2023, 7, 1, 16, 15),
                temperature = 41.5f,
            ),
            OverheatEvent(
                timestamp = LocalDateTime.of(2023, 7, 1, 17, 0),
                temperature = 39.9f,
            ),
            OverheatEvent(
                timestamp = LocalDateTime.of(2023, 7, 1, 18, 30),
                temperature = 48.5f,
            ),
        )

    MoushoTheme {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(sampleEvents) { event ->
                OverheatEventCard(event = event)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

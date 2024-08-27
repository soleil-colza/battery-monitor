package com.hinalin.mousho.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp

@Composable
fun MasterScreen() {
}

@Composable
fun TemperatureDisplay() {
    val batteryTemperature by remember { mutableFloatStateOf(0.0f) }
    Text(text = batteryTemperature.toString())
}

@Composable
fun TemperatureDashboard() {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
    ) {
    }
}

@Composable
fun ShrinkableHeaderImage(
    imageRes: Int,
    scrollState: ScrollState,
) {
    val height =
        remember {
            200.dp - (scrollState.value / 5).dp.coerceAtLeast(48.dp)
        }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureRecordDropdown() {
    ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
        Text(
            text = "Temperature Record",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

@Composable
fun InfoGrid() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoCard("Title", "Value")
            InfoCard("Title", "Value")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoCard("Title", "Value")
            InfoCard("Title", "Value")
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
) {
    ElevatedCard(
        modifier =
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
//            .weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun PreviewInfoCard() {
    InfoCard("Title", "Value")
}

@Preview
@Composable
fun InfoGridPreview() {
    InfoGrid()
}

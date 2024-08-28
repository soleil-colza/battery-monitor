package com.hinalin.mousho.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.hinalin.mousho.BatteryTemperatureMonitor
import com.hinalin.mousho.R
import com.hinalin.mousho.dataStore
import com.hinalin.mousho.ui.components.BatteryTemperatureDisplay
import com.hinalin.mousho.ui.components.ShrinkableHeaderImage
import com.hinalin.mousho.ui.sections.RecordSection
import com.hinalin.mousho.ui.sections.SettingSection
import com.hinalin.mousho.ui.sections.StatusSection
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isPortrait = screenHeight > screenWidth

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
        ) {
            ShrinkableHeaderImage(
                hotImageRes = R.drawable.hot_bg,
                coolImageRes = R.drawable.cool_bg,
                isOverheated = batteryInfo.isOverheated,
                scrollState = scrollState,
                maxHeight = screenHeight * 0.3f,
                minHeight = screenHeight * 0.1f,
            )
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(
                            top =
                                WindowInsets.statusBars
                                    .asPaddingValues()
                                    .calculateTopPadding(),
                            bottom =
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding(),
                        ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BatteryTemperatureDisplay(temperature = batteryInfo.temperature)
                StatusSection(batteryInfo, isPortrait)
                HorizontalDivider()
                SettingSection(
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
}

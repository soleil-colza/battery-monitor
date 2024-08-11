package com.hinalin.mousho

import com.hinalin.mousho.notification.NotificationHelper
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hinalin.mousho.ui.composables.LottieAnimationView
import com.hinalin.mousho.ui.theme.MoushoTheme
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.lifecycleScope
import com.hinalin.mousho.ui.screens.SettingScreen
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
private val OVERHEAT_THRESHOLD = floatPreferencesKey("overheat_threshold")

class MainActivity : ComponentActivity() {

    private lateinit var batteryMonitor: BatteryTemperatureMonitor
    private lateinit var notificationHelper: NotificationHelper

    private val NOTIFICATION_PERMISSION_CODE = 1

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        notificationHelper = NotificationHelper(this)
        batteryMonitor = BatteryTemperatureMonitor(this)

        val updateRequest = PeriodicWorkRequestBuilder<BatteryTemperatureUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BatteryTemperatureUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )

        lifecycleScope.launch {
            dataStore.data.map { preferences ->
                preferences[OVERHEAT_THRESHOLD] ?: 40f
            }.collect { threshold ->
                batteryMonitor.overheatThreshold = threshold
            }
        }

        setContent {
            MoushoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(batteryMonitor)
                }
            }
        }

        batteryMonitor.onOverheatedChanged = { isOverheated, temperature ->
            if (isOverheated) {
                runBlocking {
                    val notificationEnabled = dataStore.data.map { preferences ->
                        preferences[NOTIFICATION_ENABLED] ?: true
                    }.first()
                    if (notificationEnabled) {
                        notificationHelper.showOverheatNotification()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryMonitor.stopMonitoring(this)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(batteryMonitor: BatteryTemperatureMonitor) {
    var isOverheated by remember { mutableStateOf(batteryMonitor.isOverheated) }
    var currentTemperature by remember { mutableStateOf(batteryMonitor.currentTemperature) }
    var currentScreen by remember { mutableStateOf(0) }

    batteryMonitor.onOverheatedChanged = { newIsOverheated, temperature ->
        isOverheated = newIsOverheated
        currentTemperature = temperature
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            0 -> if (isOverheated) "Burning hot!" else "Comfy!"
                            1 -> "Today's Overheat Events"
                            2 -> "Settings"
                            else -> "Battery Monitor"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(currentScreen) { screen ->
                currentScreen = screen
            }
        },
        floatingActionButton = {
            if (currentScreen == 0) {
                FloatingActionButton(
                    onClick = {
                        currentTemperature = batteryMonitor.getCurrentBatteryTemperature()
                        isOverheated = currentTemperature > batteryMonitor.overheatThreshold
                    }
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Update temperature")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (currentScreen) {
                0 -> {
                    LottieAnimationView(isOverheated = isOverheated)
                    BatteryTemperatureDisplay(temperature = currentTemperature)
                }
                1 -> RecordScreen(batteryMonitor)
                2 -> SettingScreen()
            }
        }
    }
}


@Composable
fun BottomNavigationBar(currentScreen: Int, onScreenChange: (Int) -> Unit) {
    val items = listOf("Home", "Record", "Setting")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Settings)

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = currentScreen == index,
                onClick = { onScreenChange(index) }
            )
        }
    }
}

@Composable
fun BatteryTemperatureDisplay(temperature: Float) {
    ElevatedCard(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(), // Adjusted to only fill the width
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Add padding to the content within the card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Text(
                text = "Current Battery Temperature",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${String.format("%.1f", temperature)}°C",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
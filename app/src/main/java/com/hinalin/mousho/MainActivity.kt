package com.hinalin.mousho

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hinalin.mousho.notification.NotificationHelper
import com.hinalin.mousho.ui.screens.MasterScreen
import com.hinalin.mousho.ui.theme.MoushoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

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
                    NOTIFICATION_PERMISSION_CODE,
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

        val updateRequest =
            PeriodicWorkRequestBuilder<BatteryTemperatureUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build(),
                )
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BatteryTemperatureUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest,
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
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(batteryMonitor)
                }
            }
        }

        batteryMonitor.onOverheatedChanged = { isOverheated, temperature ->
            if (isOverheated) {
                runBlocking {
                    val notificationEnabled =
                        dataStore.data.map { preferences ->
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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Battery Monitor",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MasterScreen(batteryMonitor)
        }
    }
}

package com.hinalin.mousho

import com.hinalin.mousho.notification.NotificationHelper
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.setValue

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                notificationHelper.showOverheatNotification()
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

    batteryMonitor.onTemperatureChanged = { temperature ->
        currentTemperature = temperature
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isOverheated) "Burning out!🔋" else "Comfy！🔋",
                        style = MaterialTheme.typography.headlineMedium
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize() // Columnが画面全体を占めるようにする
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (currentScreen) {
                0 -> {
                    LottieAnimationView(isOverheated = isOverheated)
                    BatteryTemperatureDisplay(temperature = currentTemperature)
                    Spacer(modifier = Modifier.height(16.dp)) // ここで隙間を追加
                }
                1 -> RecordScreen(batteryMonitor)
                2 -> Text("設定画面")
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
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Battery Temperature",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${String.format("%.1f", temperature)}°C",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(16.dp)) // Textの下にさらにスペースを追加
        }
    }
}

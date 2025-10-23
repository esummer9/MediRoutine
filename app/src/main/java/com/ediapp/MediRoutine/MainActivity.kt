package com.ediapp.MediRoutine

import android.app.AlarmManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ediapp.MediRoutine.ui.theme.MyApplicationTheme
import java.util.Calendar

import com.ediapp.MediRoutine.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Getting a writable database instance will create the database and call onCreate if needed.
        val dbHelper = DatabaseHelper(this)
        dbHelper.writableDatabase

        val sharedPref = getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)
        if (!sharedPref.contains("med_name")) {
            with(sharedPref.edit()) {
                putString("med_name", "약이름입력")
                apply()
            }
        }
        if (!sharedPref.contains("notification_time")) {
            with(sharedPref.edit()) {
                putString("notification_time", "08:00")
                apply()
            }
        }

        setContent {
            MyApplicationTheme {
                MyApplicationApp { onPermissionRequired ->
                    setAlarm(onPermissionRequired)
                }
            }
        }
    }

    private fun setAlarm(onPermissionRequired: () -> Unit) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                onPermissionRequired()
                return
            }
        }

        val sharedPref = getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)
        val time = sharedPref.getString("notification_time", "08:00")?.split(":")
        val hour = time?.get(0)?.toInt() ?: 8
        val minute = time?.get(1)?.toInt() ?: 0

        val sysTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = sysTime
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        Log.d("setAlarm", "${calendar.timeInMillis} $sysTime")

        if (calendar.timeInMillis <= sysTime) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(setAlarm: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Call setAlarm with a callback for when permission is needed
    LaunchedEffect(Unit) {
        setAlarm {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("알림권한 설정") },
            text = { Text("정확한 알람을 위해 알림권한이 필요합니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    }
                ) {
                    Text("권한 설정으로 이동")
                }
            },
//            dismissButton = {
//                TextButton(onClick = { showPermissionDialog = false }) {
//                    Text("취소")
//                }
//            }
        )
    }
//    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE) }

    var medName by rememberSaveable { mutableStateOf(prefs.getString("med_name", "") ?: "") }
    var morningEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("daily_report_enabled", false)) }
    var selectedTime by rememberSaveable { mutableStateOf(prefs.getString("notification_time", "08:00") ?: "08:00") }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val navigateTo: (AppDestinations) -> Unit = {
        newDestination ->
            if (currentDestination == AppDestinations.SETTINGS && medName.length < 2) {
                Toast.makeText(context, "약 이름은 2글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                if(currentDestination == AppDestinations.SETTINGS) {
                    with(prefs.edit()) {
                        putString("med_name", medName)
                        putBoolean("daily_report_enabled", morningEnabled)
                        apply()
                    }
                }
                currentDestination = newDestination
            }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { navigateTo(it) }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.label) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentDestination.color,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeFragment()
                    AppDestinations.FAVORITES -> DataListFragment()
                    AppDestinations.SETTINGS -> SettingsFragment(
                        medName = medName,
                        onMedNameChange = { medName = it },
                        morningEnabled = morningEnabled,
                        onMorningEnabledChange = { morningEnabled = it },
                        selectedTime = selectedTime,
                        onSelectedTimeChange = { newTime ->
                            selectedTime = newTime
                            with(prefs.edit()) {
                                putString("notification_time", newTime)
                                apply()
                            }
                            // Set up the alarm with permission handling
                            setAlarm {
                                // This will be called if permission is needed
                                showPermissionDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    HOME("홈", Icons.Default.Home, Color(0xFF00668B)),
    FAVORITES("데이터 목록", Icons.Default.DateRange, Color(0xFF008080)),
    SETTINGS("설정", Icons.Default.Settings, Color(0xFF6A5ACD)),
}

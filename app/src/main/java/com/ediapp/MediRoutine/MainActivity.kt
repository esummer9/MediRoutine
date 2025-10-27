package com.ediapp.MediRoutine

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.ediapp.MediRoutine.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = DatabaseHelper(this)
        dbHelper.writableDatabase

        val sharedPref = getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)
        if (!sharedPref.contains("med_name")) {
            with(sharedPref.edit()) {
                putString("med_name", "**약")
                apply()
            }
        }
        if (!sharedPref.contains("notification_time")) {
            with(sharedPref.edit()) {
                putString("notification_time", "08:00")
                apply()
            }
        }

        createNotificationChannel()

        setContent {
            MyApplicationTheme {
                MyApplicationApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MediRoutine Channel"
            val descriptionText = "Channel for MediRoutine notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("medi_routine_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp() {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                NotificationHelper.showNotification(context)
            } else {
                Toast.makeText(context, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showNotification(context)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            NotificationHelper.showNotification(context)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.permission_dialog_title)) },
            text = { Text(stringResource(R.string.permission_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    }
                ) {
                    Text(stringResource(R.string.permission_dialog_confirm_button))
                }
            },
        )
    }
    val prefs = remember { context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE) }

    var medName by rememberSaveable { mutableStateOf(prefs.getString("med_name", "") ?: "") }
    var morningEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("daily_report_enabled", false)) }
    var selectedTime by rememberSaveable { mutableStateOf(prefs.getString("notification_time", "08:00") ?: "08:00") }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val navigateTo: (AppDestinations) -> Unit = {
        newDestination ->
//            if (currentDestination == AppDestinations.HELPS && medName.length < 2) {
//                Toast.makeText(context, "약 이름은 2글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
//            } else {
                if(currentDestination == AppDestinations.HELPS) {
//                    with(prefs.edit()) {
//                        putString("med_name", medName)
//                        putBoolean("daily_report_enabled", morningEnabled)
//                        apply()
//                    }
                }
                currentDestination = newDestination
//            }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.label)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
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
                    title = { Text(stringResource(currentDestination.label)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentDestination.color,
                        titleContentColor = Color.White
                    ),
                    // 여기에 메뉴 아이템을 추가합니다.
                    actions = {
                        // 설정 메뉴 아이템
                        IconButton(onClick = { /* 설정 화면으로 이동하는 로직 */
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)

                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        // 백업 메뉴 아이템
                        IconButton(onClick = { /* 백업 기능 실행 로직 */ }) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Backup", tint = Color.White) // Backup 아이콘이 없다면 다른 아이콘 사용
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeFragment()
                    AppDestinations.LISTS -> ListFragment()
                    AppDestinations.HELPS -> HelpsFragment()
                }
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    val color: Color
) {
    HOME(R.string.tab_home, Icons.Default.Home, Color(0xFF00668B)),
    LISTS(R.string.tab_favorites, Icons.Default.DateRange, Color(0xFF008080)),
    HELPS(R.string.tab_helps, Icons.Default.Info, Color(0xFF8BC34A)),
}

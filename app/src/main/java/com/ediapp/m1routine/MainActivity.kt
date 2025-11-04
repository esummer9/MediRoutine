package com.ediapp.m1routine

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.ediapp.m1routine.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
class MainActivity : ComponentActivity() {
    private var showAnimation = mutableStateOf(false)

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
//                    Toast.makeText(
//                        this,
//                        "Fetch and activate succeeded",
//                        Toast.LENGTH_SHORT,
//                    ).show()
                } else {
//                    Toast.makeText(
//                        this,
//                        "Fetch failed",
//                        Toast.LENGTH_SHORT,
//                    ).show()
                }
                displayWelcomeMessage()
            }

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys)

                if (configUpdate.updatedKeys.contains("welcome_message")) {
                    remoteConfig.activate().addOnCompleteListener {
                        displayWelcomeMessage()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error with code: " + error.code, error)
            }
        })

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
                MyApplicationApp(showAnimation = showAnimation.value) {
                    showAnimation.value = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "com.ediapp.m1routine.ACTION_TAKE_MEDICINE") {
            showAnimation.value = true
            val dbHelper = DatabaseHelper(this)
            dbHelper.addDrugAction()
            NotificationHelper.showNotification(this)
        }
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

    private fun displayWelcomeMessage() {
        val version = remoteConfig["AppName"].asString()
        if (version.isNotBlank()) {
            Log.d(TAG, "Config params AppName: $version")
        }
    }

    companion object {

        private const val TAG = "MainActivity"

        // Remote Config keys
        private const val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
        private const val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(showAnimation: Boolean, onAnimationConsumed: () -> Unit) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSystemAlertWindowPermissionDialog by remember { mutableStateOf(false) }

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

    val systemAlertWindowPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { _ ->
            if (Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "다른 앱 위에 표시 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "다른 앱 위에 표시 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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

        if (!Settings.canDrawOverlays(context)) {
            showSystemAlertWindowPermissionDialog = true
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

    if (showSystemAlertWindowPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showSystemAlertWindowPermissionDialog = false },
            title = { Text("다른 앱 위에 표시 권한 요청") },
            text = { Text("앱의 중요한 알림을 다른 앱 위에 표시하려면 권한이 필요합니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSystemAlertWindowPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        systemAlertWindowPermissionLauncher.launch(intent)
                    }
                ) {
                    Text("설정으로 이동")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSystemAlertWindowPermissionDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val navigateTo: (AppDestinations) -> Unit = {
        newDestination ->
                if(currentDestination == AppDestinations.HELPS) {
                }
                currentDestination = newDestination
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
                    actions = {
                        IconButton(onClick = { 
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)

                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = { /* 백업 기능 실행 로직 */ }) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Backup", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeFragment(showAnimation, onAnimationConsumed)
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
    LISTS(R.string.tab_status, Icons.Default.DateRange, Color(0xFF008080)),
    HELPS(R.string.tab_helps, Icons.Default.Info, Color(0xFF00BCD4)),
}

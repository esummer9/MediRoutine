package com.ediapp.m1routine

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.ediapp.m1routine.ui.theme.MyApplicationTheme
import java.util.Calendar
import java.util.Locale

class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var medName by mutableStateOf("")
    private var medNickName by mutableStateOf("")
    private var morningEnabled by mutableStateOf(false)
    private var selectedTime by mutableStateOf("08:00")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("MediRoutine_prefs", MODE_PRIVATE)

        // Load initial settings from SharedPreferences
        medName = prefs.getString("med_name", "") ?: ""
        medNickName = prefs.getString("med_nick_name", "") ?: ""
        morningEnabled = prefs.getBoolean("daily_report_enabled", false)
        selectedTime = prefs.getString("notification_time", "08:00") ?: "08:00"

        setContent {
            MyApplicationTheme {
                SettingsScreen(
                    medName = medName,
                    medNickName = medNickName,
                    morningEnabled = morningEnabled,
                    selectedTime = selectedTime,
                    onMedNameChange = { medName = it },
                    onMedNickNameChange = { medNickName = it },
                    onMorningEnabledChange = { isEnabled ->
                        morningEnabled = isEnabled
                    },
                    onSelectedTimeChange = { time ->
                        selectedTime = time
                    }
                )
            }
        }
    }

    override fun onStop() {
        saveSettings(prefs, medName, medNickName, morningEnabled, selectedTime)
        super.onStop()
    }
}

fun scheduleNotification(context: Context, time: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent().also {
                it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(it)
            }
            return
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val timeParts = time.split(":")
    val hour = timeParts[0].toInt()
    val minute = timeParts[1].toInt()

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }

    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DATE, 1)
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

fun cancelNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    alarmManager.cancel(pendingIntent)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    medName: String,
    medNickName: String,
    morningEnabled: Boolean,
    selectedTime: String,
    onMedNameChange: (String) -> Unit,
    onMedNickNameChange: (String) -> Unit,
    onMorningEnabledChange: (Boolean) -> Unit,
    onSelectedTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    // SharedPreferences 인스턴스 가져오기 (MainActivity와 동일한 키 사용)
    val prefs = remember { context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                scheduleNotification(context, selectedTime)
                Toast.makeText(context, "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                onMorningEnabledChange(false) // Revert switch state
                Toast.makeText(context, "알림 권한이 없어 알림을 설정할 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A5ACD), // Settings destination color
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        // 변경사항 저장 로직 호출
                        saveSettings(prefs, medName, medNickName, morningEnabled, selectedTime)
                        // Activity 종료
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = medName,
                onValueChange = onMedNameChange, // Use the callback to update state
                label = { Text("약이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, // Set single line to hide the newline
            )
            Text("💊 네오텔미, 메트포르민 ...")
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = medNickName,
                onValueChange = onMedNickNameChange, // Use the callback to update state
                label = { Text("별명") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("💊 비타민,영양제,혈압약.  📢 공유&통계에 사용")
            Spacer(modifier = Modifier.height(32.dp))


            Text(text = "알림설정", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))
            MedTimeRow(
                label = "알림사용",
                checked = morningEnabled,
                onCheckedChange = { isChecked ->
                    onMorningEnabledChange(isChecked)
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                scheduleNotification(context, selectedTime)
                            } else {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            scheduleNotification(context, selectedTime)
                        }
                    } else {
                        cancelNotification(context)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            val times = (4..24).map { String.format(Locale.getDefault(), "%02d:00", it) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { /* readOnly = true */ },
                    label = { Text("알림시간") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    times.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time) },
                            onClick = {
                                onSelectedTimeChange(time) // Use the callback to update state
                                if (morningEnabled) {
                                    scheduleNotification(context, time)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }


        }
    }
}

// MedTimeRow 컴포저블은 그대로 유지 (SettingsFragment.kt에서 복사하거나, 동일 파일 내에 정의)
@Composable
fun MedTimeRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Text(text = label, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
            // (2) thumbColors 인스턴스 사용
            // Switch가 'On' 상태일 때의 핸들(thumb) 색상 설정
            checkedThumbColor = Color.White

            // 참고: 'Off' 상태일 때의 핸들 색상도 흰색으로 하려면:
            // uncheckedThumbColor = Color.White
        ))
    }
}

// 변경사항을 SharedPreferences에 저장하는 함수
fun saveSettings(prefs: SharedPreferences, medName: String?, medNickName: String?, morningEnabled: Boolean, selectedTime: String?) {
    prefs.edit {
        putString("med_name", medName)
        putString("med_nick_name", medNickName)
        putBoolean("daily_report_enabled", morningEnabled)
        putString("notification_time", selectedTime) // selectedTime도 함께 저장
    }
}

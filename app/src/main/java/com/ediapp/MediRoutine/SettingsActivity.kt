package com.ediapp.MediRoutine

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.ediapp.MediRoutine.ui.theme.MyApplicationTheme
import java.util.Calendar

class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var medName by mutableStateOf("")
    private var morningEnabled by mutableStateOf(false)
    private var selectedTime by mutableStateOf("08:00")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = applicationContext.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)

        // Load initial settings from SharedPreferences
        medName = prefs.getString("med_name", "") ?: ""
        morningEnabled = prefs.getBoolean("daily_report_enabled", false)
        selectedTime = prefs.getString("notification_time", "08:00") ?: "08:00"

        setContent {
            MyApplicationTheme {
                SettingsScreen(
                    medName = medName,
                    morningEnabled = morningEnabled,
                    selectedTime = selectedTime,
                    onMedNameChange = { medName = it },
                    onMorningEnabledChange = { isEnabled ->
                        morningEnabled = isEnabled
                        if (isEnabled) {
                            scheduleNotification(this, selectedTime)
                        } else {
                            cancelNotification(this)
                        }
                    },
                    onSelectedTimeChange = { time ->
                        selectedTime = time
                        if (morningEnabled) {
                            scheduleNotification(this, time)
                        }
                    }
                )
            }
        }
    }

    override fun onStop() {
        saveSettings(prefs, medName, morningEnabled, selectedTime)
        super.onStop()
    }
}

fun scheduleNotification(context: Context, time: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
    morningEnabled: Boolean,
    selectedTime: String,
    onMedNameChange: (String) -> Unit,
    onMorningEnabledChange: (Boolean) -> Unit,
    onSelectedTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    // SharedPreferences 인스턴스 가져오기 (MainActivity와 동일한 키 사용)
    val prefs = remember { context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE) }

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
                        saveSettings(prefs, medName, morningEnabled, selectedTime)
                        // Activity 종료
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                modifier = Modifier.fillMaxWidth()
            )
            Text("예시) 혈압약, 영양제 ...")
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "시간설정", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            val times = (6..24).map { String.format("%02d:00", it) }

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
                                Toast.makeText(context, "selectedTime $time.", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            MedTimeRow(
                label = "일일보고",
                checked = morningEnabled,

                onCheckedChange = onMorningEnabledChange // Use the callback to update state
            )
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// 변경사항을 SharedPreferences에 저장하는 함수
fun saveSettings(prefs: SharedPreferences, medName: String?, morningEnabled: Boolean, selectedTime: String?) {
    with(prefs.edit()) {
        putString("med_name", medName)
        putBoolean("daily_report_enabled", morningEnabled)
        putString("notification_time", selectedTime) // selectedTime도 함께 저장
        apply()
    }
}

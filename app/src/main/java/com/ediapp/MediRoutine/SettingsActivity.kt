package com.ediapp.MediRoutine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings // Settings 아이콘은 필요 없으므로 제거하거나 그대로 두어도 됩니다.
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediapp.MediRoutine.ui.theme.MyApplicationTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    // SharedPreferences 인스턴스 가져오기 (MainActivity와 동일한 키 사용)
    val prefs = remember { context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE) }

    // SettingsFragment에서 사용하던 상태 변수들을 rememberSaveable로 관리
    // 초기값을 SharedPreferences에서 불러옵니다.
    var medName by rememberSaveable { mutableStateOf(prefs.getString("med_name", "") ?: "") }
    var morningEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("daily_report_enabled", false)) }
    var selectedTime by rememberSaveable { mutableStateOf(prefs.getString("notification_time", "08:00") ?: "08:00") }

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            // SettingsFragment의 UI 요소들을 여기에 배치
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = medName,
                onValueChange = { newMedName ->
                    medName = newMedName
                },
                label = { Text("약이름") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("예시) 혈압약, 영양제 ...")
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "시간설정", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // ExposedDropdownMenuBox 로직
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
                                selectedTime = time
                                Toast.makeText(context, "selectedTime $time.", Toast.LENGTH_SHORT).show()
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MedTimeRow 컴포저블 재사용
            MedTimeRow2(
                label = "일일보고",
                checked = morningEnabled,
                onCheckedChange = {
                    morningEnabled = it
                }
            )
        }
    }
}

// MedTimeRow 컴포저블은 그대로 유지 (SettingsFragment.kt에서 복사하거나, 동일 파일 내에 정의)
@Composable
fun MedTimeRow2(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// 변경사항을 SharedPreferences에 저장하는 함수
fun saveSettings(prefs: SharedPreferences, medName: String, morningEnabled: Boolean, selectedTime: String) {
    with(prefs.edit()) {
        putString("med_name", medName)
        putBoolean("daily_report_enabled", morningEnabled)
        putString("notification_time", selectedTime) // selectedTime도 함께 저장
        apply()
    }
}

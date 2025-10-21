package com.ediapp.MediRoutine

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsFragment(
    medName: String,
    onMedNameChange: (String) -> Unit,
    morningEnabled: Boolean,
    onMorningEnabledChange: (Boolean) -> Unit,
    selectedTime: String,
    onSelectedTimeChange: (String) -> Unit
) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val times = (6..24).map { String.format("%02d:00", it) }

    Column(modifier = Modifier.padding(16.dp)) {
        Titlebar(title = "설정")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = medName,
            onValueChange = onMedNameChange,
            label = { Text("약이름") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "시간설정", fontSize = 20.sp)


        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { },
                label = { Text("알림시간") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                times.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            onSelectedTimeChange(time)
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
            onCheckedChange = onMorningEnabledChange
        )

    }
}

@Composable
fun MedTimeRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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

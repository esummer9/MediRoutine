package com.ediapp.MediRoutine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediapp.MediRoutine.R

@Composable
fun SettingsFragment() {
    var medName by remember { mutableStateOf("") }
    var morningEnabled by remember { mutableStateOf(false) }
    var lunchEnabled by remember { mutableStateOf(false) }
    var dinnerEnabled by remember { mutableStateOf(false) }
    var beforeBedEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Titlebar(title = stringResource(id = R.string.tab_settings))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = medName,
            onValueChange = { medName = it },
            label = { Text(stringResource(id = R.string.setting_med_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(id = R.string.setting_med_time_title), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        MedTimeRow(
            label = stringResource(id = R.string.setting_time_morning),
            checked = morningEnabled,
            onCheckedChange = { morningEnabled = it }
        )
        MedTimeRow(
            label = stringResource(id = R.string.setting_time_lunch),
            checked = lunchEnabled,
            onCheckedChange = { lunchEnabled = it }
        )
        MedTimeRow(
            label = stringResource(id = R.string.setting_time_dinner),
            checked = dinnerEnabled,
            onCheckedChange = { dinnerEnabled = it }
        )
        MedTimeRow(
            label = stringResource(id = R.string.setting_time_before_bed),
            checked = beforeBedEnabled,
            onCheckedChange = { beforeBedEnabled = it }
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
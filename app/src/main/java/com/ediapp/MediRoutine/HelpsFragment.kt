package com.ediapp.MediRoutine

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpsFragment() {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val times = (6..24).map { String.format("%02d:00", it) }


}
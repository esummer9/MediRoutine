package com.ediapp.MediRoutine

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ProfileFragment() {
    Column {
        Text("Profile Screen")
        Text(dayNames.joinToString(", "))
    }
}
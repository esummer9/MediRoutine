package com.ediapp.MediRoutine

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeFragment() {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = sdf.format(Date())
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var progress by remember { mutableStateOf(getProgress(context)) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Titlebar(title = "오늘")
            Text(
                text = "Home Screen - $currentDate",
                modifier = Modifier.clickable { showDialog = true }
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow)
            ) {
                Text(
                    text = "$progress%",
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )

        Column(modifier = Modifier.weight(1f)) {
            WeekCalendarView()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("확인") },
            text = { Text("약을 복용하셨습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newProgress = 0
                        saveProgress(context, newProgress)
                        progress = newProgress
                        Toast.makeText(context, "Yes", Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

private fun getProgress(context: Context): Int {
    val sharedPref = context.getSharedPreferences("MediRoutinePrefs", Context.MODE_PRIVATE)
    return sharedPref.getInt("progress", 100) // Default to 100
}

private fun saveProgress(context: Context, progress: Int) {
    val sharedPref = context.getSharedPreferences("MediRoutinePrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putInt("progress", progress)
        apply()
    }
}

data class CalendarDay(val dayNumber: String, val fullDate: String)

@Composable
fun WeekCalendarView() {
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val context = LocalContext.current
    val today = sdf.format(Date())

    // Current Week
    calendar.time = Date()
    calendar.add(Calendar.WEEK_OF_YEAR, -1)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    val currentWeekDays = (0..6).map {
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val fullDate = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        CalendarDay(day, fullDate)
    }

    // Previous Week
    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    val previousWeekDays = (0..6).map {
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val fullDate = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        CalendarDay(day, fullDate)
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEachIndexed { index, dayName ->
                val color = when (index) {
                    0 -> Color.Red
                    6 -> Color.Blue
                    else -> Color.Unspecified
                }
                Text(text = dayName, modifier = Modifier.weight(1f).padding(vertical = 8.dp), textAlign = TextAlign.Center, color = color)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            currentWeekDays.forEachIndexed { index, calendarDay ->
                val color = when (index) {
                    0 -> Color.Red
                    6 -> Color.Blue
                    else -> Color.Unspecified
                }
                Text(
                    text = calendarDay.dayNumber,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .background(if (calendarDay.fullDate == today) Color.LightGray else Color.Transparent)
                        .clickable { Toast.makeText(context, calendarDay.fullDate, Toast.LENGTH_SHORT).show() },
                    textAlign = TextAlign.Center,
                    color = color
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            previousWeekDays.forEachIndexed { index, calendarDay ->
                val color = when (index) {
                    0 -> Color.Red
                    6 -> Color.Blue
                    else -> Color.Unspecified
                }
                Text(
                    text = calendarDay.dayNumber,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .background(if (calendarDay.fullDate == today) Color.LightGray else Color.Transparent)
                        .clickable { Toast.makeText(context, calendarDay.fullDate, Toast.LENGTH_SHORT).show() },
                    textAlign = TextAlign.Center,
                    color = color
                )
            }
        }
    }
}

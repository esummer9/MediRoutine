package com.ediapp.MediRoutine

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

//val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
fun HomeFragment() {

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = sdf.format(Date())

    val currentMonth = SimpleDateFormat("yy.MM", Locale.getDefault()).format(Date())

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    var progress by remember { mutableStateOf(dbHelper.getDrugActionCount())
}

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
//            Titlebar(title = "오늘")
            Text(
                text = "사용기간 - $currentDate",
                modifier = Modifier.clickable { showDialog = true }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GridItem(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), index = 0, title = currentMonth, fontSize = 18.sp, onDrugTaken = { progress = dbHelper.getDrugActionCount() })
                    GridItem(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), index = 1, title = "달성율", progress = progress)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
//                    GridItem(modifier = Modifier.weight(1f).fillMaxHeight(), title = "저녁")
//                    GridItem(modifier = Modifier.weight(1f).fillMaxHeight(), title = "취침 전")
                }
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

@Composable
fun GridItem(modifier: Modifier = Modifier, index : Int = 1, title: String, fontSize: TextUnit = 18.sp, progress: Int = 0, onDrugTaken: () -> Unit = {}) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (index == 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now()
                val dayOfWeek = today.dayOfWeek
                val dayOfMonth = today.dayOfMonth.toString()
                val dayOfWeekDisplayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)

                val color = when (dayOfWeek) {
                    DayOfWeek.SUNDAY -> Color.Red
                    DayOfWeek.SATURDAY -> Color.Blue
                    else -> Color.Black
                }

                Text(text = title, fontSize = fontSize, color = color)
                Text(text = "($dayOfWeekDisplayName)", fontSize = fontSize, color = color)
                Text(text = dayOfMonth, fontSize = 28.sp, color = color)
                Button(onClick = {
                    val dbHelper = DatabaseHelper(context)
                    val newId = dbHelper.addDoAction()

                    Toast.makeText(context, "복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT).show()
                    onDrugTaken()
                }) {
                    Text("복용")
                }
            }
        } else if (index == 1) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = title, fontSize = fontSize)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.Yellow)
                ) {
                    Text(
                        text = "$progress%",
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                }
            }
        } else {
            Text(text = title, fontSize = fontSize)
        }
    }
}

data class CalendarDay(val dayNumber: String, val fullDate: String)

@Composable
fun WeekCalendarView() {

    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val context = LocalContext.current
    val today = sdf.format(Date())
    val dbHelper = DatabaseHelper(context)
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val actions = dbHelper.getAllActions(monthFormat.format(Date()))

    // Current Week
    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.add(Calendar.WEEK_OF_YEAR, -1)

    val twoWeekDays = (0..13).map {
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val fullDate = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        CalendarDay(day, fullDate)
    }

    calendar.time = Date()

    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    val currentWeekDays = (0..6).map {
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
                Text(text = dayName, modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp), textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = color)
            }
        }

        for (range in listOf(0..6, 7..13)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                range.forEach { index ->
                    val calendarDay = twoWeekDays[index]
                    val color = when (index) {
                        0,7 -> Color.Red
                        6,13 -> Color.Blue
                        else -> Color.Unspecified
                    }
                    val isTaken = actions.any { it.actRegisteredAt?.startsWith(calendarDay.fullDate) == true }
                    val backgroundColor = when {
                        calendarDay.fullDate == today -> Color.Transparent
                        isTaken -> Color.LightGray
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
                            ,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calendarDay.dayNumber,
                            modifier = Modifier.padding(4.dp), // Added padding for the text itself
                            textAlign = TextAlign.Center,
                            fontWeight = if (calendarDay.fullDate != today) FontWeight.Normal else FontWeight.Bold,
                            fontStyle = if (calendarDay.fullDate != today) FontStyle.Italic else FontStyle.Normal,
                            fontSize = if (calendarDay.fullDate != today) 18.sp else 22.sp,
                            color = color
                        )
                    }
                }
            }
        }

    }
}

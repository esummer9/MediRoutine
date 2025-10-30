package com.ediapp.MediRoutine

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ediapp.MediRoutine.model.Action
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@Composable
fun HomeFragment(showAnimationFromNotification: Boolean = false, onAnimationConsumed: () -> Unit = {}) {

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentMonth = SimpleDateFormat("yy.MM", Locale.getDefault()).format(Date())

    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var progress by remember { mutableStateOf(dbHelper.getDrugTodayCount()) }
    var achievementRate by remember { mutableStateOf(0) }
    var totalDays by remember { mutableStateOf(0L) }

    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    var actions by remember { mutableStateOf(dbHelper.getDrugListsByMonth(monthFormat.format(Date()))) }

    var showAnimation by remember { mutableStateOf(false) }
    val size = remember { Animatable(20f) }

    // Get SharedPreferences instance
    val prefs = context.getSharedPreferences("MediRoutine_prefs", Context.MODE_PRIVATE)

    fun updateAchievementRate() {
        val (startDate, drugCount) = dbHelper.getAchievementStats()
        if (startDate != null && drugCount > 0) {
            val diff = Date().time - startDate.time
            val days = TimeUnit.MILLISECONDS.toDays(diff) + 1
            achievementRate = ((drugCount.toFloat() / days) * 100).toInt()
            totalDays = days
        } else {
            achievementRate = 0
            totalDays = 0L
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                actions = dbHelper.getDrugListsByMonth(monthFormat.format(Date()))
                progress = dbHelper.getDrugTodayCount()
                updateAchievementRate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        updateAchievementRate()
    }

    LaunchedEffect(showAnimationFromNotification) {
        if (showAnimationFromNotification) {
            showAnimation = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color.Transparent)
                ) {
                    Text(
                        text = "오늘",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }

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
                        GridItem(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            index = 0,
                            title = currentMonth,
                            fontSize = 18.sp,
                            onDrugTaken = {
                                val newId = dbHelper.addDrugAction()
                                val medNickName = prefs.getString("med_nick_name", "") ?: ""
                                Toast
                                    .makeText(context, "$medNickName 을 복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT)
                                    .show()
                                progress = dbHelper.getDrugTodayCount()
                                actions =
                                    dbHelper.getDrugListsByMonth(monthFormat.format(Date()))
                                updateAchievementRate()
                                showAnimation = true
                            }
                        )
                        GridItem(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            index = 1,
                            title = "달성율",
                            progress = achievementRate,
                            days = totalDays
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // GridItem 추가 영역
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
                WeekCalendarView(actions = actions)
            }
        }

        if (showAnimation) {
            LaunchedEffect(showAnimation) {
                // Grow
                size.animateTo (
                    targetValue = 200f,
                    animationSpec = tween(durationMillis = 1000)
                )
                // Shrink
                size.animateTo (
                    targetValue = 20f,
                    animationSpec = tween(durationMillis = 1000)
                )
                showAnimation = false
                size.snapTo(20f) // Reset for next time
                onAnimationConsumed()
            }

            Image(
                painter = painterResource(id = R.drawable.med_routine_256),
                contentDescription = null,
                modifier = Modifier
                    .size(size.value.dp)
                    .align(Alignment.Center)
            )
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
}

@Composable
fun GridItem(modifier: Modifier = Modifier, index : Int = 1, title: String, fontSize: TextUnit = 18.sp, progress: Int = 0, maxProgress: Int = 100, days: Long = 0, onDrugTaken: () -> Unit = {}) {
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

                Text(text = title, fontSize = fontSize, color = Color.Black)
                Text(text = "($dayOfWeekDisplayName)", fontSize = fontSize, color = color)
                Text(text = dayOfMonth, fontSize = 28.sp, color = color)
                Button(
                    onClick = onDrugTaken,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray) // Set button color
                ) {
                    Text("복용", color = Color.White) // Set text color to white
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
                    modifier = Modifier.size(90.dp)
                ) {
                    val color = when (progress) {
                        in -100..59 -> Color.Red
                        in 60..89 -> Color.Blue
                        else -> Color(0xFF00668B)
                    }
                    CircularProgressIndicator(
                        progress = progress.toFloat() / maxProgress,
                        modifier = Modifier.size(90.dp),
                        color = color,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "$progress%",
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                }
                Text(text = "$days days")
            }
        } else {
            Text(text = title, fontSize = fontSize)
        }
    }
}

data class CalendarDay(val dayNumber: String, val fullDate: String)

@Composable
fun WeekCalendarView(actions: List<Action>) {

    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())

    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.add(Calendar.WEEK_OF_YEAR, -1)

    val twoWeekDays = (0..13).map {
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
                    val color = when (index % 7) { 
                        0 -> Color.Red
                        6 -> Color.Blue
                        else -> Color.Unspecified
                    }
                    val isTaken = actions.any { it.actRegisteredAt?.startsWith(calendarDay.fullDate) == true }
                    val backgroundColor = when {
                        isTaken -> Color.LightGray
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(6.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calendarDay.dayNumber,
                            modifier = Modifier.padding(4.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (calendarDay.fullDate != today) FontWeight.Normal else FontWeight.Bold,
                            fontSize = if (calendarDay.fullDate != today) 18.sp else 19.sp,
                            fontStyle = if (calendarDay.fullDate != today) FontStyle.Italic else FontStyle.Normal,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

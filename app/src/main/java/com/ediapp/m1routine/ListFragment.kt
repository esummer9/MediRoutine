package com.ediapp.m1routine

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediapp.m1routine.model.Action
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ViewType {
    CALENDAR, LIST
}

@Composable
fun ListFragment() {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var viewType by remember { mutableStateOf(ViewType.CALENDAR) }
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    var actions by remember { mutableStateOf<List<Action>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var dateForDialog by remember { mutableStateOf<Calendar?>(null) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }
    var dayToDelete by remember { mutableStateOf<Int?>(null) }

    val monthFormat = remember { SimpleDateFormat("yyyy년 MM월", Locale.KOREAN) }
    val monthFormatForQuery = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun refetchActions() {
        val monthStr = monthFormatForQuery.format(currentDate.time)
        actions = dbHelper.getDrugListsByMonthOrWeek(month = monthStr, orderBy = "act_registered_at", orderDirection = "DESC")
    }

    LaunchedEffect(currentDate) {
        refetchActions()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dateForDialog = null
                showDialog = true
            }, shape = CircleShape) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCal = currentDate.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        currentDate = newCal
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        text = monthFormat.format(currentDate.time),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        currentDate = Calendar.getInstance()
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "이번달",
                            tint = Color.DarkGray
                        )
                    }
                }

                Row {
                    TextButton(onClick = { viewType = ViewType.CALENDAR }) {
                        Text("달력")
                    }
                    TextButton(onClick = { viewType = ViewType.LIST }) {
                        Text("목록")
                    }
                }
            }

            when (viewType) {
                ViewType.LIST -> {
                    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                        items(actions) { action ->
                            ActionCard(action) {
                                dbHelper.deleteDrugAction(action.id)
                                refetchActions()
                            }
                        }
                    }
                }
                ViewType.CALENDAR -> {
                    val highlightedDays = remember(actions) {
                        actions.mapNotNull { action ->
                            try {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val date = dateFormat.parse(action.actRegisteredAt)
                                date?.let {
                                    val cal = Calendar.getInstance()
                                    cal.time = it
                                    cal.get(Calendar.DAY_OF_MONTH)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }.distinct()
                    }
                    CalendarView(
                        currentDate = currentDate,
                        highlightedDays = highlightedDays,
                        onDayClick = { day ->
                            if (highlightedDays.contains(day)) {
                                dayToDelete = day
                                showDeleteDayDialog = true
                            } else {
                                val clickedCalendar = currentDate.clone() as Calendar
                                clickedCalendar.set(Calendar.DAY_OF_MONTH, day)
                                dateForDialog = clickedCalendar
                                showDialog = true
                            }
                        }
                    )
                    val view = LocalView.current
                    Button(
                        onClick = {
                            saveCalendarViewAsImage(view, context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)

                    ) {
                        Text("달력을 사진으로 기록", color = Color.White)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddRefillDialog(
            initialCalendar = dateForDialog,
            onDismiss = { showDialog = false },
            onConfirm = { date ->
                val newId = dbHelper.addDrugAction(date)
                refetchActions()

                val medNickName = prefs.getString("med_nick_name", "") ?: ""
                Toast
                    .makeText(context, "$medNickName 을 복용했습니다. (ID: $newId)", Toast.LENGTH_SHORT)
                    .show()

//                Toast.makeText(context, "추가 되었습니다 (ID: $newId)", Toast.LENGTH_SHORT).show()
                showDialog = false
            }
        )
    }

    if (showDeleteDayDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDayDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 이 항목을 삭제 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dayToDelete?.let { day ->
                            val cal = (currentDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                            val datePrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                            actions.filter { it.actRegisteredAt!!.startsWith(datePrefix) }.forEach {
                                dbHelper.deleteDrugAction(it.id)
                            }
                            refetchActions()
                        }
                        showDeleteDayDialog = false
                        dayToDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDayDialog = false
                        dayToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun CalendarView(currentDate: Calendar, highlightedDays: List<Int>, onDayClick: (Int) -> Unit) {
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val days = (1..daysInMonth).toList()
    val emptyCells = List(firstDayOfWeek) { }

    val today = Calendar.getInstance()

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            dayNames.forEachIndexed { index, label ->
                val color = when (index) {
                    0 -> Color.Red
                    6 -> Color.Blue
                    else -> Color.Unspecified
                }
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(emptyCells.size) {
                Box(modifier = Modifier.size(40.dp)) {}
            }
            items(days.size) { dayIndex ->
                val day = days[dayIndex]
                val isHighlighted = highlightedDays.contains(day)

                val cellDate = (currentDate.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val isFuture = cellDate.after(today)
                var textColor = Color.DarkGray
                if (isFuture)
                    textColor = Color.LightGray
                else if (isHighlighted)
                    textColor = Color.White

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(3.dp)
                        .clickable(enabled = !isFuture) { onDayClick(day) }
                        .background(
                            color = if (isHighlighted) Color(0xFF008080) else Color.Transparent,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(if (isHighlighted) 10.dp else 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

// Function to save the CalendarView as an image
private fun saveCalendarViewAsImage(view: View, context: Context) {
    // Implementation for saving the image
    // This part requires more context or specific implementation details
    // For now, we'll just log a message
    Log.d("ListFragment", "Save calendar view as image clicked")
    Toast.makeText(context, "사진 저장 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
}


@Composable
fun ActionCard(action: Action, onDelete: () -> Unit) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDayOfWeek(action.actRegisteredAt),
                modifier = Modifier.weight(1f)
            )
            Column(modifier = Modifier.weight(2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${action.actMessage}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
                Text("${action.actRegisteredAt}")
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 이 항목을 삭제 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

private fun getDayOfWeek(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val dayOfWeekSdf = SimpleDateFormat("E", Locale.getDefault())
        date?.let { dayOfWeekSdf.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}
